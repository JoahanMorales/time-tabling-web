package programacion.horarios.constructor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.stream.Collectors;

import programacion.horarios.constructor.ConstructorHorario;
import programacion.horarios.nucleo.Materia;
import programacion.horarios.nucleo.RepositorioMaterias;
import programacion.horarios.nucleo.Seleccion;

public class ConstructorHorarioProfesoresFijados extends ConstructorHorario {

    private static final boolean DEBUG = false;

    /* ======= Entrada: profesores fijados (materia -> profesor) ======= */
    private final Map<String, String> profesoresFijados;

    /* ======= (Opcional) limitar materias objetivo explícitamente ======= */
    private final Set<String> materiasObjetivoExplicitas; // puede ser null

    /* ======= Estado A* ======= */
    private static class NodoEstado {
        final List<Seleccion> asignadas;          // asignaciones ya tomadas
        final Set<String> materiasRestantes;      // nombres de materia faltantes
        final double costoG;                      // costo acumulado (sum(10 - calif))
        final double costoH;                      // heurística optimista

        NodoEstado(List<Seleccion> asignadas, Set<String> restantes, double g, double h) {
            this.asignadas = asignadas;
            this.materiasRestantes = restantes;
            this.costoG = g;
            this.costoH = h;
        }
        double costoF() { return costoG + costoH; }
    }

    /* ======================== Constructores ======================== */

    /** A* con profesores fijados; materias objetivo = mejor grupo por cobertura. */
    public ConstructorHorarioProfesoresFijados(RepositorioMaterias repositorio,
                                                    String nombreGrupoObjetivo,
                                                    Map<String, String> profesoresFijados) {
        super(repositorio, nombreGrupoObjetivo);
        this.profesoresFijados = (profesoresFijados != null) ? profesoresFijados : Map.of();
        this.materiasObjetivoExplicitas = null;
        if (DEBUG) {
            System.out.println("🔧 A* con profesores fijados:");
            this.profesoresFijados.forEach((m,p) -> System.out.println("  " + m + " -> " + p));
        }
    }

    /** A* con profesores fijados y un conjunto explícito de materias objetivo. */
    public ConstructorHorarioProfesoresFijados(RepositorioMaterias repositorio,
                                                    String nombreGrupoObjetivo,
                                                    Map<String, String> profesoresFijados,
                                                    Set<String> materiasObjetivo) {
        super(repositorio, nombreGrupoObjetivo);
        this.profesoresFijados = (profesoresFijados != null) ? profesoresFijados : Map.of();
        this.materiasObjetivoExplicitas = (materiasObjetivo != null) ? new HashSet<>(materiasObjetivo) : null;
        if (DEBUG) {
            System.out.println("🔧 A* con profesores fijados + materias explícitas:");
            this.profesoresFijados.forEach((m,p) -> System.out.println("  " + m + " -> " + p));
            System.out.println("  Materias objetivo: " + this.materiasObjetivoExplicitas);
        }
    }

    /* ======================== API ======================== */

    @Override
    public List<Seleccion> construir() {
        final Set<String> materiasObjetivo;

        if (materiasObjetivoExplicitas != null && !materiasObjetivoExplicitas.isEmpty()) {
            // 1) Usar las que te interese resolver (p.ej. esas 5)
            materiasObjetivo = new HashSet<>(materiasObjetivoExplicitas);
        } else {
            // 2) Si no te pasan un set, tomamos el mejor grupo por cobertura (como tu A*)
            String mejorGrupo = repositorio.encontrarGrupoMejorCobertura();
            if (mejorGrupo == null) return List.of();
            materiasObjetivo = new HashSet<>(repositorio.calcularCoberturaGrupos().get(mejorGrupo));
        }

        // Aseguramos que las materias con profesor fijado estén incluidas
        materiasObjetivo.addAll(profesoresFijados.keySet());

        return buscarConAStar(materiasObjetivo);
    }

    @Override
    protected boolean validarRestricciones(List<Materia> seleccionadas) {
        return true; // ya se valida compatibilidad al expandir nodos
    }

    /* ======================== Núcleo A* ======================== */

    private List<Seleccion> buscarConAStar(Set<String> materiasRequeridas) {
        PriorityQueue<NodoEstado> open = new PriorityQueue<>(Comparator.comparingDouble(NodoEstado::costoF));

        NodoEstado inicial = new NodoEstado(
                new ArrayList<>(),
                new HashSet<>(materiasRequeridas),
                0.0,
                calcularHeuristica(materiasRequeridas, List.of())
        );
        open.offer(inicial);

        // Para evitar expandir estados equivalentes repetidos
        Set<String> cerrados = new HashSet<>();

        while (!open.isEmpty()) {
            NodoEstado actual = open.poll();

            if (actual.materiasRestantes.isEmpty()) {
                if (DEBUG) System.out.println("🎉 Objetivo alcanzado. Costo=" + actual.costoF());
                return actual.asignadas;
            }

            String clave = claveEstado(actual.asignadas);
            if (cerrados.contains(clave)) continue;
            cerrados.add(clave);

            // Elegir la siguiente materia por MRV (la de menos opciones válidas)
            String proxima = seleccionarMateriaMRV(actual.materiasRestantes, actual.asignadas);

            // Expandir sucesores (opciones compatibles)
            for (Materia opcion : opcionesMateriaFiltradas(proxima, actual.asignadas)) {
                List<Seleccion> nuevasAsignadas = new ArrayList<>(actual.asignadas);
                nuevasAsignadas.add(new Seleccion(clonarAGrupo(opcion), opcion.obtenerGrupo()));

                Set<String> nuevasRestantes = new HashSet<>(actual.materiasRestantes);
                nuevasRestantes.remove(proxima);

                double g = actual.costoG + (10.0 - opcion.obtenerCalificacion());
                double h = calcularHeuristica(nuevasRestantes, nuevasAsignadas);

                open.offer(new NodoEstado(nuevasAsignadas, nuevasRestantes, g, h));
            }
        }
        return List.of(); // sin solución
    }

    /* ======================== Heurística y Sucesores ======================== */

    /** Selecciona la materia con menos opciones válidas (MRV). */
    private String seleccionarMateriaMRV(Set<String> materiasRestantes, List<Seleccion> asignadas) {
        return materiasRestantes.stream()
                .min(Comparator.comparingInt(m -> opcionesMateriaFiltradas(m, asignadas).size()))
                .orElseGet(() -> materiasRestantes.iterator().next());
    }

    /** Opciones válidas para una materia: respeta profesor fijado y compatibilidad de horarios. */
    private List<Materia> opcionesMateriaFiltradas(String materia, List<Seleccion> asignadas) {
        List<Materia> candidatas = repositorio.obtenerPorNombreMateria(materia);
        // Si profesor está fijado, filtrar por nombre (igualdad flexible)
        if (profesoresFijados.containsKey(materia)) {
            String requerido = profesoresFijados.get(materia);
            candidatas = candidatas.stream()
                    .filter(m -> equalsNombre(m.obtenerProfesor().obtenerNombreCompleto(), requerido))
                    .collect(Collectors.toList());
        }
        // Compatibilidad horaria con lo ya asignado
        return candidatas.stream()
                .filter(op -> esCompatible(op, asignadas))
                .collect(Collectors.toList());
    }

    /** Heurística: suma de (10 - mejorCalifPosible) por materia restante, bajo restricciones actuales. */
    private double calcularHeuristica(Set<String> restantes, List<Seleccion> asignadas) {
        double h = 0.0;
        for (String m : restantes) {
            double mejor = opcionesMateriaFiltradas(m, asignadas).stream()
                    .mapToDouble(Materia::obtenerCalificacion)
                    .max()
                    .orElse(0.0);
            h += (10.0 - mejor);
        }
        return h;
    }

    /* ======================== Utilidades ======================== */

    private boolean esCompatible(Materia nueva, List<Seleccion> asignadas) {
        for (Seleccion sel : asignadas) {
            if (nueva.tieneConflictoCon(sel.asignada())) return false;
        }
        return true;
    }

    private String claveEstado(List<Seleccion> asignadas) {
        return asignadas.stream()
                .map(sel -> sel.asignada().obtenerIdentificador())
                .sorted()
                .collect(Collectors.joining("|"));
    }

    /** Igualdad flexible de nombres de profesor (normaliza acentos y espacios). */
    private boolean equalsNombre(String a, String b) {
        return normalizar(a).equals(normalizar(b));
    }

    private String normalizar(String s) {
        if (s == null) return "";
        return s.trim()
                .toUpperCase()
                .replaceAll("\\s+", " ")
                .replace('Ñ','N')
                .replaceAll("[ÁÀÂÄÃ]", "A")
                .replaceAll("[ÉÈÊË]", "E")
                .replaceAll("[ÍÌÎÏ]", "I")
                .replaceAll("[ÓÒÔÖÕ]", "O")
                .replaceAll("[ÚÙÛÜ]", "U");
    }
}

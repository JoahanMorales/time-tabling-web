package programacion.horarios.algoritmo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
import programacion.horarios.constructor.ConstructorHorario;
import programacion.horarios.nucleo.Materia;
import programacion.horarios.nucleo.RepositorioMaterias;
import programacion.horarios.nucleo.Seleccion;

public class ConstructorHorarioAStar extends ConstructorHorario {
    
    private static class NodoEstado {
        final List<Seleccion> asignadas;
        final Set<String> materiasRestantes;
        final double costoG; // Costo actual
        final double costoH; // Heurística
        
        NodoEstado(List<Seleccion> asignadas, Set<String> restantes, 
                  double costoG, double costoH) {
            this.asignadas = asignadas;
            this.materiasRestantes = restantes;
            this.costoG = costoG;
            this.costoH = costoH;
        }
        
        double costoF() { return costoG + costoH; }
    }
    
    public ConstructorHorarioAStar(RepositorioMaterias repositorio, 
                                  String nombreGrupoObjetivo) {
        super(repositorio, nombreGrupoObjetivo);
    }
    
    @Override
    public List<Seleccion> construir() {
        String mejorGrupo = repositorio.encontrarGrupoMejorCobertura();
        if (mejorGrupo == null) return List.of();
        
        Set<String> materiasRequeridas = repositorio.calcularCoberturaGrupos()
            .get(mejorGrupo);
        
        return buscarConAStar(materiasRequeridas);
    }
    
    private List<Seleccion> buscarConAStar(Set<String> materiasRequeridas) {
        PriorityQueue<NodoEstado> colaPrioridad = new PriorityQueue<>(
            Comparator.comparingDouble(NodoEstado::costoF)
        );
        
        // Estado inicial
        NodoEstado inicial = new NodoEstado(
            new ArrayList<>(), 
            new HashSet<>(materiasRequeridas), 
            0.0, 
            calcularHeuristica(materiasRequeridas)
        );
        
        colaPrioridad.offer(inicial);
        Set<String> visitados = new HashSet<>();
        
        while (!colaPrioridad.isEmpty()) {
            NodoEstado actual = colaPrioridad.poll();
            
            // Estado objetivo alcanzado
            if (actual.materiasRestantes.isEmpty()) {
                return actual.asignadas;
            }
            
            String claveEstado = generarClaveEstado(actual);
            if (visitados.contains(claveEstado)) continue;
            visitados.add(claveEstado);
            
            // Generar estados sucesores
            generarSucesores(actual, colaPrioridad);
        }
        
        return List.of(); // No se encontró solución
    }
    
    private void generarSucesores(NodoEstado actual, 
                                 PriorityQueue<NodoEstado> cola) {
        if (actual.materiasRestantes.isEmpty()) return;
        
        String proximaMateria = actual.materiasRestantes.iterator().next();
        List<Materia> opciones = repositorio.obtenerPorNombreMateria(proximaMateria);
        
        for (Materia opcion : opciones) {
            if (esCompatible(opcion, actual.asignadas)) {
                List<Seleccion> nuevasAsignadas = new ArrayList<>(actual.asignadas);
                nuevasAsignadas.add(new Seleccion(clonarAGrupo(opcion), 
                                                opcion.obtenerGrupo()));
                
                Set<String> nuevasRestantes = new HashSet<>(actual.materiasRestantes);
                nuevasRestantes.remove(proximaMateria);
                
                double nuevoCostoG = actual.costoG + (10.0 - opcion.obtenerCalificacion());
                double nuevoCostoH = calcularHeuristica(nuevasRestantes);
                
                NodoEstado sucesor = new NodoEstado(nuevasAsignadas, nuevasRestantes, 
                                                   nuevoCostoG, nuevoCostoH);
                cola.offer(sucesor);
            }
        }
    }
    
    private boolean esCompatible(Materia nueva, List<Seleccion> asignadas) {
        for (Seleccion sel : asignadas) {
            if (nueva.tieneConflictoCon(sel.asignada())) {
                return false;
            }
        }
        return true;
    }
    
    private double calcularHeuristica(Set<String> materiasRestantes) {
        // Heurística: suma de las mejores calificaciones posibles restantes
        return materiasRestantes.stream()
            .mapToDouble(materia -> {
                return repositorio.obtenerPorNombreMateria(materia).stream()
                    .mapToDouble(Materia::obtenerCalificacion)
                    .max().orElse(0.0);
            })
            .map(cal -> 10.0 - cal) // Convertir a costo (menor calificación = mayor costo)
            .sum();
    }
    
    private String generarClaveEstado(NodoEstado estado) {
        StringBuilder sb = new StringBuilder();
        estado.asignadas.stream()
            .map(sel -> sel.asignada().obtenerIdentificador())
            .sorted()
            .forEach(id -> sb.append(id).append("|"));
        return sb.toString();
    }
    
    @Override
    protected boolean validarRestricciones(List<Materia> seleccionadas) {
        return true; // Ya validado durante la búsqueda
    }
}
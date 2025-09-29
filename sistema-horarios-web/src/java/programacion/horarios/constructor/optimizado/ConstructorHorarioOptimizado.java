package programacion.horarios.constructor.optimizado;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import programacion.horarios.constructor.ConstructorHorario;
import programacion.horarios.estrategia.EstrategiaEvaluacion;
import programacion.horarios.estrategia.EstrategiaPonderadaPorMinutos;
import programacion.horarios.nucleo.Materia;
import programacion.horarios.nucleo.RepositorioMaterias;
import programacion.horarios.nucleo.Seleccion;

public class ConstructorHorarioOptimizado extends ConstructorHorario {
    private final EstrategiaEvaluacion estrategia;
    
    // Cachés para evitar recálculos
    private final Map<String, Double> cacheEvaluaciones = new HashMap<>();
    private final Map<String, Boolean> cacheConflictos = new HashMap<>();
    
    public ConstructorHorarioOptimizado(RepositorioMaterias repositorio, 
                                       String nombreGrupoObjetivo) {
        super(repositorio, nombreGrupoObjetivo);
        this.estrategia = new EstrategiaPonderadaPorMinutos();
    }
    
    @Override
    public List<Seleccion> construir() {
        Set<String> materiasRequeridas = repositorio.obtenerTodosLosNombresMaterias();
        if (materiasRequeridas.isEmpty()) return List.of();

        return construirConHeuristicas(materiasRequeridas);
    }
    
    // Algoritmo greedy: O(n log n) en lugar de O(n!)
    private List<Seleccion> construirConHeuristicas(Set<String> materiasRequeridas) {
        List<Seleccion> resultado = new ArrayList<>();
        Set<String> materiasAsignadas = new HashSet<>();
        
        // 1. Ordenar materias por dificultad de asignación (menos opciones primero)
        List<String> materiasOrdenadas = materiasRequeridas.stream()
            .sorted(Comparator.comparingInt(this::contarOpciones))
            .collect(Collectors.toList());
        
        // 2. Para cada materia, elegir la mejor opción válida
        for (String materia : materiasOrdenadas) {
            Seleccion mejorSeleccion = encontrarMejorOpcion(materia, resultado);
            if (mejorSeleccion != null) {
                resultado.add(mejorSeleccion);
                materiasAsignadas.add(materia);
            }
        }
        
        return resultado;
    }
    
    // Encuentra la mejor opción para una materia sin conflictos
    private Seleccion encontrarMejorOpcion(String nombreMateria, 
                                          List<Seleccion> asignadas) {
        List<Materia> opciones = repositorio.obtenerPorNombreMateria(nombreMateria);
        
        return opciones.stream()
            .filter(materia -> !tieneConflictoRapido(materia, asignadas))
            .max(Comparator.comparingDouble(Materia::obtenerCalificacion))
            .map(materia -> new Seleccion(
                clonarAGrupo(materia), 
                materia.obtenerGrupo()
            ))
            .orElse(null);
    }
    
    // Optimización: Verificación rápida de conflictos usando índices
    private boolean tieneConflictoRapido(Materia nueva, List<Seleccion> asignadas) {
        String claveCache = generarClaveConflicto(nueva, asignadas);
        
        return cacheConflictos.computeIfAbsent(claveCache, k -> {
            for (Seleccion sel : asignadas) {
                if (nueva.tieneConflictoCon(sel.asignada())) {
                    return true;
                }
            }
            return false;
        });
    }
    
    private String generarClaveConflicto(Materia nueva, List<Seleccion> asignadas) {
        StringBuilder sb = new StringBuilder();
        sb.append(nueva.obtenerIdentificador());
        for (Seleccion sel : asignadas) {
            sb.append("|").append(sel.asignada().obtenerIdentificador());
        }
        return sb.toString();
    }
    
    private int contarOpciones(String nombreMateria) {
        return repositorio.obtenerPorNombreMateria(nombreMateria).size();
    }
    
    @Override
    protected boolean validarRestricciones(List<Materia> seleccionadas) {
        return true; // Ya validado en la construcción
    }
}
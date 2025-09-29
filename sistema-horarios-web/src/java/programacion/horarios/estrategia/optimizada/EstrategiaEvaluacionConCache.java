package programacion.horarios.estrategia.optimizada;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import programacion.horarios.estrategia.EstrategiaEvaluacion;
import programacion.horarios.nucleo.Materia;

public class EstrategiaEvaluacionConCache implements EstrategiaEvaluacion {
    private final EstrategiaEvaluacion estrategiaBase;
    private final Map<String, Double> cache = new ConcurrentHashMap<>();
    
    public EstrategiaEvaluacionConCache(EstrategiaEvaluacion base) {
        this.estrategiaBase = base;
    }
    
    @Override
    public double evaluar(List<Materia> materias) {
        String clave = generarClave(materias);
        return cache.computeIfAbsent(clave, k -> estrategiaBase.evaluar(materias));
    }
    
    private String generarClave(List<Materia> materias) {
        return materias.stream()
            .map(Materia::obtenerIdentificador)
            .sorted()
            .collect(Collectors.joining("|"));
    }
    
    @Override
    public String obtenerNombreEstrategia() {
        return estrategiaBase.obtenerNombreEstrategia() + " (Cached)";
    }
}
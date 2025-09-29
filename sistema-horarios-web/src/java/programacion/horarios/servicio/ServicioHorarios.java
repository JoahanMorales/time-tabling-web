package programacion.horarios.servicio;

import java.util.concurrent.ConcurrentHashMap;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import programacion.horarios.constructor.*;
import programacion.horarios.estrategia.*;
import programacion.horarios.nucleo.*;

public class ServicioHorarios {
    private final RepositorioMaterias repositorio;
    private final Map<String, ResultadoCache> cache;
    
    public ServicioHorarios(RepositorioMaterias repositorio) {
        this.repositorio = repositorio;
        this.cache = new ConcurrentHashMap<>();
    }
    
    // Método principal - reemplaza el uso directo del constructor
    public List<Seleccion> crearHorario(String grupoObjetivo, EstrategiaEvaluacion estrategia) {
        String claveCache = generarClaveCache(grupoObjetivo, estrategia);
        
        ResultadoCache resultado = cache.get(claveCache);
        if (resultado != null && !estaExpirado(resultado)) {
            System.out.println("✓ Cache hit - resultado en <1ms");
            return resultado.selecciones;
        }
        
        // Calcular si no está en cache
        long inicio = System.currentTimeMillis();
        ConstructorHorario constructor = new ConstructorHorarioMaximaCobertura(
            repositorio, grupoObjetivo, estrategia
        );
        List<Seleccion> nuevasSelecciones = constructor.construir();
        long tiempo = System.currentTimeMillis() - inicio;
        
        // Guardar en cache
        cache.put(claveCache, new ResultadoCache(nuevasSelecciones, LocalDateTime.now()));
        System.out.println("⚠ Cache miss - calculado en " + tiempo + "ms");
        
        return nuevasSelecciones;
    }
    
    public List<Seleccion> crearHorarioConProfesoresFijados(String grupoObjetivo,
                                                           Map<String, String> profesoresFijados) {
        String claveCache = generarClaveCacheFijados(grupoObjetivo, profesoresFijados);
        
        ResultadoCache resultado = cache.get(claveCache);
        if (resultado != null && !estaExpirado(resultado)) {
            System.out.println("✓ Cache hit (profesores fijados) - resultado en <1ms");
            return resultado.selecciones;
        }
        
        long inicio = System.currentTimeMillis();
        ConstructorHorario constructor = new ConstructorHorarioProfesoresFijados(
            repositorio, grupoObjetivo, profesoresFijados
        );
        List<Seleccion> nuevasSelecciones = constructor.construir();
        long tiempo = System.currentTimeMillis() - inicio;
        
        cache.put(claveCache, new ResultadoCache(nuevasSelecciones, LocalDateTime.now()));
        System.out.println("⚠ Cache miss (profesores fijados) - calculado en " + tiempo + "ms");
        
        return nuevasSelecciones;
    }
    
    public void limpiarCache() {
        cache.clear();
        System.out.println("Cache limpiado");
    }
    
    public String obtenerEstadisticasCache() {
        long total = cache.size();
        long validas = cache.values().stream()
            .mapToLong(r -> estaExpirado(r) ? 0 : 1)
            .sum();
        
        return String.format("Cache: %d total, %d válidas", total, validas);
    }
    
    private String generarClaveCache(String grupo, EstrategiaEvaluacion estrategia) {
        return grupo + "|" + estrategia.obtenerNombreEstrategia();
    }
    
    private String generarClaveCacheFijados(String grupo, Map<String, String> profesoresFijados) {
        String profesoresStr = profesoresFijados.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .map(e -> e.getKey() + ":" + e.getValue())
            .collect(Collectors.joining(","));
        
        return grupo + "|FIJADOS|" + profesoresStr;
    }
    
    private boolean estaExpirado(ResultadoCache resultado) {
        return LocalDateTime.now().minusHours(1).isAfter(resultado.timestamp);
    }
    
    private static class ResultadoCache {
        final List<Seleccion> selecciones;
        final LocalDateTime timestamp;
        
        ResultadoCache(List<Seleccion> selecciones, LocalDateTime timestamp) {
            this.selecciones = new ArrayList<>(selecciones);
            this.timestamp = timestamp;
        }
    }
}
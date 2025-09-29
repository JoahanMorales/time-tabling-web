package programacion.horarios.nucleo.optimizado;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import programacion.horarios.nucleo.FranjaHoraria;

public class PoolFranjaHoraria {
    private final Map<String, FranjaHoraria> pool = new ConcurrentHashMap<>();
    
    public FranjaHoraria obtener(String rango) {
        return pool.computeIfAbsent(rango, FranjaHoraria::new);
    }
    
    public FranjaHoraria obtener(int inicio, int fin) {
        String clave = inicio + "-" + fin;
        return pool.computeIfAbsent(clave, k -> new FranjaHoraria(inicio, fin));
    }
}
package programacion.horarios.estrategia;

import java.util.List;
import programacion.horarios.nucleo.Materia;

public class EstrategiaMediaArmonica implements EstrategiaEvaluacion {
    @Override
    public double evaluar(List<Materia> materias) {
        if (materias.isEmpty()) return 0.0;
        
        double suma = materias.stream()
            .mapToDouble(m -> 1.0 / m.obtenerCalificacion())
            .sum();
        
        return materias.size() / suma;
    }
    
    @Override
    public String obtenerNombreEstrategia() {
        return "Media Armónica";
    }
}
package programacion.horarios.estrategia;

import java.util.List;
import programacion.horarios.nucleo.Materia;

public class EstrategiaPromedioMaxMin implements EstrategiaEvaluacion {
    @Override
    public double evaluar(List<Materia> materias) {
        if (materias.isEmpty()) return 0.0;
        
        double min = materias.stream()
            .mapToDouble(Materia::obtenerCalificacion)
            .min()
            .orElse(0.0);
        
        double max = materias.stream()
            .mapToDouble(Materia::obtenerCalificacion)
            .max()
            .orElse(0.0);
        
        return (min + max) / 2.0;
    }
    
    @Override
    public String obtenerNombreEstrategia() {
        return "Promedio Máx-Mín";
    }
}
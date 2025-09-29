package programacion.horarios.estrategia;

import java.util.List;
import programacion.horarios.nucleo.Materia;

public class EstrategiaPromedioSimple implements EstrategiaEvaluacion {
    @Override
    public double evaluar(List<Materia> materias) {
        return materias.stream()
            .mapToDouble(Materia::obtenerCalificacion)
            .average()
            .orElse(0.0);
    }
    
    @Override
    public String obtenerNombreEstrategia() {
        return "Promedio Simple";
    }
}

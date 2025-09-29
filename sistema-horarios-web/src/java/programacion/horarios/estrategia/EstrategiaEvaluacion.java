package programacion.horarios.estrategia;

import java.util.List;
import programacion.horarios.nucleo.Materia;

public interface EstrategiaEvaluacion {
    double evaluar(List<Materia> materias);
    String obtenerNombreEstrategia();
}


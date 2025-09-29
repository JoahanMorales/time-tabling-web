package programacion.horarios.estrategia;

import java.util.List;
import programacion.horarios.nucleo.Materia;

public class EstrategiaPonderadaPorMinutos implements EstrategiaEvaluacion {
    @Override
    public double evaluar(List<Materia> materias) {
        double sumaPonderada = 0;
        int minutostTotales = 0;
        
        for (Materia m : materias) {
            int minutos = m.obtenerMinutosTotales();
            sumaPonderada += m.obtenerCalificacion() * minutos;
            minutostTotales += minutos;
        }
        
        return minutostTotales == 0 ? 0 : sumaPonderada / minutostTotales;
    }
    
    @Override
    public String obtenerNombreEstrategia() {
        return "Ponderado por Minutos";
    }
}



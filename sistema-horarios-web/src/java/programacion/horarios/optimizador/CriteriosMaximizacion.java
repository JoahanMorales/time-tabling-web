package programacion.horarios.optimizador;

public class CriteriosMaximizacion implements CriteriosOptimizacion {
    @Override
    public boolean esMejorQue(double actual, double mejor) {
        return actual > mejor;
    }
    
    @Override
    public double obtenerValorInicialPeor() {
        return Double.NEGATIVE_INFINITY;
    }
}
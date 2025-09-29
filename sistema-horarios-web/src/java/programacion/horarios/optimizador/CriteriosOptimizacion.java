package programacion.horarios.optimizador;

public interface CriteriosOptimizacion {
    boolean esMejorQue(double actual, double mejor);
    double obtenerValorInicialPeor();
}

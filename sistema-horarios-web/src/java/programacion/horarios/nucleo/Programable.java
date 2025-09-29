package programacion.horarios.nucleo;

import java.util.List;
import java.util.Map;
public interface Programable {
    Map<Integer, List<FranjaHoraria>> obtenerHorario();
    boolean tieneConflictoCon(Programable otro);
    int obtenerMinutosTotales();
    String obtenerIdentificador();
}

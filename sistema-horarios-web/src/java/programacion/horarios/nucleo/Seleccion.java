package programacion.horarios.nucleo;

import java.util.Objects;

public record Seleccion(Materia asignada, String grupoOrigen) {
    
    public Seleccion {
        Objects.requireNonNull(asignada, "La materia asignada no puede ser nula");
        Objects.requireNonNull(grupoOrigen, "El grupo origen no puede ser nulo");
    }
    
    public String obtenerDescripcion() {
        return String.format("%s (desde %s) impartida por %s",
            asignada.obtenerNombreMateria(),
            grupoOrigen,
            asignada.obtenerProfesor().obtenerNombreCompleto());
    }
    
    @Override
    public String toString() {
        return obtenerDescripcion();
    }
}
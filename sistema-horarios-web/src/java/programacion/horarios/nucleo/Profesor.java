package programacion.horarios.nucleo;

import java.util.Objects;

public class Profesor {
    private final String nombre;
    private final String apellido;
    private final double calificacion;
    
    public Profesor(String nombre, String apellido, double calificacion) {
        this.nombre = Objects.requireNonNull(nombre);
        this.apellido = Objects.requireNonNull(apellido);
        if (calificacion < 0 || calificacion > 10) {
            throw new IllegalArgumentException("La calificación debe estar entre 0 y 10");
        }
        this.calificacion = calificacion;
    }
    
    public String obtenerNombreCompleto() {
        return nombre + " " + apellido;
    }
    
    public boolean coincideCon(String nombreBuscar) {
        String normalizado = nombreBuscar.toLowerCase().trim();
        return obtenerNombreCompleto().toLowerCase().contains(normalizado) ||
               nombre.toLowerCase().equals(normalizado) ||
               apellido.toLowerCase().equals(normalizado);
    }
    
    // getters
    public String obtenerNombre() { return nombre; }
    public String obtenerApellido() { return apellido; }
    public double obtenerCalificacion() { return calificacion; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Profesor)) return false;
        Profesor p = (Profesor) o;
        return nombre.equals(p.nombre) && apellido.equals(p.apellido);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(nombre, apellido);
    }
}

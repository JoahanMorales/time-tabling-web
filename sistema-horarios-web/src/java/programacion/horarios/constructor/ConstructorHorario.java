package programacion.horarios.constructor;

import java.util.List;
import programacion.horarios.nucleo.Materia;
import programacion.horarios.nucleo.RepositorioMaterias;
import programacion.horarios.nucleo.Seleccion;

public abstract class ConstructorHorario {
    protected final RepositorioMaterias repositorio;
    protected final String nombreGrupoObjetivo;
    
    public ConstructorHorario(RepositorioMaterias repositorio, String nombreGrupoObjetivo) {
        this.repositorio = repositorio;
        this.nombreGrupoObjetivo = nombreGrupoObjetivo;
    }
    
    public abstract List<Seleccion> construir();
    
    protected abstract boolean validarRestricciones(List<Materia> seleccionadas);
    
    protected Materia clonarAGrupo(Materia origen) {
        return origen.clonarConGrupo(nombreGrupoObjetivo);
    }
}
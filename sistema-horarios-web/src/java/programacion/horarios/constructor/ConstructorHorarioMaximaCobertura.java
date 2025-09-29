package programacion.horarios.constructor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import programacion.horarios.estrategia.EstrategiaEvaluacion;
import programacion.horarios.estrategia.EstrategiaPonderadaPorMinutos;
import programacion.horarios.nucleo.Materia;
import programacion.horarios.nucleo.RepositorioMaterias;
import programacion.horarios.nucleo.Seleccion;

public class ConstructorHorarioMaximaCobertura extends ConstructorHorario {
    private final EstrategiaEvaluacion estrategiaEvaluacion;
    
    public ConstructorHorarioMaximaCobertura(RepositorioMaterias repositorio, String nombreGrupoObjetivo) {
        super(repositorio, nombreGrupoObjetivo);
        this.estrategiaEvaluacion = new EstrategiaPonderadaPorMinutos();
    }
    
    public ConstructorHorarioMaximaCobertura(RepositorioMaterias repositorio, String nombreGrupoObjetivo, 
                                      EstrategiaEvaluacion estrategia) {
        super(repositorio, nombreGrupoObjetivo);
        this.estrategiaEvaluacion = estrategia;
    }
    
    @Override
    public List<Seleccion> construir() {
        Set<String> materiasRequeridas = repositorio.obtenerTodosLosNombresMaterias();
        if (materiasRequeridas.isEmpty()) return List.of();
        return construirHorarioParaMaterias(materiasRequeridas);
    }
    
    private List<Seleccion> construirHorarioParaMaterias(Set<String> materiasRequeridas) {
        Map<String, List<Materia>> opciones = new HashMap<>();
        
        for (String materia : materiasRequeridas) {
            List<Materia> opcionesMateria = repositorio.obtenerPorNombreMateria(materia);
            opcionesMateria.sort(Comparator.reverseOrder()); // Por calificación
            opciones.put(materia, opcionesMateria);
        }
        
        List<String> materiasOrdenadas = new ArrayList<>(materiasRequeridas);
        materiasOrdenadas.sort(Comparator.comparingInt(m -> opciones.get(m).size()));
        
        List<Materia> elegidas = new ArrayList<>();
        List<String> gruposOrigen = new ArrayList<>();
        
        if (retroceso(0, materiasOrdenadas, opciones, elegidas, gruposOrigen)) {
            return crearSelecciones(elegidas, gruposOrigen);
        }
        
        return List.of();
    }
    
    protected boolean retroceso(int indice, List<String> materias,
                             Map<String, List<Materia>> opciones,
                             List<Materia> elegidas, List<String> gruposOrigen) {
        if (indice == materias.size()) return true;
        
        String materiaActual = materias.get(indice);
        List<Materia> opcionesMateria = opciones.get(materiaActual);
        
        for (Materia candidata : opcionesMateria) {
            if (!tieneConflicto(candidata, elegidas)) {
                elegidas.add(candidata);
                gruposOrigen.add(candidata.obtenerGrupo());
                
                if (retroceso(indice + 1, materias, opciones, elegidas, gruposOrigen)) {
                    return true;
                }
                
                elegidas.remove(elegidas.size() - 1);
                gruposOrigen.remove(gruposOrigen.size() - 1);
            }
        }
        
        return false;
    }
    
    private boolean tieneConflicto(Materia candidata, List<Materia> elegidas) {
        for (Materia m : elegidas) {
            if (m.tieneConflictoCon(candidata)) {
                return true;
            }
        }
        return false;
    }
    
    protected List<Seleccion> crearSelecciones(List<Materia> elegidas, List<String> gruposOrigen) {
        List<Seleccion> resultado = new ArrayList<>();
        for (int i = 0; i < elegidas.size(); i++) {
            Materia clonada = clonarAGrupo(elegidas.get(i));
            resultado.add(new Seleccion(clonada, gruposOrigen.get(i)));
        }
        return resultado;
    }
    
    @Override
    protected boolean validarRestricciones(List<Materia> seleccionadas) {
        // Validar que no hay conflictos
        for (int i = 0; i < seleccionadas.size(); i++) {
            for (int j = i + 1; j < seleccionadas.size(); j++) {
                if (seleccionadas.get(i).tieneConflictoCon(seleccionadas.get(j))) {
                    return false;
                }
            }
        }
        return true;
    }
}
package programacion.horarios.nucleo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class RepositorioMaterias {
    private final List<Materia> materias;
    private final Map<String, List<Materia>> porGrupo;
    private final Map<String, List<Materia>> porMateria;
    
    public RepositorioMaterias(List<Materia> materias) {
        this.materias = new ArrayList<>(materias);
        this.porGrupo = materias.stream()
            .collect(Collectors.groupingBy(Materia::obtenerGrupo));
        this.porMateria = materias.stream()
            .collect(Collectors.groupingBy(Materia::obtenerNombreMateria));
    }
    
    public List<Materia> obtenerMaterias() {
        return new ArrayList<>(materias);
    }
    
    public List<Materia> obtenerPorGrupo(String grupo) {
        return porGrupo.getOrDefault(grupo, List.of());
    }
    
    public List<Materia> obtenerPorNombreMateria(String nombreMateria) {
        return porMateria.getOrDefault(nombreMateria, List.of());
    }
    
    public Set<String> obtenerTodosLosGrupos() {
        return new HashSet<>(porGrupo.keySet());
    }
    
    public Set<String> obtenerTodosLosNombresMaterias() {
        return new HashSet<>(porMateria.keySet());
    }
    
    public Map<String, Set<String>> calcularCoberturaGrupos() {
        Map<String, Set<String>> cobertura = new HashMap<>();
        for (Materia m : materias) {
            cobertura.computeIfAbsent(m.obtenerGrupo(), k -> new HashSet<>())
                   .add(m.obtenerNombreMateria());
        }
        return cobertura;
    }
    
    public String encontrarGrupoMejorCobertura() {
        Map<String, Set<String>> cobertura = calcularCoberturaGrupos();
        return cobertura.entrySet().stream()
            .max(Map.Entry.comparingByValue(Comparator.comparingInt(Set::size)))
            .map(Map.Entry::getKey)
            .orElse(null);
    }
}
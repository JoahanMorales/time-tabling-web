package programacion.horarios.validador;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import programacion.horarios.nucleo.Materia;

public class ValidadorHorario {
    
    public static class ResultadoValidacion {
        private final boolean valido;
        private final List<String> conflictos;
        
        public ResultadoValidacion(boolean valido, List<String> conflictos) {
            this.valido = valido;
            this.conflictos = conflictos;
        }
        
        public boolean esValido() { return valido; }
        public List<String> obtenerConflictos() { return conflictos; }
    }
    
    public ResultadoValidacion validar(List<Materia> materias) {
        List<String> conflictos = new ArrayList<>();
        
        // Agrupar por grupo
        Map<String, List<Materia>> porGrupo = materias.stream()
            .collect(Collectors.groupingBy(Materia::obtenerGrupo));
        
        // Validar conflictos dentro de cada grupo
        for (var entrada : porGrupo.entrySet()) {
            String grupo = entrada.getKey();
            List<Materia> materiasGrupo = entrada.getValue();
            
            for (int i = 0; i < materiasGrupo.size(); i++) {
                for (int j = i + 1; j < materiasGrupo.size(); j++) {
                    Materia m1 = materiasGrupo.get(i);
                    Materia m2 = materiasGrupo.get(j);
                    
                    if (m1.tieneConflictoCon(m2)) {
                        conflictos.add(String.format("Conflicto en %s: %s ↔ %s",
                            grupo, m1.obtenerNombreMateria(), m2.obtenerNombreMateria()));
                    }
                }
            }
        }
        
        return new ResultadoValidacion(conflictos.isEmpty(), conflictos);
    }
    
    public ResultadoValidacion validarEntreGrupos(Materia m1, Materia m2) {
        List<String> conflictos = new ArrayList<>();
        
        if (m1.tieneConflictoCon(m2)) {
            conflictos.add(String.format("%s (%s) tiene conflicto con %s (%s)",
                m1.obtenerNombreMateria(), m1.obtenerGrupo(),
                m2.obtenerNombreMateria(), m2.obtenerGrupo()));
        }
        
        return new ResultadoValidacion(conflictos.isEmpty(), conflictos);
    }
}
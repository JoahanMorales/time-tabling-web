package programacion.horarios.nucleo.optimizado;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import programacion.horarios.nucleo.FranjaHoraria;
import programacion.horarios.nucleo.Materia;

public class IndiceHorarios {
    // Estructura: Día -> Hora -> Lista de Materias
    private final Map<Integer, Map<Integer, Set<String>>> indiceOcupacion;
    
    public IndiceHorarios() {
        this.indiceOcupacion = new HashMap<>();
        for (int dia = 1; dia <= 7; dia++) {
            indiceOcupacion.put(dia, new HashMap<>());
        }
    }
    
    public void agregarMateria(Materia materia) {
        for (var entrada : materia.obtenerHorario().entrySet()) {
            int dia = entrada.getKey();
            for (FranjaHoraria franja : entrada.getValue()) {
                for (int hora = franja.obtenerHoraInicio() / 100; 
                     hora < franja.obtenerHoraFin() / 100; hora++) {
                    
                    indiceOcupacion.get(dia)
                        .computeIfAbsent(hora, k -> new HashSet<>())
                        .add(materia.obtenerIdentificador());
                }
            }
        }
    }
    
    // O(1) en lugar de O(n) para verificar conflictos
    public boolean tieneConflicto(Materia nueva) {
        for (var entrada : nueva.obtenerHorario().entrySet()) {
            int dia = entrada.getKey();
            for (FranjaHoraria franja : entrada.getValue()) {
                for (int hora = franja.obtenerHoraInicio() / 100;
                     hora < franja.obtenerHoraFin() / 100; hora++) {
                    
                    Set<String> ocupadas = indiceOcupacion.get(dia).get(hora);
                    if (ocupadas != null && !ocupadas.isEmpty()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
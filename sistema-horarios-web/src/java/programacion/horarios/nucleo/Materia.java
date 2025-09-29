package programacion.horarios.nucleo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

public class Materia implements Programable, Comparable<Materia> {
    private final Profesor profesor;
    private final String nombreMateria;
    private final String grupo;
    private final Map<DiaSemana, List<FranjaHoraria>> horario;
    
    public Materia(Profesor profesor, String nombreMateria, String grupo) {
        this.profesor = Objects.requireNonNull(profesor);
        this.nombreMateria = Objects.requireNonNull(nombreMateria);
        this.grupo = Objects.requireNonNull(grupo);
        this.horario = new EnumMap<>(DiaSemana.class);
    }
    
    public Materia agregarBloqueHorario(DiaSemana dia, FranjaHoraria franja) {
        horario.computeIfAbsent(dia, k -> new ArrayList<>()).add(franja);
        horario.get(dia).sort(Comparator.comparingInt(FranjaHoraria::obtenerHoraInicio));
        horario.get(dia).sort(Comparator.comparingInt(FranjaHoraria::obtenerHoraInicio));
        return this;
    }
    
    public Materia agregarBloqueHorario(String digitosDias, String... rangos) {
        Set<DiaSemana> dias = procesarDias(digitosDias);
        for (DiaSemana dia : dias) {
            for (String rango : rangos) {
                agregarBloqueHorario(dia, new FranjaHoraria(rango));
            }
        }
        return this;
    }
    
    private Set<DiaSemana> procesarDias(String digitos) {
        Set<DiaSemana> dias = new HashSet<>();
        for (char c : digitos.toCharArray()) {
            if (Character.isDigit(c)) {
                int codigo = c - '0';
                if (codigo >= 1 && codigo <= 7) {
                    dias.add(DiaSemana.desdeCodigo(codigo));
                }
            }
        }
        return dias;
    }
    
    @Override
    public Map<Integer, List<FranjaHoraria>> obtenerHorario() {
        Map<Integer, List<FranjaHoraria>> resultado = new HashMap<>();
        for (var entrada : horario.entrySet()) {
            resultado.put(entrada.getKey().obtenerCodigo(), new ArrayList<>(entrada.getValue()));
        }
        return resultado;
    }
    
    @Override
    public boolean tieneConflictoCon(Programable otro) {
        if (!(otro instanceof Materia)) return false;
        Materia otraMateria = (Materia) otro;
        
        if (!this.grupo.equals(otraMateria.grupo)) {
            return verificarSuperposicionTiempo(otraMateria);
        }
        
        return verificarSuperposicionTiempo(otraMateria);
    }
    
    private boolean verificarSuperposicionTiempo(Materia otra) {
        for (DiaSemana dia : horario.keySet()) {
            List<FranjaHoraria> misFranjas = horario.get(dia);
            List<FranjaHoraria> otrasFranjas = otra.horario.get(dia);
            
            if (otrasFranjas != null) {
                for (FranjaHoraria miFranja : misFranjas) {
                    for (FranjaHoraria otraFranja : otrasFranjas) {
                        if (miFranja.seSuperpone(otraFranja)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
    
    @Override
    public int obtenerMinutosTotales() {
        return horario.values().stream()
            .flatMap(List::stream)
            .mapToInt(FranjaHoraria::obtenerDuracionMinutos)
            .sum();
    }
    
    @Override
    public String obtenerIdentificador() {
        return nombreMateria + "_" + grupo + "_" + profesor.obtenerNombreCompleto();
    }
    
    public Materia clonarConGrupo(String nuevoGrupo) {
        Materia clon = new Materia(profesor, nombreMateria, nuevoGrupo);
        for (var entrada : horario.entrySet()) {
            for (FranjaHoraria franja : entrada.getValue()) {
                clon.agregarBloqueHorario(entrada.getKey(), franja);
            }
        }
        return clon;
    }
    
    @Override
    public int compareTo(Materia otra) {
        return Double.compare(otra.profesor.obtenerCalificacion(), 
                             this.profesor.obtenerCalificacion());
    }
    
    // getters
    public Profesor obtenerProfesor() { return profesor; }
    public String obtenerNombreMateria() { return nombreMateria; }
    public String obtenerGrupo() { return grupo; }
    public double obtenerCalificacion() { return profesor.obtenerCalificacion(); }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(nombreMateria).append(" [").append(grupo).append("] | ")
          .append("Prof: ").append(profesor.obtenerNombreCompleto()).append(" | ")
          .append("Calif: ").append(profesor.obtenerCalificacion()).append(" | ");
        
        for (var entrada : horario.entrySet()) {
            sb.append(entrada.getKey().obtenerAbreviacion()).append(": ");
            for (FranjaHoraria franja : entrada.getValue()) {
                sb.append("[").append(franja.formatear()).append("] ");
            }
        }
        return sb.toString();
    }
}


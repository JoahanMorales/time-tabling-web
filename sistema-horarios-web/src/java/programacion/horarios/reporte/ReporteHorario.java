
package programacion.horarios.reporte;

import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import programacion.horarios.estrategia.EstrategiaEvaluacion;
import programacion.horarios.nucleo.DiaSemana;
import programacion.horarios.nucleo.FranjaHoraria;
import programacion.horarios.nucleo.Materia;

public class ReporteHorario {
    private final EstrategiaEvaluacion estrategia;
    
    public ReporteHorario(EstrategiaEvaluacion estrategia) {
        this.estrategia = estrategia;
    }
    
    public void imprimirResumenGrupo(String grupo, List<Materia> materias) {
        System.out.println("\n══════ " + grupo + " ══════");
        
        materias.stream()
            .sorted(Comparator.reverseOrder())
            .forEach(m -> System.out.println("  • " + m));
        
        double puntuacion = estrategia.evaluar(materias);
        System.out.printf("  Puntuación (%s): %.2f%n", estrategia.obtenerNombreEstrategia(), puntuacion);
    }
    
    public void imprimirMatrizHorario(List<Materia> materias) {
        System.out.println("\n══════ MATRIZ DE HORARIO ══════");
        
        // Crear matriz de horarios
        Map<DiaSemana, Map<Integer, String>> matriz = new EnumMap<>(DiaSemana.class);
        
        for (Materia m : materias) {
            for (var entrada : m.obtenerHorario().entrySet()) {
                DiaSemana dia = DiaSemana.desdeCodigo(entrada.getKey());
                for (FranjaHoraria franja : entrada.getValue()) {
                    int hora = franja.obtenerHoraInicio() / 100;
                    matriz.computeIfAbsent(dia, k -> new TreeMap<>())
                         .put(hora, m.obtenerNombreMateria());
                }
            }
        }
        
        // Imprimir matriz
        System.out.print("Hora  ");
        for (DiaSemana dia : DiaSemana.values()) {
            if (dia.obtenerCodigo() <= 5) { // Solo días laborables
                System.out.printf("%-15s", dia.obtenerAbreviacion());
            }
        }
        System.out.println();
        
        for (int hora = 7; hora <= 20; hora++) {
            System.out.printf("%02d:00 ", hora);
            for (DiaSemana dia : DiaSemana.values()) {
                if (dia.obtenerCodigo() <= 5) {
                    String materia = matriz.getOrDefault(dia, Map.of()).get(hora);
                    System.out.printf("%-15s", materia != null ? materia.substring(0, 
                        Math.min(materia.length(), 13)) : "");
                }
            }
            System.out.println();
        }
    }
}
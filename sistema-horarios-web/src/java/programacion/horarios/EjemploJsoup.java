package programacion.horarios;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import programacion.horarios.nucleo.FranjaHoraria;
import programacion.horarios.nucleo.Materia;

public class EjemploJsoup {
    public static void main(String[] args) {
        try {
            String htmlFilePath = "C:/Users/chump/OneDrive/Escritorio/Escritorio/Time-tabling-java/time-tabling/src/programacion/horarios/Horarios de clase.html";
            String html = new String(Files.readAllBytes(Paths.get(htmlFilePath)));

            List<Materia> materias = HorarioParser.parseHorarios(html);

            for (Materia m : materias) {
                System.out.println("materias.add(crearMateria(" +
                        m.obtenerCalificacion() + ", \"" +
                        m.obtenerProfesor().obtenerNombre() + "\", \"" +
                        m.obtenerProfesor().obtenerApellido() + "\",");
                System.out.println("            \"" + m.obtenerNombreMateria() + "\", \"" +
                        m.obtenerGrupo() + "\",");

                List<String> bloques = new ArrayList<>();
                for (Map.Entry<Integer, List<FranjaHoraria>> entry : m.obtenerHorario().entrySet()) {
                    Integer dia = entry.getKey(); // 1..7
                    for (FranjaHoraria fr : entry.getValue()) {
                        String rangoSinDosPuntos = fr.formatear().replace(":", "");
                        bloques.add("            new Bloque(\"" + dia + "\", \"" + rangoSinDosPuntos + "\")");
                    }
                }
                if (!bloques.isEmpty()) {
                    System.out.println(String.join(",\n", bloques));
                }
                System.out.println("));\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

package programacion.horarios;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import programacion.horarios.nucleo.Materia;
import programacion.horarios.nucleo.Profesor;
import programacion.horarios.nucleo.DiaSemana;
import programacion.horarios.nucleo.FranjaHoraria;

public class HorarioParser {

    public static List<Materia> parseHorarios(String html) {
        List<Materia> materias = new ArrayList<>();
        Document doc = Jsoup.parse(html);

        Elements rows = doc.select("#ctl00_mainCopy_dbgHorarios tbody tr");
        if (rows.isEmpty()) {
            System.out.println("No se encontraron filas en la tabla.");
        }

        for (Element row : rows) {
            Elements cols = row.select("td");
            if (cols.size() < 11) {
                // cabecera o fila incompleta
                continue;
            }

            String grupo = cols.get(0).text().trim();
            String materia = cols.get(1).text().trim();
            String profesorTxt = cols.get(2).text().trim();

            String lunes = cols.get(5).text().trim();
            String martes = cols.get(6).text().trim();
            String miercoles = cols.get(7).text().trim();
            String jueves = cols.get(8).text().trim();
            String viernes = cols.get(9).text().trim();
            String sabado = cols.get(10).text().trim();

            String[] nombreApellido = partirNombreApellidos(profesorTxt);
            String nombreProf = nombreApellido[0];
            String apellidoProf = nombreApellido[1];

            Profesor profesor = new Profesor(nombreProf, apellidoProf, 0.0);
            Materia m = new Materia(profesor, materia, grupo);

            if (tieneHora(lunes)) {
                m.agregarBloqueHorario(DiaSemana.LUNES, new FranjaHoraria(convertirRango(lunes)));
            }
            if (tieneHora(martes)) {
                m.agregarBloqueHorario(DiaSemana.MARTES, new FranjaHoraria(convertirRango(martes)));
            }
            if (tieneHora(miercoles)) {
                m.agregarBloqueHorario(DiaSemana.MIERCOLES, new FranjaHoraria(convertirRango(miercoles)));
            }
            if (tieneHora(jueves)) {
                m.agregarBloqueHorario(DiaSemana.JUEVES, new FranjaHoraria(convertirRango(jueves)));
            }
            if (tieneHora(viernes)) {
                m.agregarBloqueHorario(DiaSemana.VIERNES, new FranjaHoraria(convertirRango(viernes)));
            }
            if (tieneHora(sabado)) {
                m.agregarBloqueHorario(DiaSemana.SABADO, new FranjaHoraria(convertirRango(sabado)));
            }

            materias.add(m);
        }

        return materias;
    }

    private static String convertirRango(String rango) {
        String limpio = rango.replace("\u00A0", " ") 
                             .trim()
                             .replace(" ", "");
        return limpio.replace(":", "");
    }

    private static boolean tieneHora(String celda) {
        if (celda == null) return false;
        String t = celda.replace("\u00A0", " ").trim();
        return !t.isEmpty() && !t.equals("&nbsp;");
    }

    private static String[] partirNombreApellidos(String fullNameRaw) {
        if (fullNameRaw == null || fullNameRaw.isBlank()) {
            return new String[] {"", ""};
        }

        String s = fullNameRaw.replace(".", " ").replaceAll("\\s+", " ").trim().toUpperCase();

        Set<String> titulos = new HashSet<>(Arrays.asList(
            "DR", "DRA", "ING", "LIC", "MTRO", "MTRA", "MAESTRO",
            "MSC", "PH", "D", "M", "EN", "C", "E" // para secuencias tipo "M EN C"
        ));

        List<String> toks = new ArrayList<>(Arrays.asList(s.split(" ")));

        // Quitar títulos iniciales (incluye secuencia M EN C, etc.)
        while (!toks.isEmpty() && titulos.contains(toks.get(0))) {
            toks.remove(0);
        }

        if (toks.isEmpty()) return new String[] {"", ""};

        if (toks.size() >= 4) {
            String apellidos = toks.get(toks.size()-2) + " " + toks.get(toks.size()-1);
            String nombres = String.join(" ", toks.subList(0, toks.size()-2));
            return new String[] {toTitle(nombres), toTitle(apellidos)};
        } else if (toks.size() == 3) {
            String apellidos = toks.get(1) + " " + toks.get(2);
            String nombres = toks.get(0);
            return new String[] {toTitle(nombres), toTitle(apellidos)};
        } else if (toks.size() == 2) {
            return new String[] {toTitle(toks.get(0)), toTitle(toks.get(1))};
        } else {
            return new String[] {toTitle(toks.get(0)), ""};
        }
    }

    private static String toTitle(String up) {
        String[] p = up.split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < p.length; i++) {
            String w = p[i].toLowerCase();
            if (!w.isEmpty()) {
                sb.append(Character.toUpperCase(w.charAt(0)))
                  .append(w.substring(1));
                if (i < p.length - 1) sb.append(' ');
            }
        }
        return sb.toString();
    }
}

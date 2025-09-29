package time.tabling;

import java.util.*;
import java.util.stream.Collectors;

public class TimeTabling {
    public static void main(String[] args) {
        List<Subjects> all = DataFactory.buildFromTable();

        List<String> conflicts = ScheduleUtils.validateNoClashesPerGroup(all);
        if (conflicts.isEmpty()) {
            System.out.println("Sin choques dentro de cada grupo.");
        } else {
            System.out.println("️Choques encontrados:");
            conflicts.forEach(System.out::println);
        }

        Map<String, List<Subjects>> byGroup = ScheduleUtils.groupByGroup(all);
        byGroup.forEach(ScheduleUtils::printGroupSummary);

        System.out.println("\nTop profes por grupo (desc):");
        byGroup.forEach((g, L) -> {
            System.out.println(">> " + g);
            L.stream()
             .sorted(Comparator.comparingDouble(Subjects::getQualification).reversed())
             .forEach(s -> System.out.printf("  %.2f — %s %s — %s%n",
                        s.getQualification(), s.getName(), s.getLastname(), s.getSubject()));
        });

        System.out.println("\nChoque INTER-grupos (ejemplo manual):");
        Subjects a = all.get(0);  // ALGORITMOS 
        Subjects b = all.get(7);  // ETICA 
        boolean inter = ScheduleUtils.overlapsAnyDay(a, b);
        System.out.println(a.getSubject() + " (" + a.getGroup() + ") vs " +
                           b.getSubject() + " (" + b.getGroup() + ") => " +
                           (inter ? "CHOQUE" : "ok"));

        // ====== A partir de aquí: construir el nuevo grupo ======
        List<Subjects> pool = DataFactory.buildFromTable();

        List<String> conflicts1 = ScheduleUtils.validateNoClashesPerGroup(pool);
        System.out.println(conflicts1.isEmpty() ? "Sin choques intra-grupo en el pool."
                                               : "Choques: " + conflicts1);

        String NEW_GROUP = "2AMX";

        // Devuelve List<Selection> (Subjects asignado + grupo original del profe)
        List<Selection> nuevo = Scheduler.buildGroupFromMaxCoverageWithSource(pool, NEW_GROUP);

        System.out.println("\n== Grupo generado: " + NEW_GROUP + " ==");
        // Impresión bonita: materia nueva + grupo original del profe
        nuevo.forEach(sel -> {
            Subjects s = sel.assigned();       // clon al grupo nuevo
            String src = sel.sourceGroup();    // grupo original del profesor
            System.out.printf("%s [%s] | Prof: %s %s | Calf: %.2f | Origen profe: %s%n",
                    s.getSubject(), s.getGroup(), s.getName(), s.getLastname(),
                    s.getQualification(), src);
        });

        // >>> DESEMPAQUETAR para puntajes (List<Subjects>):
        List<Subjects> nuevoSubjects = nuevo.stream()
                                            .map(Selection::assigned)
                                            .collect(Collectors.toList());

        System.out.printf("\nScore simple: %.2f | Ponderado por minutos: %.2f%n",
                ScheduleUtils.groupScoreMean(nuevoSubjects),
                ScheduleUtils.groupScoreWeightedByMinutes(nuevoSubjects));
        
        Map<String, String> pinned = Map.of(
            "ANÁLISIS Y DISEÑO DE ALGORITMOS", "Sandra Diaz Santiago"
        );


        List<Selection> nuevo2 = Scheduler.buildGroupWithPinnedTeachers(pool, "Grupo cabron", pinned);

        System.out.println("\n== Grupo generado POR PIN =="); 
        nuevo2.forEach(System.out::println);
        
        List<Subjects> nuevoSubjects2 = nuevo2.stream()
                                            .map(Selection::assigned)
                                            .collect(Collectors.toList());

        System.out.printf("\nScore simple: %.2f | Ponderado por minutos: %.2f%n",
                ScheduleUtils.groupScoreMean(nuevoSubjects2),
                ScheduleUtils.groupScoreWeightedByMinutes(nuevoSubjects2));

    }
}

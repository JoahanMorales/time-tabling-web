package time.tabling;

import java.util.*;
import java.util.stream.Collectors;

public class ScheduleUtils {

    public static Map<String, List<Subjects>> groupByGroup(List<Subjects> all) {
        return all.stream().collect(Collectors.groupingBy(Subjects::getGroup));
    }

    public static List<String> validateNoClashesPerGroup(List<Subjects> all) {
        Map<String, List<Subjects>> byGroup = groupByGroup(all);
        List<String> conflicts = new ArrayList<>();
        for (var e : byGroup.entrySet()) {
            String g = e.getKey();
            List<Subjects> L = e.getValue();
            for (int i=0;i<L.size();i++) for (int j=i+1;j<L.size();j++) {
                if (L.get(i).clashesWith(L.get(j))) {
                    conflicts.add("Choque en " + g + ": " + L.get(i).getSubject() + " ↔ " + L.get(j).getSubject());
                }
            }
        }
        return conflicts;
    }

    public static boolean overlapsAnyDay(Subjects a, Subjects b) {
        for (int day : a.getSchedule().keySet()) {
            List<int[]> A = a.getSchedule().get(day);
            List<int[]> B = b.getSchedule().get(day);
            if (A == null || B == null) continue;
            for (int[] x : A) for (int[] y : B)
                if (x[0] < y[1] && y[0] < x[1]) return true;
        }
        return false;
    }

    public static int totalMinutes(Subjects s) {
        int min = 0;
        for (var entry : s.getSchedule().entrySet()) {
            for (int[] r : entry.getValue()) {
                int startMin = (r[0]/100)*60 + (r[0]%100);
                int endMin   = (r[1]/100)*60 + (r[1]%100);
                min += (endMin - startMin);
            }
        }
        return min;
    }

    public static double groupScoreMean(List<Subjects> L) {
        return L.stream().mapToDouble(Subjects::getQualification).average().orElse(0);
    }

    public static double groupScoreWeightedByMinutes(List<Subjects> L) {
        double num = 0; int den = 0;
        for (Subjects s : L) { int m = totalMinutes(s); num += s.getQualification()*m; den += m; }
        return den==0 ? 0 : num/den;
    }

    public static void printGroupSummary(String group, List<Subjects> L) {
        System.out.println("\n== " + group + " ==");
        for (Subjects s : L) System.out.println("• " + s);
        System.out.printf("Promedio simple: %.2f | Ponderado por minutos: %.2f%n",
                groupScoreMean(L), groupScoreWeightedByMinutes(L));
    }
}

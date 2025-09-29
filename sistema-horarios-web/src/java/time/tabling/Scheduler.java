package time.tabling;

import java.util.*;
import java.util.stream.Collectors;
import static time.tabling.ScheduleUtils.overlapsAnyDay;

public class Scheduler {

    public static List<Selection> buildGroupFromMaxCoverageWithSource(List<Subjects> pool, String newGroupName) {
        if (pool == null || pool.isEmpty()) return List.of();

        Map<String, Set<String>> coverage = computeGroupCoverage(pool);
        String bestGroup = pickBestGroup(coverage, pool);
        if (bestGroup == null) return List.of();

        Set<String> requiredSubjects = coverage.getOrDefault(bestGroup, Set.of());
        if (requiredSubjects.isEmpty()) return List.of();

        Map<String, List<Subjects>> optionsBySubject = pool.stream()
                .filter(s -> requiredSubjects.contains(s.getSubject()))
                .collect(Collectors.groupingBy(Subjects::getSubject));
        for (List<Subjects> lst : optionsBySubject.values()) {
            lst.sort(Comparator.comparingDouble(Subjects::getQualification).reversed());
        }

        List<String> subjectsOrdered = new ArrayList<>(requiredSubjects);
        subjectsOrdered.sort(Comparator.comparingInt(s -> optionsBySubject.getOrDefault(s, List.of()).size()));

        List<Subjects> chosen = new ArrayList<>();
        List<String> chosenSourceGroups = new ArrayList<>();
        boolean ok = chooseWithoutOverlap(0, subjectsOrdered, optionsBySubject, chosen, chosenSourceGroups);

        if (!ok) return List.of();

        List<Selection> result = new ArrayList<>(chosen.size());
        for (int i = 0; i < chosen.size(); i++) {
            Subjects src = chosen.get(i);
            String srcGroup = chosenSourceGroups.get(i);
            result.add(new Selection(cloneWithGroup(src, newGroupName), srcGroup));
        }
        return result;
    }

    /** Versión que devuelve solo Subjects. */
    public static List<Subjects> buildGroupSubjectsOnly(List<Subjects> pool, String newGroupName) {
        List<Selection> full = buildGroupFromMaxCoverageWithSource(pool, newGroupName);
        List<Subjects> subjects = new ArrayList<>(full.size());
        for (Selection sel : full) subjects.add(sel.assigned());
        return subjects;
    }

    /** Igual que la anterior, pero minimiza el score ponderado por minutos. Empates: mayor calificación promedio. */
    public static List<Selection> buildGroupMinWeightedByMinutes(List<Subjects> pool, String newGroupName) {
        if (pool == null || pool.isEmpty()) return List.of();

        Map<String, Set<String>> coverage = computeGroupCoverage(pool);
        String bestGroup = pickBestGroup(coverage, pool);
        if (bestGroup == null) return List.of();

        Set<String> requiredSubjects = coverage.getOrDefault(bestGroup, Set.of());
        if (requiredSubjects.isEmpty()) return List.of();

        Map<String, List<Subjects>> optionsBySubject = pool.stream()
                .filter(s -> requiredSubjects.contains(s.getSubject()))
                .collect(Collectors.groupingBy(Subjects::getSubject));
        for (List<Subjects> lst : optionsBySubject.values()) {
            lst.sort(Comparator.comparingDouble(Subjects::getQualification).reversed());
        }

        List<String> subjectsOrdered = new ArrayList<>(requiredSubjects);
        subjectsOrdered.sort(Comparator.comparingInt(s -> optionsBySubject.getOrDefault(s, List.of()).size()));

        List<Subjects> bestChosen = new ArrayList<>();
        List<String>  bestSrcGrp  = new ArrayList<>();
        double[] bestScore = { Double.POSITIVE_INFINITY };
        double[] bestMeanQ = { -1.0 };

        dfsMinWeighted(
                0, subjectsOrdered, optionsBySubject,
                new ArrayList<>(), new ArrayList<>(),
                bestChosen, bestSrcGrp, bestScore, bestMeanQ
        );

        if (bestChosen.isEmpty()) return List.of();

        List<Selection> out = new ArrayList<>(bestChosen.size());
        for (int i = 0; i < bestChosen.size(); i++) {
            Subjects src = bestChosen.get(i);
            String srcGroup = bestSrcGrp.get(i);
            out.add(new Selection(cloneWithGroup(src, newGroupName), srcGroup));
        }
        return out;
    }

    private static boolean chooseWithoutOverlap(
            int idx,
            List<String> subjectsOrdered,
            Map<String, List<Subjects>> optionsBySubject,
            List<Subjects> chosen,
            List<String> chosenSourceGroups
    ) {
        if (idx == subjectsOrdered.size()) return true;

        String subj = subjectsOrdered.get(idx);
        List<Subjects> options = optionsBySubject.getOrDefault(subj, List.of());
        if (options.isEmpty()) return false;

        for (Subjects cand : options) {
            boolean clash = false;
            for (Subjects already : chosen) {
                if (overlapsAnyDay(already, cand)) {
                    clash = true; break;
                }
            }
            if (clash) continue;

            chosen.add(cand);
            chosenSourceGroups.add(cand.getGroup());

            if (chooseWithoutOverlap(idx + 1, subjectsOrdered, optionsBySubject, chosen, chosenSourceGroups)) {
                return true;
            }

            chosen.remove(chosen.size() - 1);
            chosenSourceGroups.remove(chosenSourceGroups.size() - 1);
        }
        return false;
    }

    private static void dfsMinWeighted(
            int idx,
            List<String> subjectsOrdered,
            Map<String, List<Subjects>> optionsBySubject,
            List<Subjects> chosen,
            List<String> chosenSrcGroups,
            List<Subjects> bestChosen,
            List<String> bestSrcGrp,
            double[] bestScore,
            double[] bestMeanQ
    ) {
        if (idx == subjectsOrdered.size()) {
            double score = ScheduleUtils.groupScoreWeightedByMinutes(chosen);
            double meanQ = chosen.stream().mapToDouble(Subjects::getQualification).average().orElse(0.0);

            if (score < bestScore[0] || (Double.compare(score, bestScore[0]) == 0 && meanQ > bestMeanQ[0])) {
                bestScore[0] = score;
                bestMeanQ[0] = meanQ;
                bestChosen.clear();     bestChosen.addAll(chosen);
                bestSrcGrp.clear();     bestSrcGrp.addAll(chosenSrcGroups);
            }
            return;
        }

        double partial = ScheduleUtils.groupScoreWeightedByMinutes(chosen);
        if (partial > bestScore[0]) return;

        String subj = subjectsOrdered.get(idx);
        List<Subjects> options = optionsBySubject.getOrDefault(subj, List.of());
        if (options.isEmpty()) return;

        for (Subjects cand : options) {
            boolean clash = false;
            for (Subjects already : chosen) {
                if (overlapsAnyDay(already, cand)) {
                    clash = true; break;
                }
            }
            if (clash) continue;

            chosen.add(cand);
            chosenSrcGroups.add(cand.getGroup());

            dfsMinWeighted(idx + 1, subjectsOrdered, optionsBySubject,
                           chosen, chosenSrcGroups, bestChosen, bestSrcGrp, bestScore, bestMeanQ);

            chosen.remove(chosen.size() - 1);
            chosenSrcGroups.remove(chosenSrcGroups.size() - 1);
        }
    }
    private static Map<String, Set<String>> computeGroupCoverage(List<Subjects> pool) {
        Map<String, Set<String>> map = new HashMap<>();
        for (Subjects s : pool) {
            String g = s.getGroup();
            if (g == null) continue;
            map.computeIfAbsent(g, k -> new HashSet<>()).add(s.getSubject());
        }
        return map;
    }

    private static String pickBestGroup(Map<String, Set<String>> coverage, List<Subjects> pool) {
        if (coverage.isEmpty()) return null;

        Map<String, Integer> sectionsPerGroup = new HashMap<>();
        for (Subjects s : pool) {
            String g = s.getGroup();
            if (g == null) continue;
            sectionsPerGroup.merge(g, 1, Integer::sum);
        }

        return coverage.entrySet().stream()
                .max((a, b) -> {
                    int cmp = Integer.compare(a.getValue().size(), b.getValue().size());
                    if (cmp != 0) return cmp;
                    int sa = sectionsPerGroup.getOrDefault(a.getKey(), 0);
                    int sb = sectionsPerGroup.getOrDefault(b.getKey(), 0);
                    return Integer.compare(sa, sb);
                })
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    private static Subjects cloneWithGroup(Subjects src, String newGroup) {
        Subjects copy = new Subjects(
                src.getQualification(),
                src.getName(),
                src.getLastname(),
                src.getSubject(),
                newGroup
        );
        var sched = src.getSchedule();
        List<Integer> days = new ArrayList<>(sched.keySet());
        Collections.sort(days);
        for (int d : days) {
            for (int[] r : sched.get(d)) {
                String range = String.format("%04d-%04d", r[0], r[1]);
                copy.addBlock(String.valueOf(d), range);
            }
        }
        return copy;
    }

    public static List<Selection> buildGroupWithPinnedTeachers(
            List<Subjects> pool,
            String newGroupName,
            Map<String, String> pinned
    ) {
        if (pool == null || pool.isEmpty()) return List.of();
        final Map<String, String> pinnedFinal = (pinned == null ? Map.of() : pinned);

        Map<String, Set<String>> coverage = computeGroupCoverage(pool);
        String bestGroup = pickBestGroup(coverage, pool);
        if (bestGroup == null) return List.of();

        Set<String> requiredSubjects = coverage.getOrDefault(bestGroup, Set.of());
        if (requiredSubjects.isEmpty()) return List.of();

        Map<String, List<Subjects>> optionsBySubject = pool.stream()
                .filter(s -> requiredSubjects.contains(s.getSubject()))
                .collect(Collectors.groupingBy(Subjects::getSubject));
        for (List<Subjects> lst : optionsBySubject.values()) {
            lst.sort(Comparator.comparingDouble(Subjects::getQualification).reversed());
        }

        List<String> freeSubjects = new ArrayList<>(requiredSubjects.stream()
                .filter(s -> !pinnedFinal.containsKey(s))
                .collect(Collectors.toList()));

        List<String> pinnedSubjects = new ArrayList<>(requiredSubjects.stream()
                .filter(pinnedFinal::containsKey)
                .collect(Collectors.toList()));

        List<Subjects> chosen = new ArrayList<>();
        List<String> chosenSrcGroups = new ArrayList<>();

        for (String subj : pinnedSubjects) {
            String requiredTeacher = pinnedFinal.get(subj);
            List<Subjects> opts = optionsBySubject.getOrDefault(subj, List.of());

            Subjects match = opts.stream()
                    .filter(o -> (o.getName() + " " + o.getLastname()).equalsIgnoreCase(requiredTeacher))
                    .findFirst().orElse(null);

            if (match == null) {
                System.out.println("⚠️ No se encontró a " + requiredTeacher + " en " + subj);
                return List.of();
            }

            boolean clash = false;
            for (Subjects already : chosen) {
                if (ScheduleUtils.overlapsAnyDay(already, match)) { clash = true; break; }
            }
            if (clash) {
                System.out.println("⚠️ Choque de horario con " + requiredTeacher + " en " + subj);
                return List.of();
            }

            chosen.add(match);
            chosenSrcGroups.add(match.getGroup());
        }

        freeSubjects.sort(Comparator.comparingInt(s -> optionsBySubject.getOrDefault(s, List.of()).size()));

        boolean ok = chooseWithoutOverlap(0, freeSubjects, optionsBySubject, chosen, chosenSrcGroups);
        if (!ok) {
            System.out.println("⚠️ No se pudo completar el horario respetando los pinned.");
            return List.of();
        }

        List<Selection> result = new ArrayList<>(chosen.size());
        for (int i = 0; i < chosen.size(); i++) {
            Subjects src = chosen.get(i);
            String srcGroup = chosenSrcGroups.get(i);
            result.add(new Selection(cloneWithGroup(src, newGroupName), srcGroup));
        }
        return result;
    }

   private static boolean dfsAllSubjectsFirstFeasible(
           int idx,
           List<String> subjectsOrdered,
           Map<String, List<Subjects>> optionsBySubject,
           List<Subjects> chosen,
           List<String> chosenSrcGroups
   ) {
       if (idx == subjectsOrdered.size()) return true;

       String subj = subjectsOrdered.get(idx);
       List<Subjects> options = optionsBySubject.getOrDefault(subj, List.of());
       if (options.isEmpty()) return false;

       for (Subjects cand : options) {
           boolean clash = false;
           for (Subjects a : chosen) {
               if (ScheduleUtils.overlapsAnyDay(a, cand)) { clash = true; break; }
           }
           if (clash) continue;

           chosen.add(cand);
           chosenSrcGroups.add(cand.getGroup());

           if (dfsAllSubjectsFirstFeasible(idx + 1, subjectsOrdered, optionsBySubject, chosen, chosenSrcGroups)) {
               return true;
           }

           chosen.remove(chosen.size() - 1);
           chosenSrcGroups.remove(chosenSrcGroups.size() - 1);
       }
       return false;
   }

   private static void dfsAllSubjectsMinWeighted(
           int idx,
           List<String> subjectsOrdered,
           Map<String, List<Subjects>> optionsBySubject,
           List<Subjects> chosen,
           List<String> chosenSrcGroups,
           List<Subjects> bestChosen,
           List<String> bestSrcGrp,
           double[] bestScore,
           double[] bestMeanQ
   ) {
       if (idx == subjectsOrdered.size()) {
           double score = ScheduleUtils.groupScoreWeightedByMinutes(chosen);
           double meanQ = chosen.stream().mapToDouble(Subjects::getQualification).average().orElse(0.0);

           if (score < bestScore[0] || (Double.compare(score, bestScore[0]) == 0 && meanQ > bestMeanQ[0])) {
               bestScore[0] = score;
               bestMeanQ[0] = meanQ;
               bestChosen.clear();  bestChosen.addAll(chosen);
               bestSrcGrp.clear();  bestSrcGrp.addAll(chosenSrcGroups);
           }
           return;
       }

       double partial = ScheduleUtils.groupScoreWeightedByMinutes(chosen);
       if (partial > bestScore[0]) return;

       String subj = subjectsOrdered.get(idx);
       List<Subjects> options = optionsBySubject.getOrDefault(subj, List.of());
       if (options.isEmpty()) return;

       for (Subjects cand : options) {
           boolean clash = false;
           for (Subjects a : chosen) {
               if (ScheduleUtils.overlapsAnyDay(a, cand)) { clash = true; break; }
           }
           if (clash) continue;

           chosen.add(cand);
           chosenSrcGroups.add(cand.getGroup());

           dfsAllSubjectsMinWeighted(idx + 1, subjectsOrdered, optionsBySubject,
                   chosen, chosenSrcGroups, bestChosen, bestSrcGrp, bestScore, bestMeanQ);

           chosen.remove(chosen.size() - 1);
           chosenSrcGroups.remove(chosenSrcGroups.size() - 1);
       }
   }

   private static String normalizeName(String s) {
       return s == null ? "" : s.trim().toLowerCase(java.util.Locale.ROOT).replaceAll("\\s+", " ");
   }

   private static boolean matchesProfessor(Subjects s, String profKeyNormalized) {
       String full = normalizeName(s.getName() + " " + s.getLastname());
       if (full.equals(profKeyNormalized)) return true;
       String nameOnly = normalizeName(s.getName());
       String lastOnly = normalizeName(s.getLastname());
       return nameOnly.equals(profKeyNormalized) || lastOnly.equals(profKeyNormalized);
   }

}

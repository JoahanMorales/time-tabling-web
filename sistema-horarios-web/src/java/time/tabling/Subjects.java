package time.tabling;

import java.util.*;

public class Subjects {
    private double qualification;
    private String name;
    private String lastname;
    private String subject;
    private String group;

    private final Map<Integer, List<int[]>> schedule = new HashMap<>();

    public static final class Block {
        public final String daysDigits;   
        public final String[] ranges; 
        public Block(String daysDigits, String... ranges) {
            this.daysDigits = daysDigits;
            this.ranges = ranges;
        }
    }

    public Subjects(double qualification, String name, String lastname, String subject, String group) {
        this.qualification = qualification;
        this.name = name;
        this.lastname = lastname;
        this.subject = subject;
        this.group = group;
    }

    public static Subjects fromBlocks(double q, String name, String last, String subj, String group, Block... blocks) {
        Subjects s = new Subjects(q, name, last, subj, group);
        for (Block b : blocks) s.addBlock(b.daysDigits, b.ranges);
        return s;
    }

    public Subjects addBlock(String daysDigits, String... ranges) {
        Set<Integer> days = parseDaysDigits(daysDigits);
        for (int d : days) {
            List<int[]> list = schedule.computeIfAbsent(d, k -> new ArrayList<>());
            for (String r : ranges) list.add(parseRangeString(r));
            list.sort(Comparator.comparingInt(a -> a[0]));
        }
        return this;
    }

    public String getGroup() { return group; }
    public String getSubject() { return subject; }
    public String getName() { return name; }
    public String getLastname() { return lastname; }
    public double getQualification() { return qualification; }
    public Map<Integer, List<int[]>> getSchedule() { return schedule; }

    private static Set<Integer> parseDaysDigits(String digits) {
        Set<Integer> out = new HashSet<>();
        if (digits != null) {
            for (char c : digits.toCharArray()) if (Character.isDigit(c)) {
                int d = c - '0';
                if (d >= 1 && d <= 7) out.add(d);
            }
        }
        return out;
    }

    private static int[] parseRangeString(String encoded) {
        if (encoded == null) throw new IllegalArgumentException("Rango nulo.");
        String[] parts = encoded.replace(" ", "").split("-");
        if (parts.length != 2) throw new IllegalArgumentException("Formato inválido (usa 'HHMM-HHMM'): " + encoded);
        int start = Integer.parseInt(parts[0]);
        int end   = Integer.parseInt(parts[1]);
        if (start >= end) throw new IllegalArgumentException("Inicio >= fin: " + start + " >= " + end);
        int sh = start/100, sm = start%100, eh = end/100, em = end%100;
        if (sh<0||sh>23||eh<0||eh>23||sm<0||sm>59||em<0||em>59)
            throw new IllegalArgumentException("Hora/minuto fuera de rango: " + encoded);
        return new int[]{start, end};
    }

    private static boolean overlap(int aStart, int aEnd, int bStart, int bEnd) {
        return aStart < bEnd && bStart < aEnd;
    }

    public boolean clashesWith(Subjects other) {
        if (!Objects.equals(this.group, other.group)) return false;
        for (int day : this.schedule.keySet()) {
            List<int[]> A = this.schedule.get(day);
            List<int[]> B = other.schedule.get(day);
            if (A == null || B == null) continue;
            for (int[] a : A)
                for (int[] b : B)
                    if (overlap(a[0], a[1], b[0], b[1])) return true;
        }
        return false;
    }

    public static String dayName(int d) {
        return switch (d) {
            case 1 -> "Lun"; case 2 -> "Mar"; case 3 -> "Mié";
            case 4 -> "Jue"; case 5 -> "Vie"; case 6 -> "Sáb"; case 7 -> "Dom";
            default -> "?";
        };
    }

    public static String fmtHHmm(int hhmm) {
        return String.format("%02d:%02d", hhmm/100, hhmm%100);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(subject)
                .append(" [").append(group).append("]")
                .append(" | Prof: ").append(name).append(" ").append(lastname)
                .append(" | Calf: ").append(qualification)
                .append(" | ");
        var days = new ArrayList<>(schedule.keySet());
        Collections.sort(days);
        for (int d : days) {
            sb.append(dayName(d)).append(":");
            for (int[] s : schedule.get(d)) sb.append("[").append(fmtHHmm(s[0])).append("-").append(fmtHHmm(s[1])).append("]");
            sb.append(" ");
        }
        return sb.toString();
    }
}

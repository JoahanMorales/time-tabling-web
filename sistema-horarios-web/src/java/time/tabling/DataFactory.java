package time.tabling;

import java.util.*;

public class DataFactory {
    public static List<Subjects> buildFromTable() {
        List<Subjects> list = new ArrayList<>();

        /* ===== 2AM1 =====
        list.add(Subjects.fromBlocks(8.7, "Miguel Santiago", "Suarez Castañon",
                "ALGORITMOS Y ESTRUCTURAS DE DATOS", "2AM1",
                new Subjects.Block("14", "0700-0830"),
                new Subjects.Block("5",  "0830-1000")
        ));

        list.add(Subjects.fromBlocks(9.1, "Alejandro", "Gonzalez Cisneros",
                "ALGEBRA LINEAL", "2AM1",
                new Subjects.Block("235", "1030-1200")   
        ));

        list.add(Subjects.fromBlocks(8.4, "Jean", "Ortega Gonzalez",
                "ETICA Y LEGALIDAD", "2AM1",
                new Subjects.Block("235", "0700-0830")
        ));

        list.add(Subjects.fromBlocks(9.3, "Cesar", "Hernandez Vasquez",
                "CALCULO MULTIVARIABLE", "2AM1",
                new Subjects.Block("134", "0830-1000"), 
                new Subjects.Block("5",   "1200-1330")
        ));

        list.add(Subjects.fromBlocks(8.0, "Rafael", "Ramirez Tenorio",
                "FUNDAMENTOS ECONOMICOS", "2AM1",
                new Subjects.Block("1", "1030-1200"),
                new Subjects.Block("2", "0830-1000"),
                new Subjects.Block("4", "1030-1200")
        ));

        // ===== 2AM2 =====
        list.add(Subjects.fromBlocks(9.0, "Jose Marco Antonio", "Rueda Melendez",
                "ALGORITMOS Y ESTRUCTURAS DE DATOS", "2AM2",
                new Subjects.Block("235", "0700-0830")
        ));

        list.add(Subjects.fromBlocks(8.6, "Roberto", "Vazquez Arreguin",
                "ALGEBRA LINEAL", "2AM2",
                new Subjects.Block("134", "0830-1000")
        ));

        list.add(Subjects.fromBlocks(7.9, "Lilian", "Martinez Acosta",
                "ETICA Y LEGALIDAD", "2AM2",
                new Subjects.Block("14", "0700-0830"),
                new Subjects.Block("5",  "0830-1000")
        ));

        list.add(Subjects.fromBlocks(9.7, "Cesar Roman", "Martinez Garcia",
                "CALCULO MULTIVARIABLE", "2AM2",
                new Subjects.Block("1", "1030-1200"),
                new Subjects.Block("2", "0830-1000"),
                new Subjects.Block("3", "1200-1330"),
                new Subjects.Block("4", "1030-1200")
        ));

        list.add(Subjects.fromBlocks(8.2, "Rafael", "Ramirez Tenorio",
                "FUNDAMENTOS ECONOMICOS", "2AM2",
                new Subjects.Block("235", "1030-1200")
        ));*/

        // ===================== 3BM1 =====================

        list.add(Subjects.fromBlocks(
            8.4, "Sandra", "Diaz Santiago",
            "ANÁLISIS Y DISEÑO DE ALGORITMOS", "3BM1",
            new Subjects.Block("14", "1030-1200"),   // Lun y Jue 10:30-12:00
            new Subjects.Block("2",  "0830-1000")    // Mar 08:30-10:00
        ));

        list.add(Subjects.fromBlocks(
            7.4, "Jose", "Sanchez Juarez",
            "PARADIGMAS DE PROGRAMACIÓN", "3BM1",
            new Subjects.Block("134", "1200-1330")   // Lun, Mié y Jue 12:00-13:30
        ));

        list.add(Subjects.fromBlocks(
            8.6, "Juan Manuel", "Carballo Jimenez",
            "ECUACIONES DIFERENCIALES", "3BM1",
            new Subjects.Block("235", "1030-1200")   // Mar, Mié y Vie 10:30-12:00
        ));

        list.add(Subjects.fromBlocks(
            6.7, "Erika", "Hernandez Rubio",
            "BASES DE DATOS", "3BM1",
            new Subjects.Block("235", "0700-0830")   // Mar, Mié y Vie 07:00-08:30
        ));

        list.add(Subjects.fromBlocks(
            5.6, "Alexis", "Testa Nava",
            "DISEÑO DE SISTEMAS DIGITALES", "3BM1",
            new Subjects.Block("14", "0700-0830"),   // Lun y Jue 07:00-08:30
            new Subjects.Block("5",  "0830-1000")    // Vie 08:30-10:00
        ));

        list.add(Subjects.fromBlocks(
            8.0, "Gisela", "Gonzalez Albarran",
            "LIDERAZGO PERSONAL", "3BM1",
            new Subjects.Block("134", "0830-1000")   // Lun, Mar y Mié 08:30-10:00
        ));


        // ===================== 3BM2 =====================

        list.add(Subjects.fromBlocks(
            8.8, "Miguel Angel", "Rodriguez Castillo",
            "ANÁLISIS Y DISEÑO DE ALGORITMOS", "3BM2",
            new Subjects.Block("134", "0830-1000")   // Lun, Mié y Jue 08:30-10:00
        ));
        
        list.add(Subjects.fromBlocks(
            6.7, "Erika", "Hernandez Rubio",
            "BASES DE DATOS", "3BM2",
            new Subjects.Block("235", "1030-1200")   // Mar, Mié y Vie 07:00-08:30
        ));

        list.add(Subjects.fromBlocks(
            9.0, "Andrés", "Cortés Dávalos",
            "PARADIGMAS DE PROGRAMACIÓN", "3BM2",
            new Subjects.Block("14", "0700-0830"),   // Lun y Jue 07:00-08:30
            new Subjects.Block("5",  "0830-1000")    // Vie 08:30-10:00
        ));

        list.add(Subjects.fromBlocks(
            6.2, "Jorge Alberto", "Cruz Rojas",
            "ECUACIONES DIFERENCIALES", "3BM2",
            new Subjects.Block("134", "1200-1330")   // Lun, Mié y Jue 12:00-13:30
        ));

        list.add(Subjects.fromBlocks(
            8.0, "Jose Juan", "Perez Perez",
            "DISEÑO DE SISTEMAS DIGITALES", "3BM2",
            new Subjects.Block("235", "0700-0830")   // Mar, Mié y Vie 07:00-08:30
        ));

        list.add(Subjects.fromBlocks(
            8.5, "Elia Tzindejhe", "Ramirez Martinez",
            "LIDERAZGO PERSONAL", "3BM2",
            new Subjects.Block("14", "1030-1200"),   // Lun y Jue 10:30-12:00
            new Subjects.Block("2",  "0830-1000")    // Mar 08:30-10:00
        ));

        return list;
    }
}

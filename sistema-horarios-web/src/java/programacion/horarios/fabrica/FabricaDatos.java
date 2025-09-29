package programacion.horarios.fabrica;

import java.util.ArrayList;
import java.util.List;
import programacion.horarios.nucleo.Materia;
import programacion.horarios.nucleo.Profesor;
import programacion.horarios.nucleo.RepositorioMaterias;

public class FabricaDatos {
    
    public static RepositorioMaterias crearRepositorioPorDefecto() {
        List<Materia> materias = new ArrayList<>();
        
        /*
        materias.add(crearMateria(8.4, "Sandra", "Diaz Santiago",
            "ANÁLISIS Y DISEÑO DE ALGORITMOS", "3BM1",
            new Bloque("14", "1030-1200"),
            new Bloque("2", "0830-1000")));
        
        materias.add(crearMateria(7.4, "Jose", "Sanchez Juarez",
            "PARADIGMAS DE PROGRAMACIÓN", "3BM1",
            new Bloque("134", "1200-1330")));
        
        materias.add(crearMateria(8.6, "Juan Manuel", "Carballo Jimenez",
            "ECUACIONES DIFERENCIALES", "3BM1",
            new Bloque("235", "1030-1200")));
        
        materias.add(crearMateria(6.7, "Erika", "Hernandez Rubio",
            "BASES DE DATOS", "3BM1",
            new Bloque("235", "0700-0830")));
        
        materias.add(crearMateria(5.6, "Alexis", "Testa Nava",
            "DISEÑO DE SISTEMAS DIGITALES", "3BM1",
            new Bloque("14", "0700-0830"),
            new Bloque("5", "0830-1000")));
        
        materias.add(crearMateria(8.0, "Gisela", "Gonzalez Albarran",
            "LIDERAZGO PERSONAL", "3BM1",
            new Bloque("134", "0830-1000")));
        
        // Grupo 3BM2
        materias.add(crearMateria(8.8, "Miguel Angel", "Rodriguez Castillo",
            "ANÁLISIS Y DISEÑO DE ALGORITMOS", "3BM2",
            new Bloque("134", "0830-1000")));
        
        materias.add(crearMateria(6.7, "Erika", "Hernandez Rubio",
            "BASES DE DATOS", "3BM2",
            new Bloque("235", "1030-1200")));
        
        materias.add(crearMateria(9.0, "Andrés", "Cortés Dávalos",
            "PARADIGMAS DE PROGRAMACIÓN", "3BM2",
            new Bloque("14", "0700-0830"),
            new Bloque("5", "0830-1000")));
        
        materias.add(crearMateria(6.2, "Jorge Alberto", "Cruz Rojas",
            "ECUACIONES DIFERENCIALES", "3BM2",
            new Bloque("134", "1200-1330")));
        
        materias.add(crearMateria(8.0, "Jose Juan", "Perez Perez",
            "DISEÑO DE SISTEMAS DIGITALES", "3BM2",
            new Bloque("235", "0700-0830")));
        
        materias.add(crearMateria(8.5, "Elia Tzindejhe", "Ramirez Martinez",
            "LIDERAZGO PERSONAL", "3BM2",
            new Bloque("14", "1030-1200"),
            new Bloque("2", "0830-1000")));*/
        
        materias.add(crearMateria(10.0, "Zacarias", "Jimenez Adrian",
            "PROBABILIDAD", "3NM30",
            new Bloque("3", "1100-1300"),
            new Bloque("4", "1100-1300")
        ));

        materias.add(crearMateria(4.3, "Sonck", "Ledesma Judith",
                    "ALGORITMOS COMPUTACIONALES", "3NM30",
                    new Bloque("3", "0900-1100"),
                    new Bloque("4", "0700-0900")
        ));

        materias.add(crearMateria(7.5, "Cortes Noriega", "Miguel Angel",
                    "INGENIERÍA DE REQUERIMIENTOS", "3NM30",
                    new Bloque("3", "0700-0900"),
                    new Bloque("5", "0700-0900")
        ));

        materias.add(crearMateria(8.0, "Angeles", "Jacinto Arturo",
                    "DISEÑO DE INTERFACES DE USUARIO", "3NM30",
                    new Bloque("1", "0800-0900"),
                    new Bloque("2", "0700-0900")
        ));

        materias.add(crearMateria(8.0, "Angeles", "Jacinto Arturo",
                    "ARQUITECTURA Y ORGANIZACIÓN DE LAS COMPUTADORAS", "3NM30",
                    new Bloque("2", "0900-1100"),
                    new Bloque("4", "0900-1100")
        ));

        materias.add(crearMateria(8.0, "Nonato", "Ramirez Susana",
                    "CONSTRUCCIÓN DE BASES DE DATOS", "3NM30",
                    new Bloque("1", "0900-1100"),
                    new Bloque("5", "0900-1100")
        ));

        materias.add(crearMateria(6.0, "Lopez", "Valeriano Iliana",
                    "PROGRAMACIÓN ORIENTADA A OBJETOS", "3NM30",
                    new Bloque("1", "1100-1300"),
                    new Bloque("2", "1100-1300")
        ));

        materias.add(crearMateria(10.0, "Zacarias", "Jimenez Adrian",
                    "PROBABILIDAD", "3NM31",
                    new Bloque("2", "1100-1300"),
                    new Bloque("5", "1100-1300")
        ));

        materias.add(crearMateria(7.0, "Fuenlabrada Velazquez M En", "C Sergio",
                    "ALGORITMOS COMPUTACIONALES", "3NM31",
                    new Bloque("1", "0700-0900"),
                    new Bloque("5", "0700-0900")
        ));

        materias.add(crearMateria(7.0, "Mendoza Pinto M En E", "Maria Nacira",
                    "INGENIERÍA DE REQUERIMIENTOS", "3NM31",
                    new Bloque("1", "0900-1100"),
                    new Bloque("3", "0900-1100")
        ));

        materias.add(crearMateria(7.0, "Gonzalez De La O", "Francisco Fabian",
                    "DISEÑO DE INTERFACES DE USUARIO", "3NM31",
                    new Bloque("1", "1100-1200"),
                    new Bloque("3", "1100-1300")
        ));

        materias.add(crearMateria(7.0, "Gonzalez De La O", "Francisco Fabian",
                    "ARQUITECTURA Y ORGANIZACIÓN DE LAS COMPUTADORAS", "3NM31",
                    new Bloque("2", "0900-1100"),
                    new Bloque("4", "0900-1100")
        ));

        materias.add(crearMateria(9.0, "Cruz", "Martinez Ramon",
                    "CONSTRUCCIÓN DE BASES DE DATOS", "3NM31",
                    new Bloque("2", "0700-0900"),
                    new Bloque("3", "0700-0900")
        ));

        materias.add(crearMateria(6.0, "Jesus", "Carrillo Carlos",
                    "PROGRAMACIÓN ORIENTADA A OBJETOS", "3NM31",
                    new Bloque("4", "0700-0900"),
                    new Bloque("5", "0900-1100")
        ));

        materias.add(crearMateria(9.0, "Lazaro", "Gonzalez Sergio",
                    "PROBABILIDAD", "3NM32",
                    new Bloque("2", "1100-1300"),
                    new Bloque("3", "1100-1300")
        ));

        materias.add(crearMateria(7.0, "Fuenlabrada Velazquez M En", "C Sergio",
                    "ALGORITMOS COMPUTACIONALES", "3NM32",
                    new Bloque("2", "0900-1100"),
                    new Bloque("4", "1100-1300")
        ));

        materias.add(crearMateria(7.0, "Gomez Coronel", "Oskar Armando",
                    "INGENIERÍA DE REQUERIMIENTOS", "3NM32",
                    new Bloque("2", "0700-0900"),
                    new Bloque("4", "0700-0900")
        ));

        materias.add(crearMateria(8.0, "Bustamante", "Tranquilino Rocio",
                    "DISEÑO DE INTERFACES DE USUARIO", "3NM32",
                    new Bloque("1", "0700-0900"),
                    new Bloque("5", "0800-0900")
        ));

        materias.add(crearMateria(4.4, "Casillas", "Rivas Alejandro",
                    "ARQUITECTURA Y ORGANIZACIÓN DE LAS COMPUTADORAS", "3NM32",
                    new Bloque("1", "0900-1100"),
                    new Bloque("3", "0900-1100")
        ));

        materias.add(crearMateria(9.0, "Salas Cruz", "Rocio Leticia",
                    "CONSTRUCCIÓN DE BASES DE DATOS", "3NM32",
                    new Bloque("4", "0900-1100"),
                    new Bloque("5", "0900-1100")
        ));

        materias.add(crearMateria(9.0, "Lopez Goytia Dr", "Jose Luis",
                    "PROGRAMACIÓN ORIENTADA A OBJETOS", "3NM32",
                    new Bloque("1", "1100-1300"),
                    new Bloque("5", "1100-1300")
        ));

        materias.add(crearMateria(9.0, "Lazaro", "Gonzalez Sergio",
                    "PROBABILIDAD", "3NM33",
                    new Bloque("1", "1100-1300"),
                    new Bloque("5", "0900-1100")
        ));

        materias.add(crearMateria(10.0, "Mendez Giron", "Alejandra M",
                    "ALGORITMOS COMPUTACIONALES", "3NM33",
                    new Bloque("1", "0900-1100"),
                    new Bloque("3", "0900-1100")
        ));

        materias.add(crearMateria(7.0, "Mendoza Pinto M En E", "Maria Nacira",
                    "INGENIERÍA DE REQUERIMIENTOS", "3NM33",
                    new Bloque("2", "0900-1100"),
                    new Bloque("4", "0900-1100")
        ));

        materias.add(crearMateria(8.0, "Angeles", "Jacinto Arturo",
                    "DISEÑO DE INTERFACES DE USUARIO", "3NM33",
                    new Bloque("3", "0800-0900"),
                    new Bloque("4", "0700-0900")
        ));

        materias.add(crearMateria(6.0, "Ortiz Castrejon", "Nancy Lorena",
                    "ARQUITECTURA Y ORGANIZACIÓN DE LAS COMPUTADORAS", "3NM33",
                    new Bloque("2", "0700-0900"),
                    new Bloque("5", "0700-0900")
        ));

        materias.add(crearMateria(9.0, "Salas Cruz", "Rocio Leticia",
                    "CONSTRUCCIÓN DE BASES DE DATOS", "3NM33",
                    new Bloque("1", "1300-1500"),
                    new Bloque("3", "1300-1500")
        ));

        materias.add(crearMateria(6.0, "Jesus", "Carrillo Carlos",
                    "PROGRAMACIÓN ORIENTADA A OBJETOS", "3NM33",
                    new Bloque("2", "1100-1300"),
                    new Bloque("3", "1100-1300")
        ));

        
        return new RepositorioMaterias(materias);
    }
    private static Materia crearMateria(double calificacion, String nombre, String apellido,
                                        String nombreMateria, String grupo, Bloque... bloques) {
        Profesor profesor = new Profesor(nombre, apellido, calificacion);
        Materia materia = new Materia(profesor, nombreMateria, grupo);
        
        for (Bloque b : bloques) {
            materia.agregarBloqueHorario(b.digitosDias, b.rangos);
        }
        
        return materia;
    }
    
    private static class Bloque {
        final String digitosDias;
        final String[] rangos;
        
        Bloque(String digitosDias, String... rangos) {
            this.digitosDias = digitosDias;
            this.rangos = rangos;
        }
    }
}
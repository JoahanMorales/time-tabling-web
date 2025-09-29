package programacion.horarios;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import programacion.horarios.constructor.ConstructorHorario;
import programacion.horarios.constructor.ConstructorHorarioMaximaCobertura;
import programacion.horarios.constructor.ConstructorHorarioProfesoresFijados;
import programacion.horarios.constructor.optimizado.ConstructorHorarioOptimizado;
import programacion.horarios.algoritmo.ConstructorHorarioAStar;
import programacion.horarios.estrategia.EstrategiaEvaluacion;
import programacion.horarios.estrategia.EstrategiaMediaArmonica;
import programacion.horarios.estrategia.EstrategiaPonderadaPorMinutos;
import programacion.horarios.estrategia.EstrategiaPromedioMaxMin;
import programacion.horarios.estrategia.EstrategiaPromedioSimple;
import programacion.horarios.fabrica.FabricaDatos;
import programacion.horarios.nucleo.Materia;
import programacion.horarios.nucleo.Profesor;
import programacion.horarios.nucleo.Programable;
import programacion.horarios.nucleo.RepositorioMaterias;
import programacion.horarios.nucleo.Seleccion;
import programacion.horarios.reporte.ReporteHorario;
import programacion.horarios.servicio.ServicioHorarios;
import programacion.horarios.validador.ValidadorHorario;

public class ProgramacionHorarios {
    
    public static void main(String[] args) {
        // Crear repositorio de materias
        RepositorioMaterias repositorio = FabricaDatos.crearRepositorioPorDefecto();
        
        // Crear validador
        ValidadorHorario validador = new ValidadorHorario();
        
        // Validar conflictos existentes
        var resultadoValidacion = validador.validar(repositorio.obtenerMaterias());
        if (resultadoValidacion.esValido()) {
            System.out.println("✓ Sin conflictos en los grupos existentes");
        } else {
            System.out.println("⚠️ Conflictos encontrados:");
            resultadoValidacion.obtenerConflictos().forEach(System.out::println);
        }
        
        // Crear diferentes estrategias de evaluación
        EstrategiaEvaluacion estrategiaSimple = new EstrategiaPromedioSimple();
        EstrategiaEvaluacion estrategiaPonderada = new EstrategiaPonderadaPorMinutos();
        
        // Crear reporteador
        ReporteHorario reporte = new ReporteHorario(estrategiaPonderada);
        
        // Mostrar resumen por grupos existentes
        System.out.println("\n═══════════════════════════════════");
        System.out.println("    GRUPOS EXISTENTES");
        System.out.println("═══════════════════════════════════");
        
        for (String grupo : repositorio.obtenerTodosLosGrupos()) {
            reporte.imprimirResumenGrupo(grupo, repositorio.obtenerPorGrupo(grupo));
        }
        
        // Crear nuevo grupo con máxima cobertura
        System.out.println("\n═══════════════════════════════════");
        System.out.println("    CREANDO NUEVO GRUPO: 2AMX");
        System.out.println("═══════════════════════════════════");
        
        ConstructorHorario constructor = new ConstructorHorarioMaximaCobertura(repositorio, "2AMX", estrategiaPonderada);
        List<Seleccion> nuevoGrupo = constructor.construir();
        
        if (!nuevoGrupo.isEmpty()) {
            System.out.println("\n✓ Grupo creado exitosamente:");
            imprimirSelecciones(nuevoGrupo, estrategiaPonderada);
            
            // Mostrar matriz de horario
            List<Materia> nuevasMaterias = nuevoGrupo.stream()
                .map(Seleccion::asignada)
                .collect(Collectors.toList());
            reporte.imprimirMatrizHorario(nuevasMaterias);
        } else {
            System.out.println("✗ No se pudo crear el grupo");
        }
        
        // Crear grupo con profesores fijados
        System.out.println("\n═══════════════════════════════════");
        System.out.println("    GRUPO CON PROFESORES FIJADOS");
        System.out.println("═══════════════════════════════════");
        
        Map<String, String> profesoresFijados = Map.of(
            "ANÁLISIS Y DISEÑO DE ALGORITMOS", "Sandra Diaz Santiago",
            "PARADIGMAS DE PROGRAMACIÓN", "Andrés Cortés Dávalos"
        );
        
        ConstructorHorario constructorFijado = new ConstructorHorarioProfesoresFijados(
            repositorio, "GRUPO_ESPECIAL", profesoresFijados
        );
        
        List<Seleccion> grupoFijado = constructorFijado.construir();
        
        if (!grupoFijado.isEmpty()) {
            System.out.println("\n✓ Grupo con profesores fijados creado:");
            System.out.println("Profesores obligatorios:");
            profesoresFijados.forEach((materia, profesor) -> 
                System.out.println("  • " + materia + " → " + profesor));
            
            System.out.println("\nHorario resultante:");
            imprimirSelecciones(grupoFijado, estrategiaPonderada);
        } else {
            System.out.println("✗ No se pudo crear el grupo con las restricciones dadas");
        }
        
        // Análisis comparativo de estrategias
        System.out.println("\n═══════════════════════════════════");
        System.out.println("    ANÁLISIS COMPARATIVO");
        System.out.println("═══════════════════════════════════");
        
        if (!nuevoGrupo.isEmpty()) {
            List<Materia> materias = nuevoGrupo.stream()
                .map(Seleccion::asignada)
                .collect(Collectors.toList());
            
            System.out.println("Grupo 2AMX:");
            System.out.printf("  • Puntuación promedio simple: %.2f%n", 
                estrategiaSimple.evaluar(materias));
            System.out.printf("  • Puntuación ponderada por minutos: %.2f%n", 
                estrategiaPonderada.evaluar(materias));
            
            // Análisis por materia
            System.out.println("\nDesglose por materia:");
            materias.stream()
                .sorted(Comparator.reverseOrder())
                .forEach(m -> System.out.printf("  • %-40s | Prof: %-25s | Calif: %.1f | Min: %d%n",
                    m.obtenerNombreMateria(),
                    m.obtenerProfesor().obtenerNombreCompleto(),
                    m.obtenerCalificacion(),
                    m.obtenerMinutosTotales()));
        }
        
        // Demostración de polimorfismo con diferentes constructores
        demostrarPolimorfismo(repositorio);
    }
    
    private static void imprimirSelecciones(List<Seleccion> selecciones, EstrategiaEvaluacion estrategia) {
        for (Seleccion sel : selecciones) {
            Materia m = sel.asignada();
            System.out.printf("  • %-40s | Prof: %-25s | Calif: %.1f | Origen: %s%n",
                m.obtenerNombreMateria(),
                m.obtenerProfesor().obtenerNombreCompleto(),
                m.obtenerCalificacion(),
                sel.grupoOrigen());
        }
        
        List<Materia> materias = selecciones.stream()
            .map(Seleccion::asignada)
            .collect(Collectors.toList());
        
        System.out.printf("\nPuntuación total (%s): %.2f%n", 
            estrategia.obtenerNombreEstrategia(), estrategia.evaluar(materias));
    }
    
private static void demostrarPolimorfismo(RepositorioMaterias repositorio) {
    System.out.println("\n═══════════════════════════════════");
    System.out.println("    DEMOSTRACIÓN DE POLIMORFISMO");
    System.out.println("═══════════════════════════════════");
          
    List<ConstructorHorario> constructores = List.of(
        new ConstructorHorarioMaximaCobertura(repositorio, "POLI_1", new EstrategiaPromedioSimple()),
        new ConstructorHorarioMaximaCobertura(repositorio, "POLI_2", new EstrategiaPonderadaPorMinutos()),
        new ConstructorHorarioProfesoresFijados(repositorio, "POLI_3", 
            Map.of("BASES DE DATOS", "Erika Hernandez Rubio"))
        );
        
        // Usar polimorfismo para construir con diferentes estrategias
        for (int i = 0; i < constructores.size(); i++) {
            ConstructorHorario constructor = constructores.get(i);
            List<Seleccion> resultado = constructor.construir();
            
            System.out.printf("\nConstructor %d (%s):%n", 
                i + 1, constructor.getClass().getSimpleName());
            
            if (!resultado.isEmpty()) {
                System.out.printf("  Materias asignadas: %d%n", resultado.size());
                double calificacionPromedio = resultado.stream()
                    .map(Seleccion::asignada)
                    .mapToDouble(Materia::obtenerCalificacion)
                    .average()
                    .orElse(0.0);
                System.out.printf("  Calificación promedio: %.2f%n", calificacionPromedio);
            } else {
                System.out.println("  No se pudo construir");
            }
        }
        
        // Demostrar uso de interfaces con diferentes implementaciones
        demostrarInterfaces();
        
        // Demostrar cache
        demostrarCache(repositorio);
        
        // NUEVA FUNCIONALIDAD: Comparar algoritmos
        compararAlgoritmos(repositorio);
    }
    
    private static void demostrarInterfaces() {
        System.out.println("\n═══════════════════════════════════");
        System.out.println("    USO DE INTERFACES");
        System.out.println("═══════════════════════════════════");
        
        // Crear materias de ejemplo
        Profesor profesor1 = new Profesor("Juan", "Pérez", 9.0);
        Profesor profesor2 = new Profesor("María", "García", 8.5);
        
        Materia materia1 = new Materia(profesor1, "Matemáticas", "PRUEBA");
        materia1.agregarBloqueHorario("135", "0900-1030");
        
        Materia materia2 = new Materia(profesor2, "Física", "PRUEBA");
        materia2.agregarBloqueHorario("24", "0900-1030");
        
        // Usar la interfaz Programable
        List<Programable> programables = List.of(materia1, materia2);
        
        System.out.println("\nUsando interfaz Programable:");
        for (Programable p : programables) {
            System.out.printf("  • ID: %s | Minutos totales: %d%n",
                p.obtenerIdentificador(), p.obtenerMinutosTotales());
        }
        
        // Verificar conflictos usando la interfaz
        boolean tieneConflicto = materia1.tieneConflictoCon(materia2);
        System.out.printf("\n¿Conflicto entre %s y %s? %s%n",
            materia1.obtenerNombreMateria(), materia2.obtenerNombreMateria(),
            tieneConflicto ? "SÍ" : "NO");
        
        // Usar diferentes estrategias de evaluación (Patrón Strategy)
        List<EstrategiaEvaluacion> estrategias = List.of(
            new EstrategiaPromedioSimple(),
            new EstrategiaPonderadaPorMinutos(),
            new EstrategiaPromedioMaxMin(),
            new EstrategiaMediaArmonica()
        );
        
        List<Materia> materiasPrueba = List.of(materia1, materia2);
        
        System.out.println("\nEvaluación con diferentes estrategias:");
        for (EstrategiaEvaluacion estrategia : estrategias) {
            double puntuacion = estrategia.evaluar(materiasPrueba);
            System.out.printf("  • %-30s: %.2f%n", 
                estrategia.obtenerNombreEstrategia(), puntuacion);
        }
    }
    
    private static void demostrarCache(RepositorioMaterias repositorio) {
        System.out.println("\n═══════════════════════════════════");
        System.out.println("    DEMOSTRACIÓN DEL CACHE");
        System.out.println("═══════════════════════════════════");
        
        ServicioHorarios servicio = new ServicioHorarios(repositorio);
        EstrategiaEvaluacion estrategia = new EstrategiaPonderadaPorMinutos();
        
        // Primera ejecución
        System.out.println("=== Primera ejecución ===");
        List<Seleccion> resultado1 = servicio.crearHorario("CACHE_TEST", estrategia);
        
        // Segunda ejecución (debería ser más rápida)
        System.out.println("\n=== Segunda ejecución (mismo parámetros) ===");
        List<Seleccion> resultado2 = servicio.crearHorario("CACHE_TEST", estrategia);
        
        // Tercera ejecución (diferentes parámetros)
        System.out.println("\n=== Tercera ejecución (diferentes parámetros) ===");
        List<Seleccion> resultado3 = servicio.crearHorario("OTRO_GRUPO", estrategia);
        
        // Cuarta ejecución (profesores fijados)
        System.out.println("\n=== Cuarta ejecución (profesores fijados) ===");
        Map<String, String> profesoresFijados = Map.of(
            "ANÁLISIS Y DISEÑO DE ALGORITMOS", "Sandra Diaz Santiago"
        );
        List<Seleccion> resultado4 = servicio.crearHorarioConProfesoresFijados(
            "CACHE_FIJADOS", profesoresFijados);
        
        System.out.println("\n" + servicio.obtenerEstadisticasCache());
        
        // Simular uso frecuente
        System.out.println("\n=== Simulación uso frecuente ===");
        String[] grupos = {"FREQ_A", "FREQ_B", "FREQ_A", "FREQ_B", "FREQ_C", "FREQ_A"};
        
        for (int i = 0; i < grupos.length; i++) {
            System.out.printf("Request %d (%s): ", i + 1, grupos[i]);
            servicio.crearHorario(grupos[i], estrategia);
        }
        
        System.out.println("\nEstadísticas finales: " + servicio.obtenerEstadisticasCache());
    }
    
    // ============= NUEVA FUNCIONALIDAD: COMPARACIÓN DE ALGORITMOS =============
    
    private static void compararAlgoritmos(RepositorioMaterias repositorio) {
        System.out.println("\n═══════════════════════════════════");
        System.out.println("    COMPARACIÓN DE ALGORITMOS");
        System.out.println("═══════════════════════════════════");
        
        String grupoComparacion = "COMPARACION";
        EstrategiaEvaluacion estrategia = new EstrategiaPonderadaPorMinutos();
        
        // Lista de constructores para comparar
        List<ConstructorInfo> constructores = List.of(
            new ConstructorInfo(
                "Backtracking Original",
                new ConstructorHorarioMaximaCobertura(repositorio, grupoComparacion + "_ORIG", estrategia)
            ),
            new ConstructorInfo(
                "Algoritmo Voraz Optimizado", 
                new ConstructorHorarioOptimizado(repositorio, grupoComparacion + "_OPT")
            ),
            new ConstructorInfo(
                "A* Heurístico",
                new ConstructorHorarioAStar(repositorio, grupoComparacion + "_ASTAR")
            )
        );
        
        List<ResultadoComparacion> resultados = new ArrayList<>();
        ReporteHorario reporteVisual = new ReporteHorario(estrategia);
        
        for (ConstructorInfo info : constructores) {
            System.out.println("\n" + "=".repeat(50));
            System.out.println("  " + info.nombre);
            System.out.println("=".repeat(50));
            
            long tiempoInicio = System.currentTimeMillis();
            List<Seleccion> horario = info.constructor.construir();
            long tiempoTotal = System.currentTimeMillis() - tiempoInicio;
            
            if (!horario.isEmpty()) {
                List<Materia> materias = horario.stream()
                    .map(Seleccion::asignada)
                    .collect(Collectors.toList());
                
                double puntuacion = estrategia.evaluar(materias);
                
                System.out.printf("✓ Completado en %d ms%n", tiempoTotal);
                System.out.printf("  Materias asignadas: %d%n", horario.size());
                System.out.printf("  Puntuación: %.2f%n", puntuacion);
                
                // Mostrar horario detallado
                System.out.println("\n--- HORARIO GENERADO ---");
                for (Seleccion sel : horario) {
                    Materia m = sel.asignada();
                    System.out.printf("• %-35s | Prof: %-20s | Calif: %.1f | Origen: %s%n",
                        m.obtenerNombreMateria(),
                        m.obtenerProfesor().obtenerNombreCompleto(),
                        m.obtenerCalificacion(),
                        sel.grupoOrigen());
                }
                
                // Mostrar matriz visual del horario
                System.out.println("\n--- MATRIZ DE HORARIO ---");
                reporteVisual.imprimirMatrizHorario(materias);
                
                resultados.add(new ResultadoComparacion(info.nombre, tiempoTotal, horario.size(), puntuacion, horario));
            } else {
                System.out.println("✗ No se pudo generar horario");
                resultados.add(new ResultadoComparacion(info.nombre, tiempoTotal, 0, 0.0, List.of()));
            }
        }
        
        // Resumen comparativo final
        imprimirResumenComparativo(resultados);
        
        // Análisis detallado de diferencias
        analizarDiferencias(resultados);
    }
    
    private static void imprimirResumenComparativo(List<ResultadoComparacion> resultados) {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("                        RESUMEN COMPARATIVO");
        System.out.println("=".repeat(70));
        
        System.out.printf("%-25s | %8s | %8s | %10s | %8s%n", 
            "Algoritmo", "Tiempo", "Materias", "Puntuación", "Estado");
        System.out.println("-".repeat(70));
        
        for (ResultadoComparacion resultado : resultados) {
            String estado = resultado.materias > 0 ? "✓ Éxito" : "✗ Falló";
            System.out.printf("%-25s | %6d ms | %8d | %10.2f | %8s%n",
                resultado.algoritmo,
                resultado.tiempoMs,
                resultado.materias,
                resultado.puntuacion,
                estado);
        }
        
        // Encontrar ganadores
        ResultadoComparacion masPapido = resultados.stream()
            .filter(r -> r.materias > 0)
            .min(Comparator.comparingLong(r -> r.tiempoMs))
            .orElse(null);
        
        ResultadoComparacion mejorCalidad = resultados.stream()
            .max(Comparator.comparingDouble(r -> r.puntuacion))
            .orElse(null);
            
        ResultadoComparacion masMaterias = resultados.stream()
            .max(Comparator.comparingInt(r -> r.materias))
            .orElse(null);
        
        System.out.println("\n--- GANADORES POR CATEGORÍA ---");
        if (masPapido != null) {
            System.out.printf("⚡ Más rápido: %s (%d ms)%n", 
                masPapido.algoritmo, masPapido.tiempoMs);
        }
        if (mejorCalidad != null) {
            System.out.printf("🏆 Mejor calidad: %s (%.2f puntos)%n", 
                mejorCalidad.algoritmo, mejorCalidad.puntuacion);
        }
        if (masMaterias != null) {
            System.out.printf("📚 Más materias: %s (%d materias)%n", 
                masMaterias.algoritmo, masMaterias.materias);
        }
    }
    
    private static void analizarDiferencias(List<ResultadoComparacion> resultados) {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("                    ANÁLISIS DE DIFERENCIAS");
        System.out.println("=".repeat(70));
        
        // Comparar profesores seleccionados
        if (resultados.size() >= 2) {
            for (int i = 0; i < resultados.size(); i++) {
                for (int j = i + 1; j < resultados.size(); j++) {
                    ResultadoComparacion r1 = resultados.get(i);
                    ResultadoComparacion r2 = resultados.get(j);
                    
                    System.out.printf("\n--- %s vs %s ---%n", r1.algoritmo, r2.algoritmo);
                    
                    Map<String, String> profesores1 = extraerProfesoresPorMateria(r1.horario);
                    Map<String, String> profesores2 = extraerProfesoresPorMateria(r2.horario);
                    
                    Set<String> todasMaterias = new HashSet<>();
                    todasMaterias.addAll(profesores1.keySet());
                    todasMaterias.addAll(profesores2.keySet());
                    
                    boolean hayDiferencias = false;
                    for (String materia : todasMaterias) {
                        String prof1 = profesores1.get(materia);
                        String prof2 = profesores2.get(materia);
                        
                        if (prof1 != null && prof2 != null) {
                            if (!prof1.equals(prof2)) {
                                if (!hayDiferencias) {
                                    System.out.println("Diferencias en profesores:");
                                    hayDiferencias = true;
                                }
                                System.out.printf("  %s:%n", materia);
                                System.out.printf("    %s → %s%n", r1.algoritmo, prof1);
                                System.out.printf("    %s → %s%n", r2.algoritmo, prof2);
                            }
                        } else if (prof1 != null) {
                            System.out.printf("  %s: Solo en %s (%s)%n", materia, r1.algoritmo, prof1);
                        } else if (prof2 != null) {
                            System.out.printf("  %s: Solo en %s (%s)%n", materia, r2.algoritmo, prof2);
                        }
                    }
                    
                    if (!hayDiferencias && !profesores1.isEmpty() && !profesores2.isEmpty()) {
                        System.out.println("✓ Mismos profesores seleccionados");
                    }
                    
                    // Mostrar métricas comparativas
                    double diferenciaPuntuacion = Math.abs(r1.puntuacion - r2.puntuacion);
                    long diferenciaTiempo = Math.abs(r1.tiempoMs - r2.tiempoMs);
                    
                    System.out.printf("\nMétricas comparativas:%n");
                    System.out.printf("  Diferencia en puntuación: %.2f%n", diferenciaPuntuacion);
                    System.out.printf("  Diferencia en tiempo: %d ms%n", diferenciaTiempo);
                }
            }
        }
    }
    
    private static Map<String, String> extraerProfesoresPorMateria(List<Seleccion> horario) {
        Map<String, String> resultado = new HashMap<>();
        for (Seleccion sel : horario) {
            Materia m = sel.asignada();
            resultado.put(m.obtenerNombreMateria(), m.obtenerProfesor().obtenerNombreCompleto());
        }
        return resultado;
    }
    
    // ============= CLASES AUXILIARES =============
    
    private static class ConstructorInfo {
        final String nombre;
        final ConstructorHorario constructor;
        
        ConstructorInfo(String nombre, ConstructorHorario constructor) {
            this.nombre = nombre;
            this.constructor = constructor;
        }
    }
    
    private static class ResultadoComparacion {
        final String algoritmo;
        final long tiempoMs;
        final int materias;
        final double puntuacion;
        final List<Seleccion> horario;
        
        ResultadoComparacion(String algoritmo, long tiempoMs, int materias, double puntuacion, List<Seleccion> horario) {
            this.algoritmo = algoritmo;
            this.tiempoMs = tiempoMs;
            this.materias = materias;
            this.puntuacion = puntuacion;
            this.horario = horario;
        }
    }
}
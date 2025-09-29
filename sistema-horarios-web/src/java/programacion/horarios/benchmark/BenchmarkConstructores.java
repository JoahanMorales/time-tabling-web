package programacion.horarios.benchmark;

import java.util.List;
import java.util.stream.Collectors;
import programacion.horarios.algoritmo.ConstructorHorarioAStar;
import programacion.horarios.constructor.ConstructorHorario;
import programacion.horarios.constructor.ConstructorHorarioMaximaCobertura;
import programacion.horarios.constructor.optimizado.ConstructorHorarioOptimizado;
import programacion.horarios.estrategia.EstrategiaPonderadaPorMinutos;
import programacion.horarios.fabrica.FabricaDatos;
import programacion.horarios.nucleo.RepositorioMaterias;
import programacion.horarios.nucleo.Seleccion;

public class BenchmarkConstructores {
    
    public static void compararRendimiento(RepositorioMaterias repositorio) {
        String[] nombreGrupos = {"BENCH_1", "BENCH_2", "BENCH_3"};
        
        List<ConstructorHorario> constructores = List.of(
            new ConstructorHorarioMaximaCobertura(repositorio, nombreGrupos[0]),
            new ConstructorHorarioOptimizado(repositorio, nombreGrupos[1]),
            new ConstructorHorarioAStar(repositorio, nombreGrupos[2])
        );
        
        System.out.println("=== BENCHMARK DE CONSTRUCTORES ===");
        System.out.println("Constructor                    | Tiempo (ms) | Materias | Calidad");
        System.out.println("-------------------------------|-------------|----------|--------");
        
        for (int i = 0; i < constructores.size(); i++) {
            ConstructorHorario constructor = constructores.get(i);
            
            long tiempoInicio = System.currentTimeMillis();
            List<Seleccion> resultado = constructor.construir();
            long tiempoFin = System.currentTimeMillis();
            
            double calidad = resultado.isEmpty() ? 0.0 : 
                new EstrategiaPonderadaPorMinutos().evaluar(
                    resultado.stream()
                        .map(Seleccion::asignada)
                        .collect(Collectors.toList())
                );
            
            System.out.printf("%-30s | %11d | %8d | %6.2f%n",
                constructor.getClass().getSimpleName(),
                tiempoFin - tiempoInicio,
                resultado.size(),
                calidad);
        }
    }
    
    public static void main(String[] args) {
        RepositorioMaterias repo = FabricaDatos.crearRepositorioPorDefecto();
        compararRendimiento(repo);
    }
}
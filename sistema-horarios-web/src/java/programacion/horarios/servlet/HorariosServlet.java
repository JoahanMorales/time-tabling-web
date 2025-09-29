package programacion.horarios.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Logger;
import java.util.logging.Level;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import java.util.ArrayList;

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
import programacion.horarios.nucleo.FranjaHoraria;
import programacion.horarios.nucleo.Materia;
import programacion.horarios.nucleo.Profesor;
import programacion.horarios.nucleo.RepositorioMaterias;
import programacion.horarios.nucleo.Seleccion;
import programacion.horarios.servicio.ServicioHorarios;

@WebServlet(name = "HorariosServlet", urlPatterns = {"/api/horarios"})
public class HorariosServlet extends HttpServlet {


    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
    }   
    private static final Logger LOGGER = Logger.getLogger(HorariosServlet.class.getName());
    private static final int MAX_GRUPO_NAME_LENGTH = 50;
    
    private RepositorioMaterias repositorio;
    private ServicioHorarios servicioHorarios;
    private Gson gson;
    
    @Override
    public void init() throws ServletException {
        try {
            this.repositorio = FabricaDatos.crearRepositorioPorDefecto();
            this.servicioHorarios = new ServicioHorarios(repositorio);
            this.gson = new GsonBuilder()
                    .setPrettyPrinting()
                    .serializeNulls()
                    .create();
            
            LOGGER.info("HorariosServlet inicializado correctamente");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al inicializar HorariosServlet", e);
            throw new ServletException("Error de inicialización", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {

        processRequest(request, response);

        String action = obtenerParametroSeguro(request, "action");

        LOGGER.info("GET request - Action: " + action);

        try {
            switch (action != null ? action : "") {
                case "grupos":
                    obtenerGruposExistentes(response);
                    break;
                case "profesores":
                    obtenerProfesores(response);
                    break;
                case "materias":
                    obtenerMaterias(response);
                    break;
                case "estadisticas":
                    obtenerEstadisticas(response);
                    break;
                case "cache":
                    obtenerEstadisticasCache(response);
                    break;
                case "horario":
                    obtenerHorarioGrupo(request, response);
                    break;
                default:
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    escribirError(response, "Acción no válida: " + action);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error en doGet - Action: " + action, e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            escribirError(response, "Error interno del servidor");
        }
    }
    
@Override
protected void doPost(HttpServletRequest request, HttpServletResponse response) 
        throws ServletException, IOException {
    
    processRequest(request, response);
    
    String action = obtenerParametroSeguro(request, "action");
    
    LOGGER.info("POST request - Action: " + action);
    
    try {
        switch (action != null ? action : "") {
            case "crear":
                crearGrupo(request, response);
                break;
            case "crear-fijado":
                crearGrupoConProfesoresFijados(request, response);
                break;
            case "comparar":
                compararAlgoritmos(request, response);
                break;
            case "validar":
                validarHorarios(request, response);
                break;
            default:
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                escribirError(response, "Acción no válida: " + action);
        }
    } catch (Exception e) {
        LOGGER.log(Level.SEVERE, "Error en doPost - Action: " + action, e);
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        escribirError(response, "Error interno del servidor");
    }
}
    
    private String obtenerParametroSeguro(HttpServletRequest request, String nombreParam) {
        String valor = request.getParameter(nombreParam);
        return (valor != null && !valor.trim().isEmpty()) ? valor.trim() : null;
    }
    
    private boolean validarNombreGrupo(String nombreGrupo) {
        return nombreGrupo != null 
            && !nombreGrupo.trim().isEmpty() 
            && nombreGrupo.length() <= MAX_GRUPO_NAME_LENGTH
            && nombreGrupo.matches("^[a-zA-Z0-9_-]+$");
    }
    
    private void obtenerGruposExistentes(HttpServletResponse response) throws IOException {
        try {
            Map<String, Object> resultado = new HashMap<>();
            Map<String, List<Map<String, Object>>> grupos = new HashMap<>();
            
            for (String grupo : repositorio.obtenerTodosLosGrupos()) {
                List<Materia> materias = repositorio.obtenerPorGrupo(grupo);
                if (materias != null && !materias.isEmpty()) {
                    List<Map<String, Object>> materiasInfo = materias.stream()
                        .map(this::convertirMateriaAMapa)
                        .collect(java.util.stream.Collectors.toList());
                    grupos.put(grupo, materiasInfo);
                }
            }
            
            resultado.put("success", true);
            resultado.put("grupos", grupos);
            resultado.put("totalGrupos", grupos.size());
            
            escribirRespuesta(response, resultado);
            LOGGER.info("Grupos obtenidos exitosamente: " + grupos.size());
            
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error al obtener grupos existentes", e);
            throw e;
        }
    }

    private void obtenerHorarioGrupo(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {

        String nombreGrupo = obtenerParametroSeguro(request, "grupo");

        if (!validarNombreGrupo(nombreGrupo)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            escribirError(response, "Nombre de grupo inválido");
            return;
        }

        try {
            List<Materia> materias = repositorio.obtenerPorGrupo(nombreGrupo);

            if (materias == null || materias.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                escribirError(response, "Grupo no encontrado: " + nombreGrupo);
                return;
            }

            System.out.println("=== DEBUGGING GRUPO: " + nombreGrupo + " ===");
            for (Materia materia : materias) {
                debugMateriaHorario(materia);
            }

            List<Map<String, Object>> horarioDetallado = generarHorarioDetallado(materias);

            System.out.println("=== HORARIO PROCESADO ===");
            for (Map<String, Object> entrada : horarioDetallado) {
                System.out.println(entrada);
            }

            Map<String, Object> resultado = new HashMap<>();
            resultado.put("success", true);
            resultado.put("grupo", nombreGrupo);
            resultado.put("horario", horarioDetallado);
            resultado.put("totalMaterias", materias.size());

            escribirRespuesta(response, resultado);
            LOGGER.info("Horario obtenido para grupo: " + nombreGrupo);

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error al obtener horario del grupo: " + nombreGrupo, e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            escribirError(response, "Error al obtener horario");
        }
    }
    
    private List<Map<String, Object>> generarHorarioDetallado(List<Materia> materias) {
        List<Map<String, Object>> horario = new ArrayList<>();

        for (Materia materia : materias) {
            Map<Integer, List<FranjaHoraria>> horarioMateria = materia.obtenerHorario();

            if (horarioMateria != null) {
                for (Map.Entry<Integer, List<FranjaHoraria>> entrada : horarioMateria.entrySet()) {
                    Integer codigoDia = entrada.getKey();
                    List<FranjaHoraria> franjas = entrada.getValue();

                    String nombreDia = obtenerNombreCompletoDia(codigoDia);

                    for (FranjaHoraria franja : franjas) {
                        Map<String, Object> entradaHorario = new HashMap<>();
                        entradaHorario.put("dia", nombreDia);

                        entradaHorario.put("horaInicio", formatearHora(franja.obtenerHoraInicio()));
                        entradaHorario.put("horaFin", formatearHora(franja.obtenerHoraFin()));

                        entradaHorario.put("materia", materia.obtenerNombreMateria());
                        entradaHorario.put("profesor", materia.obtenerProfesor().obtenerNombreCompleto());
                        entradaHorario.put("grupo", materia.obtenerGrupo());

                        entradaHorario.put("salon", generarSalonPorMateria(materia));

                        entradaHorario.put("calificacion", materia.obtenerCalificacion());
                        entradaHorario.put("duracionMinutos", franja.obtenerDuracionMinutos());

                        horario.add(entradaHorario);
                    }
                }
            }
        }

        horario.sort((h1, h2) -> {
            int comparacionDia = obtenerOrdenDia((String) h1.get("dia")) - 
                               obtenerOrdenDia((String) h2.get("dia"));
            if (comparacionDia != 0) return comparacionDia;

            return ((String) h1.get("horaInicio")).compareTo((String) h2.get("horaInicio"));
        });

        return horario;
    }
    
    private String obtenerNombreCompletoDia(Integer codigo) {
        switch (codigo) {
            case 1: return "Lunes";
            case 2: return "Martes";
            case 3: return "Miércoles";
            case 4: return "Jueves";
            case 5: return "Viernes";
            case 6: return "Sábado";
            case 7: return "Domingo";
            default: return "Día " + codigo;
        }
    }
    
    private int obtenerOrdenDia(String nombreDia) {
        return switch (nombreDia) {
            case "Lunes" -> 1;
            case "Martes" -> 2;
            case "Miércoles" -> 3;
            case "Jueves" -> 4;
            case "Viernes" -> 5;
            case "Sábado" -> 6;
            case "Domingo" -> 7;
            default -> 8;
        };
    }
    
    private String formatearHora(int valoreInterno) {
        
        if (valoreInterno >= 100 && valoreInterno <= 2359) {
            int horas = valoreInterno / 100;
            int minutos = valoreInterno % 100;

            if (minutos >= 60) {
                horas = valoreInterno / 60;
                minutos = valoreInterno % 60;
                System.out.println("DEBUG: Valor extraño " + valoreInterno + 
                                 ", usando conversión de minutos: " + horas + ":" + minutos);
            } else {
                System.out.println("DEBUG: Formato HHMM detectado " + valoreInterno + 
                                 " -> " + horas + ":" + String.format("%02d", minutos));
            }

            return String.format("%02d:%02d", horas, minutos);
        } else {
            int horas = valoreInterno / 60;
            int minutos = valoreInterno % 60;

            System.out.println("DEBUG: Minutos desde medianoche " + valoreInterno + 
                             " -> " + horas + ":" + String.format("%02d", minutos));

            return String.format("%02d:%02d", horas, minutos);
        }
    }
    
    private String generarSalonPorMateria(Materia materia) {
        int hash = Math.abs(materia.obtenerIdentificador().hashCode());
        String[] edificios = {"A", "B", "C", "D", "E"};
        String edificio = edificios[hash % edificios.length];
        int numeroSalon = 100 + (hash % 50);
        return edificio + numeroSalon;
    }

    
    private void obtenerProfesores(HttpServletResponse response) throws IOException {
        try {
            Map<String, Object> resultado = new HashMap<>();
            List<Map<String, Object>> profesores = repositorio.obtenerMaterias().stream()
                .map(Materia::obtenerProfesor)
                .distinct()
                .map(this::convertirProfesorAMapa)
                .collect(java.util.stream.Collectors.toList());
            
            resultado.put("success", true);
            resultado.put("profesores", profesores);
            resultado.put("totalProfesores", profesores.size());
            
            escribirRespuesta(response, resultado);
            LOGGER.info("Profesores obtenidos exitosamente: " + profesores.size());
            
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error al obtener profesores", e);
            throw e;
        }
    }
    
    private void obtenerMaterias(HttpServletResponse response) throws IOException {
        try {
            Map<String, Object> resultado = new HashMap<>();
            List<String> nombresMaterias = repositorio.obtenerMaterias().stream()
                .map(Materia::obtenerNombreMateria)
                .distinct()
                .sorted()
                .collect(java.util.stream.Collectors.toList());
            
            resultado.put("success", true);
            resultado.put("materias", nombresMaterias);
            resultado.put("totalMaterias", nombresMaterias.size());
            
            escribirRespuesta(response, resultado);
            LOGGER.info("Materias obtenidas exitosamente: " + nombresMaterias.size());
            
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error al obtener materias", e);
            throw e;
        }
    }
    
    private void obtenerEstadisticas(HttpServletResponse response) throws IOException {
        try {
            Map<String, Object> resultado = new HashMap<>();
            Map<String, Object> estadisticas = new HashMap<>();
            
            long totalProfesores = repositorio.obtenerMaterias().stream()
                .map(Materia::obtenerProfesor)
                .distinct()
                .count();
            
            int totalGrupos = repositorio.obtenerTodosLosGrupos().size();
            
            long totalMaterias = repositorio.obtenerMaterias().stream()
                .map(Materia::obtenerNombreMateria)
                .distinct()
                .count();
            
            double calificacionPromedio = repositorio.obtenerMaterias().stream()
                .mapToDouble(Materia::obtenerCalificacion)
                .average()
                .orElse(0.0);
            
            estadisticas.put("totalProfesores", totalProfesores);
            estadisticas.put("totalGrupos", totalGrupos);
            estadisticas.put("totalMaterias", totalMaterias);
            estadisticas.put("calificacionPromedio", Math.round(calificacionPromedio * 100.0) / 100.0);
            estadisticas.put("totalRegistros", repositorio.obtenerMaterias().size());
            
            resultado.put("success", true);
            resultado.put("estadisticas", estadisticas);
            
            escribirRespuesta(response, resultado);
            LOGGER.info("Estadísticas obtenidas exitosamente");
            
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error al obtener estadísticas", e);
            throw e;
        }
    }
    
    private void obtenerEstadisticasCache(HttpServletResponse response) throws IOException {
        try {
            Map<String, Object> resultado = new HashMap<>();
            resultado.put("success", true);
            resultado.put("cache", servicioHorarios.obtenerEstadisticasCache());
            
            escribirRespuesta(response, resultado);
            
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error al obtener estadísticas de cache", e);
            throw e;
        }
    }
    
    private void crearGrupo(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        String nombreGrupo = obtenerParametroSeguro(request, "nombreGrupo");
        String estrategiaParam = obtenerParametroSeguro(request, "estrategia");
        String algoritmoParam = obtenerParametroSeguro(request, "algoritmo");
        
        if (!validarNombreGrupo(nombreGrupo)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            escribirError(response, "Nombre de grupo inválido");
            return;
        }
        
        if (estrategiaParam == null || algoritmoParam == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            escribirError(response, "Parámetros de estrategia y algoritmo requeridos");
            return;
        }
        
        if (repositorio.obtenerTodosLosGrupos().contains(nombreGrupo)) {
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            escribirError(response, "El grupo ya existe: " + nombreGrupo);
            return;
        }
        
        long tiempoInicio = System.currentTimeMillis();
        
        try {
            EstrategiaEvaluacion estrategia = obtenerEstrategia(estrategiaParam);
            ConstructorHorario constructor = obtenerConstructor(algoritmoParam, nombreGrupo, estrategia);
            
            List<Seleccion> horario = constructor.construir();
            long tiempoTotal = System.currentTimeMillis() - tiempoInicio;
            
            Map<String, Object> resultado = new HashMap<>();
            resultado.put("success", true);
            resultado.put("nombreGrupo", nombreGrupo);
            resultado.put("estrategia", estrategiaParam);
            resultado.put("algoritmo", algoritmoParam);
            resultado.put("tiempoEjecucion", tiempoTotal);
            
            if (!horario.isEmpty()) {
                List<Materia> materias = horario.stream()
                    .map(Seleccion::asignada)
                    .collect(java.util.stream.Collectors.toList());
                
                double puntuacion = estrategia.evaluar(materias);
                
                List<Map<String, Object>> materiasInfo = horario.stream()
                    .map(this::convertirSeleccionAMapa)
                    .collect(java.util.stream.Collectors.toList());
                
                resultado.put("materias", materiasInfo);
                resultado.put("puntuacion", Math.round(puntuacion * 100.0) / 100.0);
                resultado.put("totalMaterias", horario.size());
                
                LOGGER.info("Grupo creado exitosamente: " + nombreGrupo + 
                           " (Materias: " + horario.size() + ", Tiempo: " + tiempoTotal + "ms)");
            } else {
                resultado.put("success", false);
                resultado.put("error", "No se pudo generar el horario con los parámetros dados");
                LOGGER.warning("No se pudo generar horario para grupo: " + nombreGrupo);
            }
            
            escribirRespuesta(response, resultado);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al crear grupo: " + nombreGrupo, e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            escribirError(response, "Error al crear grupo");
        }
    }
    
    private void crearGrupoConProfesoresFijados(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        String nombreGrupo = obtenerParametroSeguro(request, "nombreGrupo");
        String profesoresFijadosJson = obtenerParametroSeguro(request, "profesoresFijados");
        
        if (!validarNombreGrupo(nombreGrupo)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            escribirError(response, "Nombre de grupo inválido");
            return;
        }
        
        if (profesoresFijadosJson == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            escribirError(response, "Profesores fijados requeridos");
            return;
        }
        
        long tiempoInicio = System.currentTimeMillis();
        
        try {
            @SuppressWarnings("unchecked")
            Map<String, String> profesoresFijados = gson.fromJson(profesoresFijadosJson, Map.class);
            
            if (profesoresFijados == null || profesoresFijados.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                escribirError(response, "Debe especificar al menos un profesor fijado");
                return;
            }
            
            ConstructorHorario constructor = new ConstructorHorarioProfesoresFijados(
                repositorio, nombreGrupo, profesoresFijados);
            
            List<Seleccion> horario = constructor.construir();
            long tiempoTotal = System.currentTimeMillis() - tiempoInicio;
            
            Map<String, Object> resultado = new HashMap<>();
            resultado.put("success", true);
            resultado.put("nombreGrupo", nombreGrupo);
            resultado.put("profesoresFijados", profesoresFijados);
            resultado.put("tiempoEjecucion", tiempoTotal);
            
            if (!horario.isEmpty()) {
                List<Materia> materias = horario.stream()
                    .map(Seleccion::asignada)
                    .collect(java.util.stream.Collectors.toList());
                
                EstrategiaEvaluacion estrategia = new EstrategiaPonderadaPorMinutos();
                double puntuacion = estrategia.evaluar(materias);
                
                List<Map<String, Object>> materiasInfo = horario.stream()
                    .map(this::convertirSeleccionAMapa)
                    .collect(java.util.stream.Collectors.toList());
                
                resultado.put("materias", materiasInfo);
                resultado.put("puntuacion", Math.round(puntuacion * 100.0) / 100.0);
                resultado.put("totalMaterias", horario.size());
                
                LOGGER.info("Grupo con profesores fijados creado: " + nombreGrupo + 
                           " (Fijados: " + profesoresFijados.size() + ")");
            } else {
                resultado.put("success", false);
                resultado.put("error", "No se pudo generar el horario con las restricciones dadas");
            }
            
            escribirRespuesta(response, resultado);
            
        } catch (JsonSyntaxException e) {
            LOGGER.log(Level.WARNING, "JSON inválido para profesores fijados", e);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            escribirError(response, "Formato JSON inválido para profesores fijados");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al crear grupo con profesores fijados: " + nombreGrupo, e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            escribirError(response, "Error al crear grupo con profesores fijados");
        }
    }
    
    private void compararAlgoritmos(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        String nombreGrupo = obtenerParametroSeguro(request, "nombreGrupo");
        String algoritmosJson = obtenerParametroSeguro(request, "algoritmos");
        
        if (!validarNombreGrupo(nombreGrupo)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            escribirError(response, "Nombre de grupo inválido");
            return;
        }
        
        if (algoritmosJson == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            escribirError(response, "Lista de algoritmos requerida");
            return;
        }
        
        try {
            @SuppressWarnings("unchecked")
            List<String> algoritmos = gson.fromJson(algoritmosJson, List.class);
            
            if (algoritmos == null || algoritmos.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                escribirError(response, "Debe especificar al menos un algoritmo");
                return;
            }
            
            List<Map<String, Object>> resultados = new java.util.ArrayList<>();
            EstrategiaEvaluacion estrategia = new EstrategiaPonderadaPorMinutos();
            
            for (String algoritmo : algoritmos) {
                long tiempoInicio = System.currentTimeMillis();
                
                try {
                    ConstructorHorario constructor = obtenerConstructor(
                        algoritmo, nombreGrupo + "_" + algoritmo, estrategia);
                    List<Seleccion> horario = constructor.construir();
                    long tiempoTotal = System.currentTimeMillis() - tiempoInicio;
                    
                    Map<String, Object> resultado = new HashMap<>();
                    resultado.put("algoritmo", algoritmo);
                    resultado.put("nombre", obtenerNombreAlgoritmo(algoritmo));
                    resultado.put("tiempoEjecucion", tiempoTotal);
                    resultado.put("success", !horario.isEmpty());
                    
                    if (!horario.isEmpty()) {
                        List<Materia> materias = horario.stream()
                            .map(Seleccion::asignada)
                            .collect(java.util.stream.Collectors.toList());
                        
                        double puntuacion = estrategia.evaluar(materias);
                        resultado.put("puntuacion", Math.round(puntuacion * 100.0) / 100.0);
                        resultado.put("totalMaterias", horario.size());
                        
                        List<Map<String, Object>> materiasInfo = horario.stream()
                            .map(this::convertirSeleccionAMapa)
                            .collect(java.util.stream.Collectors.toList());
                        resultado.put("materias", materiasInfo);
                    } else {
                        resultado.put("puntuacion", 0.0);
                        resultado.put("totalMaterias", 0);
                        resultado.put("materias", new java.util.ArrayList<>());
                    }
                    
                    resultados.add(resultado);
                    
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Error en algoritmo " + algoritmo, e);
                    
                    Map<String, Object> resultado = new HashMap<>();
                    resultado.put("algoritmo", algoritmo);
                    resultado.put("nombre", obtenerNombreAlgoritmo(algoritmo));
                    resultado.put("tiempoEjecucion", System.currentTimeMillis() - tiempoInicio);
                    resultado.put("success", false);
                    resultado.put("error", e.getMessage());
                    resultado.put("puntuacion", 0.0);
                    resultado.put("totalMaterias", 0);
                    resultados.add(resultado);
                }
            }
            
            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("success", true);
            respuesta.put("resultados", resultados);
            respuesta.put("nombreGrupo", nombreGrupo);
            respuesta.put("totalAlgoritmos", resultados.size());
            
            escribirRespuesta(response, respuesta);
            LOGGER.info("Comparación completada para " + algoritmos.size() + " algoritmos");
            
        } catch (JsonSyntaxException e) {
            LOGGER.log(Level.WARNING, "JSON inválido para algoritmos", e);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            escribirError(response, "Formato JSON inválido para algoritmos");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error en comparación de algoritmos", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            escribirError(response, "Error en comparación de algoritmos");
        }
    }
    
    private void validarHorarios(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        try {
            programacion.horarios.validador.ValidadorHorario validador = 
                new programacion.horarios.validador.ValidadorHorario();
            
            var resultadoValidacion = validador.validar(repositorio.obtenerMaterias());
            
            Map<String, Object> resultado = new HashMap<>();
            resultado.put("success", true);
            resultado.put("valido", resultadoValidacion.esValido());
            resultado.put("totalMaterias", repositorio.obtenerMaterias().size());
            
            if (!resultadoValidacion.esValido()) {
                resultado.put("conflictos", resultadoValidacion.obtenerConflictos());
                resultado.put("totalConflictos", resultadoValidacion.obtenerConflictos().size());
            }
            
            escribirRespuesta(response, resultado);
            LOGGER.info("Validación completada - Válido: " + resultadoValidacion.esValido());
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error en validación de horarios", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            escribirError(response, "Error en validación");
        }
    }
    
    // Métodos auxiliares
    
    private EstrategiaEvaluacion obtenerEstrategia(String estrategia) {
        if (estrategia == null) return new EstrategiaPonderadaPorMinutos();
        
        switch (estrategia.toLowerCase()) {
            case "simple":
                return new EstrategiaPromedioSimple();
            case "weighted":
                return new EstrategiaPonderadaPorMinutos();
            case "maxmin":
                return new EstrategiaPromedioMaxMin();
            case "harmonic":
                return new EstrategiaMediaArmonica();
            default:
                LOGGER.warning("Estrategia desconocida: " + estrategia + ", usando ponderada por defecto");
                return new EstrategiaPonderadaPorMinutos();
        }
    }
    
    private ConstructorHorario obtenerConstructor(String algoritmo, String nombreGrupo, 
                                                EstrategiaEvaluacion estrategia) {
        if (algoritmo == null) {
            LOGGER.warning("Algoritmo nulo, usando maxcoverage por defecto");
            algoritmo = "maxcoverage";
        }
        
        switch (algoritmo.toLowerCase()) {
            case "maxcoverage":
                return new ConstructorHorarioMaximaCobertura(repositorio, nombreGrupo, estrategia);
            case "optimized":
                return new ConstructorHorarioOptimizado(repositorio, nombreGrupo);
            case "astar":
                return new ConstructorHorarioAStar(repositorio, nombreGrupo);
            default:
                LOGGER.warning("Algoritmo desconocido: " + algoritmo + ", usando maxcoverage por defecto");
                return new ConstructorHorarioMaximaCobertura(repositorio, nombreGrupo, estrategia);
        }
    }
    
    private String obtenerNombreAlgoritmo(String algoritmo) {
        if (algoritmo == null) return "Algoritmo Desconocido";
        
        switch (algoritmo.toLowerCase()) {
            case "maxcoverage":
                return "Máxima Cobertura (Backtracking)";
            case "optimized":
                return "Algoritmo Voraz Optimizado";
            case "astar":
                return "A* Heurístico";
            default:
                return "Algoritmo: " + algoritmo;
        }
    }
    
    private Map<String, Object> convertirMateriaAMapa(Materia materia) {
        if (materia == null) return new HashMap<>();
        
        Map<String, Object> mapa = new HashMap<>();
        mapa.put("subject", materia.obtenerNombreMateria());
        mapa.put("professor", materia.obtenerProfesor().obtenerNombreCompleto());
        mapa.put("rating", materia.obtenerCalificacion());
        mapa.put("minutes", materia.obtenerMinutosTotales());
        mapa.put("schedule", obtenerHorarioTexto(materia));
        return mapa;
    }
    
    private Map<String, Object> convertirSeleccionAMapa(Seleccion seleccion) {
        if (seleccion == null) return new HashMap<>();
        
        Map<String, Object> mapa = convertirMateriaAMapa(seleccion.asignada());
        mapa.put("origin", seleccion.grupoOrigen() != null ? seleccion.grupoOrigen() : "Asignación automática");
        return mapa;
    }
    
    private Map<String, Object> convertirProfesorAMapa(Profesor profesor) {
        if (profesor == null) return new HashMap<>();
        
        Map<String, Object> mapa = new HashMap<>();
        mapa.put("name", profesor.obtenerNombreCompleto());
        mapa.put("rating", profesor.obtenerCalificacion());
        
        // Obtener materias que imparte este profesor
        List<String> materias = repositorio.obtenerMaterias().stream()
            .filter(m -> m.obtenerProfesor().equals(profesor))
            .map(Materia::obtenerNombreMateria)
            .distinct()
            .sorted()
            .collect(java.util.stream.Collectors.toList());
        
        mapa.put("subjects", materias);
        mapa.put("totalSubjects", materias.size());
        return mapa;
    }
    
    private String obtenerHorarioTexto(Materia materia) {
        try {
            Map<Integer, List<FranjaHoraria>> horarioMap = materia.obtenerHorario();
            
            if (horarioMap != null && !horarioMap.isEmpty()) {
                StringBuilder horario = new StringBuilder();
                
                for (Map.Entry<Integer, List<FranjaHoraria>> entrada : horarioMap.entrySet()) {
                    Integer codigoDia = entrada.getKey();
                    List<FranjaHoraria> franjas = entrada.getValue();
                    
                    if (!franjas.isEmpty()) {
                        if (horario.length() > 0) horario.append(" | ");
                        
                        String nombreDia = obtenerNombreDia(codigoDia);
                        horario.append(nombreDia).append(": ");
                        
                        for (int i = 0; i < franjas.size(); i++) {
                            if (i > 0) horario.append(", ");
                            horario.append(franjas.get(i).formatear());
                        }
                    }
                }
                
                return horario.length() > 0 ? horario.toString() : "Sin horario definido";
            }
            return "Horario por asignar";
        } catch (Exception e) {
            LOGGER.log(Level.FINE, "Error al obtener horario de texto para materia: " + 
                      materia.obtenerNombreMateria(), e);
            return "Horario no disponible";
        }
    }
    
    private String obtenerNombreDia(Integer codigo) {
        switch (codigo) {
            case 1: return "Lun";
            case 2: return "Mar";
            case 3: return "Mié";
            case 4: return "Jue";
            case 5: return "Vie";
            case 6: return "Sáb";
            case 7: return "Dom";
            default: return "Día " + codigo;
        }
    }
    
    private void escribirRespuesta(HttpServletResponse response, Object objeto) throws IOException {
        if (objeto == null) {
            objeto = Map.of("success", false, "error", "Respuesta nula");
        }
        
        try (PrintWriter out = response.getWriter()) {
            out.print(gson.toJson(objeto));
            out.flush();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al escribir respuesta JSON", e);
            throw new IOException("Error al generar respuesta JSON", e);
        }
    }
    
    private void escribirError(HttpServletResponse response, String mensaje) throws IOException {
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("error", mensaje != null ? mensaje : "Error desconocido");
        error.put("timestamp", System.currentTimeMillis());
        
        escribirRespuesta(response, error);
    }
    
    @Override
    public void destroy() {
        LOGGER.info("Destruyendo HorariosServlet");
        try {
            if (servicioHorarios != null) {
                LOGGER.info("Limpiando recursos del servicio de horarios");
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error al limpiar recursos en destroy()", e);
        } finally {
            super.destroy();
        }
    }
    
    public Map<String, Object> obtenerEstadoServlet() {
        Map<String, Object> estado = new HashMap<>();
        estado.put("repositorioInicializado", repositorio != null);
        estado.put("servicioInicializado", servicioHorarios != null);
        estado.put("gsonInicializado", gson != null);
        
        if (repositorio != null) {
            estado.put("totalMateriasCargadas", repositorio.obtenerMaterias().size());
            estado.put("totalGrupos", repositorio.obtenerTodosLosGrupos().size());
        }
        
        return estado;
    }
    
    private Map<String, Object> obtenerInformacionSistema() {
        Map<String, Object> info = new HashMap<>();
        info.put("version", "1.0.0");
        info.put("timestamp", System.currentTimeMillis());
        info.put("javaVersion", System.getProperty("java.version"));
        info.put("servidor", getServletContext().getServerInfo());
        return info;
    }
    
    private void debugMateriaHorario(Materia materia) {
        System.out.println("=== DEBUG MATERIA ===");
        System.out.println("Nombre: " + materia.obtenerNombreMateria());
        System.out.println("Profesor: " + materia.obtenerProfesor().obtenerNombreCompleto());
        System.out.println("Grupo: " + materia.obtenerGrupo());

        Map<Integer, List<FranjaHoraria>> horario = materia.obtenerHorario();
        if (horario != null) {
            for (Map.Entry<Integer, List<FranjaHoraria>> entrada : horario.entrySet()) {
                Integer dia = entrada.getKey();
                List<FranjaHoraria> franjas = entrada.getValue();
                System.out.println("Día " + dia + ":");

                for (FranjaHoraria franja : franjas) {
                    int inicio = franja.obtenerHoraInicio();
                    int fin = franja.obtenerHoraFin();

                    System.out.println("  - Inicio RAW: " + inicio);
                    System.out.println("  - Fin RAW: " + fin);
                    System.out.println("  - Inicio convertido: " + formatearHora(inicio));
                    System.out.println("  - Fin convertido: " + formatearHora(fin));
                    System.out.println("  - Duración: " + franja.obtenerDuracionMinutos() + " min");
                    System.out.println("  ---");
                }
            }
        } else {
            System.out.println("Horario NULL");
        }
        System.out.println("==================");
    }
    
    private int convertirHHMMaMinutos(String horaHHMM) {
        try {

            String hora = horaHHMM;
            if (hora.length() == 3) {
                hora = "0" + hora;
            }

            if (hora.length() != 4) {
                LOGGER.warning("Formato de hora inválido: " + horaHHMM);
                return 0;
            }

            int horas = Integer.parseInt(hora.substring(0, 2));
            int minutos = Integer.parseInt(hora.substring(2, 4));

            int totalMinutos = (horas * 60) + minutos;

            LOGGER.info("Conversión: " + horaHHMM + " -> " + horas + ":" + minutos + " -> " + totalMinutos + " minutos");

            return totalMinutos;

        } catch (NumberFormatException e) {
            LOGGER.severe("Error parseando hora: " + horaHHMM + " - " + e.getMessage());
            return 0;
        }
    }

    @SuppressWarnings("unused")
    private void obtenerInformacionSistema(HttpServletResponse response) throws IOException {
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("success", true);
        resultado.put("sistema", obtenerInformacionSistema());
        resultado.put("estado", obtenerEstadoServlet());
        
        escribirRespuesta(response, resultado);
    }
}
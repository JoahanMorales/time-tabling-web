<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*" %>
<%@ page import="programacion.horarios.fabrica.FabricaDatos" %>
<%@ page import="programacion.horarios.nucleo.*" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%
    request.setCharacterEncoding("UTF-8");  
    RepositorioMaterias repo = FabricaDatos.crearRepositorioPorDefecto();

    // Materias únicas
    Set<String> materiasUnicas = new HashSet<>();
    for (Materia m : repo.obtenerMaterias()) {
        materiasUnicas.add(m.obtenerNombreMateria());
    }
    request.setAttribute("materiasUnicas", materiasUnicas);

    // Profesores únicos
    Set<Profesor> profesoresUnicos = new HashSet<>();
    for (Materia m : repo.obtenerMaterias()) {
        profesoresUnicos.add(m.obtenerProfesor());
    }
    request.setAttribute("profesoresUnicos", profesoresUnicos);
%>

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Timer</title>
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css" rel="stylesheet">
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.4/css/all.min.css">
    
    <!-- Incluir Google Fonts para Poppins -->
    <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@400;600&display=swap" rel="stylesheet">
    
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        :root {
            --primary: #8b5cf6;
            --primary-dark: #7c3aed;
            --primary-light: #a78bfa;
            --primary-ultra-light: #ede9fe;
            --accent: #6366f1;
            --text-primary: #0f172a;
            --text-secondary: #64748b;
            --text-tertiary: #94a3b8;
            --bg-primary: #ffffff;
            --bg-secondary: #f8fafc;
            --bg-tertiary: #f1f5f9;
            --border: #e2e8f0;
            --border-light: #f1f5f9;
            --shadow-sm: 0 1px 2px 0 rgba(0, 0, 0, 0.05);
            --shadow-md: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
            --shadow-lg: 0 10px 15px -3px rgba(0, 0, 0, 0.1);
            --shadow-primary: 0 4px 14px 0 rgba(139, 92, 246, 0.2);
        }

        body {
            font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
            background: linear-gradient(to bottom, #faf5ff 0%, #f8fafc 100%);
            color: var(--text-primary);
            line-height: 1.6;
            min-height: 100vh;
            -webkit-font-smoothing: antialiased;
            -moz-osx-font-smoothing: grayscale;
        }

        .container {
            max-width: 1280px;
            margin: 0 auto;
            padding: 0 24px;
        }

        .header {
            background: linear-gradient(135deg, var(--primary) 0%, var(--accent) 100%);
            padding: 48px 0;
            margin-bottom: 40px;
            position: relative;
            overflow: hidden;
        }

        .header::before {
            content: '';
            position: absolute;
            top: 0;
            left: 0;
            right: 0;
            bottom: 0;
            background: url("data:image/svg+xml,%3Csvg width='60' height='60' viewBox='0 0 60 60' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='none' fill-rule='evenodd'%3E%3Cg fill='%23ffffff' fill-opacity='0.05'%3E%3Cpath d='M36 34v-4h-2v4h-4v2h4v4h2v-4h4v-2h-4zm0-30V0h-2v4h-4v2h4v4h2V6h4V4h-4zM6 34v-4H4v4H0v2h4v4h2v-4h4v-2H6zM6 4V0H4v4H0v2h4v4h2V6h4V4H6z'/%3E%3C/g%3E%3C/g%3E%3C/svg%3E");
            opacity: 0.5;
        }

        .header-content {
            position: relative;
            z-index: 1;
        }

        .header h1 {
            font-size: 32px;
            font-weight: 700;
            color: white;
            margin-bottom: 8px;
            letter-spacing: -0.02em;
        }

        .header p {
            color: rgba(255, 255, 255, 0.9);
            font-size: 16px;
            font-weight: 400;
        }

        .main-content {
            padding-bottom: 60px;
        }

        .tabs-wrapper {
            margin-bottom: 32px;
            background: var(--bg-primary);
            border-radius: 12px;
            padding: 6px;
            box-shadow: var(--shadow-sm);
            border: 1px solid var(--border-light);
        }

        .tabs {
            display: flex;
            gap: 4px;
            overflow-x: auto;
            -webkit-overflow-scrolling: touch;
        }

        .tabs::-webkit-scrollbar {
            height: 4px;
        }

        .tabs::-webkit-scrollbar-track {
            background: transparent;
        }

        .tabs::-webkit-scrollbar-thumb {
            background: var(--border);
            border-radius: 4px;
        }

        .tab {
            flex: 1;
            min-width: fit-content;
            padding: 12px 20px;
            text-align: center;
            border-radius: 8px;
            cursor: pointer;
            transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
            font-weight: 500;
            font-size: 14px;
            color: var(--text-secondary);
            background: transparent;
            border: none;
            white-space: nowrap;
            position: relative;
        }

        .tab i {
            margin-right: 6px;
            font-size: 14px;
        }

        .tab:hover:not(.active) {
            background: var(--bg-tertiary);
            color: var(--text-primary);
            transform: translateY(-1px);
        }

        .tab.active {
            background: linear-gradient(135deg, var(--primary) 0%, var(--accent) 100%);
            color: white;
            box-shadow: var(--shadow-primary);
        }

        .content {
            background: var(--bg-primary);
            border-radius: 16px;
            padding: 40px;
            box-shadow: var(--shadow-md);
            border: 1px solid var(--border-light);
        }

        .tab-content {
            display: none;
        }

        .tab-content.active {
            display: block;
            animation: fadeInUp 0.4s cubic-bezier(0.4, 0, 0.2, 1);
        }

        @keyframes fadeInUp {
            from { 
                opacity: 0; 
                transform: translateY(12px);
            }
            to { 
                opacity: 1; 
                transform: translateY(0);
            }
        }

        .form-section {
            margin-bottom: 40px;
        }

        .form-section:last-child {
            margin-bottom: 0;
        }

        .section-header {
            display: flex;
            align-items: center;
            gap: 12px;
            margin-bottom: 28px;
            padding-bottom: 16px;
            border-bottom: 2px solid var(--border-light);
        }

        .section-header i {
            color: var(--primary);
            font-size: 20px;
        }

        .section-header h3 {
            font-size: 20px;
            font-weight: 600;
            color: var(--text-primary);
            letter-spacing: -0.01em;
        }

        .form-group {
            margin-bottom: 20px;
        }

        .form-label {
            display: flex;
            align-items: center;
            gap: 8px;
            font-weight: 500;
            color: var(--text-primary);
            font-size: 14px;
            margin-bottom: 8px;
        }

        .form-label i {
            color: var(--text-tertiary);
            font-size: 14px;
        }

        .form-control {
            width: 100%;
            padding: 12px 16px;
            border: 1.5px solid var(--border);
            border-radius: 10px;
            font-size: 14px;
            transition: all 0.2s ease;
            background: var(--bg-primary);
            color: var(--text-primary);
            font-family: inherit;
        }

        .form-control:hover {
            border-color: var(--primary-light);
        }

        .form-control:focus {
            outline: none;
            border-color: var(--primary);
            box-shadow: 0 0 0 3px var(--primary-ultra-light);
        }

        .form-control::placeholder {
            color: var(--text-tertiary);
        }

        .btn {
            padding: 12px 24px;
            border: none;
            border-radius: 10px;
            font-size: 14px;
            font-weight: 600;
            cursor: pointer;
            transition: all 0.2s ease;
            display: inline-flex;
            align-items: center;
            justify-content: center;
            gap: 8px;
            font-family: inherit;
            text-decoration: none;
        }

        .btn i {
            font-size: 14px;
        }

        .btn:active {
            transform: scale(0.98);
        }

        .btn-primary {
            background: linear-gradient(135deg, var(--primary) 0%, var(--accent) 100%);
            color: white;
            box-shadow: var(--shadow-primary);
        }

        .btn-primary:hover {
            transform: translateY(-2px);
            box-shadow: 0 6px 20px 0 rgba(139, 92, 246, 0.35);
        }

        .btn-secondary {
            background: var(--bg-secondary);
            color: var(--text-primary);
            border: 1.5px solid var(--border);
        }

        .btn-secondary:hover {
            background: var(--bg-tertiary);
            border-color: var(--primary-light);
        }

        .btn-success {
            background: linear-gradient(135deg, #10b981 0%, #059669 100%);
            color: white;
            box-shadow: 0 4px 14px 0 rgba(16, 185, 129, 0.2);
        }

        .btn-success:hover {
            transform: translateY(-2px);
            box-shadow: 0 6px 20px 0 rgba(16, 185, 129, 0.35);
        }

        .btn-danger {
            background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
            color: white;
            box-shadow: 0 4px 14px 0 rgba(239, 68, 68, 0.2);
        }

        .btn-danger:hover {
            transform: translateY(-2px);
            box-shadow: 0 6px 20px 0 rgba(239, 68, 68, 0.35);
        }

        .loading {
            display: none;
            text-align: center;
            padding: 60px 20px;
        }

        .loading.active {
            display: block;
        }

        .spinner {
            border: 3px solid var(--border-light);
            border-top: 3px solid var(--primary);
            border-radius: 50%;
            width: 48px;
            height: 48px;
            animation: spin 0.8s linear infinite;
            margin: 0 auto 20px;
        }

        @keyframes spin {
            0% { transform: rotate(0deg); }
            100% { transform: rotate(360deg); }
        }

        .loading p {
            color: var(--text-secondary);
            font-size: 15px;
        }

        .alert {
            padding: 16px 20px;
            border-radius: 10px;
            margin-bottom: 24px;
            font-size: 14px;
            border: 1.5px solid;
            display: flex;
            align-items: flex-start;
            gap: 12px;
        }

        .alert i {
            font-size: 18px;
            margin-top: 1px;
        }

        .alert-success {
            background: linear-gradient(135deg, #f0fdf4 0%, #dcfce7 100%);
            color: #166534;
            border-color: #86efac;
        }

        .alert-error {
            background: linear-gradient(135deg, #fef2f2 0%, #fee2e2 100%);
            color: #991b1b;
            border-color: #fca5a5;
        }

        .alert-warning {
            background: linear-gradient(135deg, #fffbeb 0%, #fef3c7 100%);
            color: #92400e;
            border-color: #fde047;
        }

        .alert-info {
            background: linear-gradient(135deg, #eff6ff 0%, #dbeafe 100%);
            color: #1e40af;
            border-color: #93c5fd;
        }

        .existing-groups {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
            gap: 20px;
        }

        .group-card {
            background: linear-gradient(135deg, var(--bg-primary) 0%, var(--bg-secondary) 100%);
            border-radius: 12px;
            padding: 24px;
            border: 1.5px solid var(--border-light);
            transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
            cursor: pointer;
        }

        .group-card:hover {
            border-color: var(--primary-light);
            box-shadow: var(--shadow-lg);
            transform: translateY(-4px);
        }

        .group-card-header {
            display: flex;
            align-items: center;
            justify-content: space-between;
            margin-bottom: 20px;
            padding-bottom: 16px;
            border-bottom: 1.5px solid var(--border-light);
        }

        .group-card h4 {
            color: var(--primary);
            font-size: 18px;
            font-weight: 600;
            letter-spacing: -0.01em;
        }

        .group-badge {
            background: var(--primary-ultra-light);
            color: var(--primary-dark);
            padding: 4px 12px;
            border-radius: 20px;
            font-size: 12px;
            font-weight: 600;
        }

        .subject-list {
            list-style: none;
        }

        .subject-item {
            padding: 12px 0;
            border-bottom: 1px solid var(--border-light);
            transition: all 0.2s ease;
        }

        .subject-item:last-child {
            border-bottom: none;
        }

        .subject-item:hover {
            padding-left: 8px;
        }

        .subject-name {
            font-weight: 600;
            color: var(--text-primary);
            display: block;
            margin-bottom: 4px;
            font-size: 14px;
        }

        .professor-info {
            display: flex;
            align-items: center;
            justify-content: space-between;
            gap: 8px;
        }

        .professor-name {
            color: var(--text-secondary);
            font-size: 13px;
        }

        .rating {
            background: linear-gradient(135deg, var(--primary) 0%, var(--accent) 100%);
            color: white;
            padding: 3px 10px;
            border-radius: 6px;
            font-size: 12px;
            font-weight: 600;
            display: inline-flex;
            align-items: center;
            gap: 4px;
        }

        .rating i {
            font-size: 10px;
        }

        .professor-assignment {
            display: grid;
            grid-template-columns: 1fr 1fr auto;
            gap: 12px;
            margin-bottom: 12px;
            align-items: center;
        }

        .fixed-section {
            background: linear-gradient(135deg, #faf5ff 0%, #f3e8ff 100%);
            border: 2px dashed var(--primary-light);
            border-radius: 12px;
            padding: 28px;
            margin: 28px 0;
        }

        .fixed-section-header {
            display: flex;
            align-items: center;
            gap: 10px;
            margin-bottom: 24px;
        }

        .fixed-section-header i {
            color: var(--primary);
            font-size: 18px;
        }

        .fixed-section h4 {
            font-size: 16px;
            font-weight: 600;
            color: var(--text-primary);
        }

        .modal-overlay {
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background: rgba(0, 0, 0, 0.6);
            backdrop-filter: blur(4px);
            z-index: 1000;
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 20px;
            animation: fadeIn 0.2s ease;
        }

        @keyframes fadeIn {
            from { opacity: 0; }
            to { opacity: 1; }
        }

        .modal-content {
            background: white;
            border-radius: 16px;
            padding: 36px;
            max-width: 960px;
            width: 100%;
            max-height: 90vh;
            overflow-y: auto;
            position: relative;
            box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
            animation: slideUp 0.3s cubic-bezier(0.4, 0, 0.2, 1);
        }

        @keyframes slideUp {
            from { 
                opacity: 0;
                transform: translateY(20px);
            }
            to { 
                opacity: 1;
                transform: translateY(0);
            }
        }

        .modal-close {
            position: absolute;
            top: 24px;
            right: 24px;
            background: var(--bg-secondary);
            border: 1.5px solid var(--border);
            font-size: 20px;
            cursor: pointer;
            color: var(--text-secondary);
            width: 36px;
            height: 36px;
            display: flex;
            align-items: center;
            justify-content: center;
            border-radius: 8px;
            transition: all 0.2s ease;
        }

        .modal-close:hover {
            background: var(--primary);
            color: white;
            border-color: var(--primary);
            transform: rotate(90deg);
        }

        .schedule-table {
            width: 100%;
            border-collapse: separate;
            border-spacing: 0;
            margin-top: 24px;
            background: white;
            border-radius: 12px;
            overflow: hidden;
            border: 1.5px solid var(--border-light);
            box-shadow: var(--shadow-sm);
        }

        .schedule-table th {
            background: linear-gradient(135deg, var(--bg-secondary) 0%, var(--bg-tertiary) 100%);
            color: var(--text-primary);
            padding: 14px 12px;
            text-align: center;
            font-weight: 600;
            font-size: 13px;
            border-bottom: 1.5px solid var(--border);
            text-transform: uppercase;
            letter-spacing: 0.05em;
        }

        .schedule-table td {
            padding: 10px;
            text-align: center;
            border: 1px solid var(--border-light);
            vertical-align: middle;
            font-size: 13px;
        }

        .schedule-subject {
            background: linear-gradient(135deg, #faf5ff 0%, #f3e8ff 100%);
            border-radius: 8px;
            padding: 10px;
            margin: 3px;
            border-left: 3px solid var(--primary);
            transition: all 0.2s ease;
        }

        .schedule-subject:hover {
            transform: scale(1.02);
            box-shadow: var(--shadow-md);
        }

        .schedule-subject-name {
            font-weight: 600;
            color: var(--text-primary);
            display: block;
            margin-bottom: 3px;
            font-size: 13px;
        }

        .schedule-professor {
            font-size: 12px;
            color: var(--text-secondary);
        }

        .results-section {
            margin-top: 40px;
        }

        .schedule-container {
            background: linear-gradient(135deg, var(--bg-secondary) 0%, var(--bg-tertiary) 100%);
            border-radius: 12px;
            padding: 28px;
            margin-top: 24px;
            border: 1.5px solid var(--border-light);
        }

        .schedule-container h4 {
            font-size: 18px;
            font-weight: 600;
            color: var(--text-primary);
            margin-bottom: 20px;
            display: flex;
            align-items: center;
            gap: 10px;
        }

        .schedule-container h4 i {
            color: var(--primary);
        }

        .checkbox-group {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
            gap: 14px;
            margin-top: 14px;
        }

        .checkbox-label {
            display: flex;
            align-items: center;
            gap: 10px;
            font-size: 14px;
            color: var(--text-primary);
            cursor: pointer;
            padding: 12px 16px;
            border-radius: 8px;
            border: 1.5px solid var(--border);
            background: var(--bg-primary);
            transition: all 0.2s ease;
        }

        .checkbox-label:hover {
            border-color: var(--primary-light);
            background: var(--primary-ultra-light);
        }

        .checkbox-label input[type="checkbox"] {
            width: 18px;
            height: 18px;
            cursor: pointer;
            accent-color: var(--primary);
        }

        .data-actions {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 14px;
            margin-bottom: 32px;
        }

        .empty-state {
            text-align: center;
            padding: 60px 20px;
        }

        .empty-state i {
            font-size: 64px;
            color: var(--text-tertiary);
            margin-bottom: 20px;
            opacity: 0.5;
        }

        .empty-state h3 {
            font-size: 20px;
            font-weight: 600;
            color: var(--text-primary);
            margin-bottom: 8px;
        }

        .empty-state p {
            color: var(--text-secondary);
            font-size: 15px;
        }

        @media (max-width: 768px) {
            .container {
                padding: 0 16px;
            }

            .header {
                padding: 32px 0;
            }

            .header h1 {
                font-size: 24px;
            }

            .header p {
                font-size: 14px;
            }

            .content {
                padding: 24px 20px;
            }

            .tabs {
                flex-direction: column;
            }

            .tab {
                width: 100%;
            }

            .existing-groups {
                grid-template-columns: 1fr;
            }

            .professor-assignment {
                grid-template-columns: 1fr;
            }

            .modal-content {
                padding: 24px;
            }

            .schedule-table {
                font-size: 11px;
            }

            .schedule-table th,
            .schedule-table td {
                padding: 8px 4px;
            }

            .checkbox-group {
                grid-template-columns: 1fr;
            }

            .data-actions {
                grid-template-columns: 1fr;
            }
        }

        ::-webkit-scrollbar {
            width: 10px;
            height: 10px;
        }

        ::-webkit-scrollbar-track {
            background: var(--bg-secondary);
        }

        ::-webkit-scrollbar-thumb {
            background: var(--border);
            border-radius: 5px;
        }

        ::-webkit-scrollbar-thumb:hover {
            background: var(--primary-light);
        }
        
         /* Estilo para el logo */
        .header-content h1 {
            font-family: 'Poppins', sans-serif;
            font-size: 3em;
            font-weight: 600;
            color: #000000;
            display: flex;
            align-items: center;
        }

        .header-content h1 i {
            font-size: 1.3em; /* Ajustar tamaño del icono */
            margin-right: 0em; /* Espacio entre el icono y el texto */
            color: #ffffff; /* Color morado para el reloj */
        }

        .header-content p {
            font-family: 'Poppins', sans-serif;
            font-weight: 400;
            color: #ffffff;
        }
    </style>
    
    
<%
    for (int i = 0; i < 10; i++) {
%>
        <div>
            <h1>Este h1 se repetira 10 veces</h1>
        </div>
<%
}
%>
    
</head>
<body>
    <div class="header">
       <div class="container header-content">
           <h1><span>Tim</span><i class="fas fa-clock" style="color: #ffffff;"></i><span>r</span></h1>
            <p>Gestión académica inteligente con algoritmos optimizados</p>
        </div>
    </div>
    <div class="container main-content">
        <div class="tabs-wrapper">
            <div class="tabs">
                <button class="tab active" onclick="showTab('existing')">
                    <i class="fas fa-users"></i> Grupos Existentes
                </button>
                <button class="tab" onclick="showTab('create')">
                    <i class="fas fa-plus-circle"></i> Crear Grupo
                </button>
                <button class="tab" onclick="showTab('fixed')">
                    <i class="fas fa-user-tie"></i> Profesores Fijados
                </button>
                <button class="tab" onclick="showTab('compare')">
                    <i class="fas fa-chart-bar"></i> Comparar Algoritmos
                </button>
                <button class="tab" onclick="showTab('manage')">
                    <i class="fas fa-cog"></i> Gestionar
                </button>
            </div>
        </div>

        <div class="content">
            <!-- Tab: Grupos Existentes -->
            <div id="existing-tab" class="tab-content active">
                <div class="form-section">
                    <div class="section-header">
                        <i class="fas fa-list"></i>
                        <h3>Grupos Existentes</h3>
                    </div>
                    <div class="existing-groups" id="existing-groups">
                        <div id="groups-loading" class="loading">
                            <div class="spinner"></div>
                            <p>Cargando grupos...</p>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Tab: Crear Nuevo Grupo -->
            <div id="create-tab" class="tab-content">
                <div class="form-section">
                    <div class="section-header">
                        <i class="fas fa-plus"></i>
                        <h3>Crear Nuevo Grupo</h3>
                    </div>
                    <form id="create-group-form">
                        <div class="form-group">
                            <label class="form-label">
                                <i class="fas fa-tag"></i> Nombre del Grupo
                            </label>
                            <input type="text" id="group-name" class="form-control" placeholder="Ej: 2AMX" required>
                        </div>

                        <div class="form-group">
                            <label class="form-label">
                                <i class="fas fa-calculator"></i> Estrategia de Evaluación
                            </label>
                            <select id="evaluation-strategy" class="form-control">
                                <option value="simple">Promedio Simple</option>
                                <option value="weighted" selected>Ponderada por Minutos</option>
                                <option value="maxmin">Promedio Max-Min</option>
                                <option value="harmonic">Media Armónica</option>
                            </select>
                        </div>

                        <div class="form-group">
                            <label class="form-label">
                                <i class="fas fa-microchip"></i> Algoritmo
                            </label>
                            <select id="algorithm-type" class="form-control">
                                <option value="maxcoverage">Máxima Cobertura (Backtracking)</option>
                                <option value="optimized">Algoritmo Voraz Optimizado</option>
                                <option value="astar">A* Heurístico</option>
                            </select>
                        </div>

                        <button type="submit" class="btn btn-primary" style="margin-top: 8px;">
                            <i class="fas fa-play"></i> Crear Grupo
                        </button>
                    </form>
                </div>

                <div class="loading" id="create-loading">
                    <div class="spinner"></div>
                    <p>Creando grupo con el algoritmo seleccionado...</p>
                </div>

                <div class="results-section" id="create-results"></div>
            </div>

            <!-- Tab: Profesores Fijados -->
            <div id="fixed-tab" class="tab-content">
                <div class="form-section">
                    <div class="section-header">
                        <i class="fas fa-user-lock"></i>
                        <h3>Crear Grupo con Profesores Fijados</h3>
                    </div>
                    <form id="fixed-professors-form">
                        <div class="form-group">
                            <label class="form-label">
                                <i class="fas fa-tag"></i> Nombre del Grupo
                            </label>
                            <input type="text" id="fixed-group-name" class="form-control" placeholder="Ej: GRUPO_ESPECIAL" required>
                        </div>

                        <div class="fixed-section">
                            <div class="fixed-section-header">
                                <i class="fas fa-lock"></i>
                                <h4>Asignaciones Obligatorias</h4>
                            </div>
                            <div id="professor-assignments">
                                <div class="professor-assignment">
                                    <select class="form-control subject-select">
                                        <option value="">Seleccionar materia...</option>
                                        <c:forEach var="materia" items="${materiasUnicas}">
                                            <option value="<c:out value='${materia}'/>"><c:out value="${materia}"/></option>
                                        </c:forEach>
                                    </select>
                                    <select class="form-control professor-select">
                                        <option value="">Seleccionar profesor...</option>
                                        <c:forEach var="profesor" items="${profesoresUnicos}">
                                            <option value="<c:out value='${profesor.obtenerNombreCompleto()}'/>">
                                                <c:out value="${profesor.obtenerNombreCompleto()}"/> (${profesor.obtenerCalificacion()})
                                            </option>
                                        </c:forEach>
                                    </select>
                                    <button type="button" class="btn btn-danger" onclick="removeAssignment(this)">
                                        <i class="fas fa-times"></i>
                                    </button>
                                </div>
                            </div>
                            <button type="button" class="btn btn-secondary" onclick="addAssignment()" style="margin-top: 12px;">
                                <i class="fas fa-plus"></i> Agregar Asignación
                            </button>
                        </div>

                        <button type="submit" class="btn btn-primary" style="margin-top: 8px;">
                            <i class="fas fa-lock"></i> Crear con Restricciones
                        </button>
                    </form>
                </div>

                <div class="loading" id="fixed-loading">
                    <div class="spinner"></div>
                    <p>Creando grupo con profesores fijados...</p>
                </div>

                <div class="results-section" id="fixed-results"></div>
            </div>

            <!-- Tab: Comparar Algoritmos -->
            <div id="compare-tab" class="tab-content">
                <div class="form-section">
                    <div class="section-header">
                        <i class="fas fa-chart-line"></i>
                        <h3>Comparación de Algoritmos</h3>
                    </div>
                    <form id="compare-form">
                        <div class="form-group">
                            <label class="form-label">
                                <i class="fas fa-tag"></i> Nombre del Grupo de Prueba
                            </label>
                            <input type="text" id="compare-group-name" class="form-control" value="COMPARACION" required>
                        </div>

                        <div class="form-group">
                            <label class="form-label">
                                <i class="fas fa-check-square"></i> Algoritmos a Comparar
                            </label>
                            <div class="checkbox-group">
                                <label class="checkbox-label">
                                    <input type="checkbox" name="algorithms" value="maxcoverage" checked>
                                    <span>Backtracking (Máxima Cobertura)</span>
                                </label>
                                <label class="checkbox-label">
                                    <input type="checkbox" name="algorithms" value="optimized" checked>
                                    <span>Algoritmo Voraz Optimizado</span>
                                </label>
                                <label class="checkbox-label">
                                    <input type="checkbox" name="algorithms" value="astar" checked>
                                    <span>A* Heurístico</span>
                                </label>
                            </div>
                        </div>

                        <button type="submit" class="btn btn-primary" style="margin-top: 8px;">
                            <i class="fas fa-play"></i> Ejecutar Comparación
                        </button>
                    </form>
                </div>

                <div class="loading" id="compare-loading">
                    <div class="spinner"></div>
                    <p>Ejecutando comparación de algoritmos...</p>
                </div>

                <div class="results-section" id="compare-results"></div>
            </div>

            <!-- Tab: Gestionar Datos -->
            <div id="manage-tab" class="tab-content">
                <div class="form-section">
                    <div class="section-header">
                        <i class="fas fa-database"></i>
                        <h3>Gestión de Datos</h3>
                    </div>

                    <div class="data-actions">
                        <button class="btn btn-primary" onclick="showDataSection('professors')">
                            <i class="fas fa-user-tie"></i> Ver Profesores
                        </button>
                        <button class="btn btn-primary" onclick="showDataSection('subjects')">
                            <i class="fas fa-book"></i> Ver Materias
                        </button>
                        <button class="btn btn-secondary" onclick="showDataSection('statistics')">
                            <i class="fas fa-chart-pie"></i> Estadísticas
                        </button>
                        <button class="btn btn-secondary" onclick="validateSchedules()">
                            <i class="fas fa-check-circle"></i> Validar Horarios
                        </button>
                    </div>

                    <div id="data-display"></div>
                </div>
            </div>
        </div>
    </div>

    <script>
        window.APP_CONFIG = {
            API_BASE: '${pageContext.request.contextPath}/api/horarios',
            MATERIAS_OPTIONS: `
                <c:forEach var="materia" items="${materiasUnicas}">
                    <option value="<c:out value='${materia}'/>"><c:out value="${materia}"/></option>
                </c:forEach>
            `,
            PROFESORES_OPTIONS: `
                <c:forEach var="profesor" items="${profesoresUnicos}">
                    <option value="<c:out value='${profesor.obtenerNombreCompleto()}'/>">
                        <c:out value="${profesor.obtenerNombreCompleto()}"/> (${profesor.obtenerCalificacion()})
                    </option>
                </c:forEach>
            `
        };
    </script>
    
    <script src="${pageContext.request.contextPath}/js/procesos.js"></script>
</body>
</html>
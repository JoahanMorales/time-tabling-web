/**
 * Sistema de Programación de Horarios - Procesos JavaScript Corregidos
 * Versión optimizada con mejor manejo de errores y arquitectura limpia
 */

// ==================== CONFIGURACIÓN Y CONSTANTES ====================

const CONFIG = {
    API_BASE: window.APP_CONFIG?.API_BASE || '/api/horarios',
    SCHEDULE_CONFIG: {
        default: {
            startTime: '07:00',
            endTime: '22:00',
            defaultDuration: 90,
            breakTime: 15,
            lunchBreak: { start: '12:00', end: '13:00' },
            daysOfWeek: ['Lunes', 'Martes', 'Miércoles', 'Jueves', 'Viernes']
        }
    },
    ALGORITHM_NAMES: {
        'maxcoverage': 'Máxima Cobertura (Backtracking)',
        'optimized': 'Algoritmo Voraz Optimizado',
        'astar': 'A* Heurístico'
    }
};

// ==================== ESTADO GLOBAL Y CACHE ====================

const AppState = {
    dataCache: {
        groups: null,
        professors: null,
        subjects: null
    },
    currentModal: null,
    loading: new Set()
};

// ==================== UTILIDADES ====================

class Utils {
    static timeToMinutes(timeStr) {
        const [hours, minutes] = timeStr.split(':').map(Number);
        return hours * 60 + minutes;
    }

    static minutesToTime(minutes) {
        const hours = Math.floor(minutes / 60);
        const mins = minutes % 60;
        return String(hours).padStart(2, '0') + ':' + String(mins).padStart(2, '0');
    }

    static calculateAverage(items, property) {
        if (!items.length) return 0;
        const sum = items.reduce((acc, item) => acc + (item[property] || 0), 0);
        return (sum / items.length).toFixed(2);
    }

    static generateHash(input) {
        return Math.abs(input.split('').reduce((a, b) => {
            a = ((a << 5) - a) + b.charCodeAt(0);
            return a & a;
        }, 0));
    }

    static normalizeScheduleData(item) {
        return {
            materia: item.materia || item.subject || '',
            profesor: item.profesor || item.professor || '',
            dia: item.dia || item.day || '',
            horaInicio: item.horaInicio || item.startTime || '',
            horaFin: item.horaFin || item.endTime || '',
            calificacion: item.calificacion || item.rating || 0,
            duracionMinutos: parseInt(item.duracionMinutos) || parseInt(item.minutes) || 90,
            tipo: item.tipo || item.type || 'Clase teórica'
        };
    }
}

// ==================== MANEJO DE API ====================

class APIManager {
    static async request(endpoint, options = {}) {
        const url = endpoint.startsWith('http') ? endpoint : CONFIG.API_BASE + endpoint;
        
        try {
            console.log('API Request:', url, options);
            
            const response = await fetch(url, {
                headers: {
                    'Accept': 'application/json',
                    'Cache-Control': 'no-cache',
                    ...options.headers
                },
                ...options
            });

            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }

            const data = await response.json();
            console.log('API Response:', data);
            
            return data;
        } catch (error) {
            console.error('API Error:', error);
            throw error;
        }
    }

    static async get(endpoint) {
        return this.request(endpoint, { method: 'GET' });
    }

    static async post(endpoint, data) {
        const isFormData = data instanceof FormData;
        
        return this.request(endpoint, {
            method: 'POST',
            headers: isFormData ? {} : { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: isFormData ? data : new URLSearchParams(data)
        });
    }
}

// ==================== GESTIÓN DE GRUPOS ====================

class GroupManager {
    static async loadExistingGroups() {
        const container = document.getElementById('existing-groups');
        const loading = document.getElementById('groups-loading');

        if (!container || !loading) {
            console.error('Required DOM elements not found');
            return;
        }

        this.showLoading(loading, true);

        try {
            const data = await APIManager.get('?action=grupos');

            if (data.success) {
                AppState.dataCache.groups = data.grupos;
                this.displayGroups(data.grupos);
            } else {
                this.showError(container, data.error || 'Error desconocido');
            }
        } catch (error) {
            this.showError(container, `Error de conexión: ${error.message}`);
        } finally {
            this.showLoading(loading, false);
        }
    }

    static displayGroups(groups) {
        const container = document.getElementById('existing-groups');
        if (!container) return;

        container.innerHTML = '';

        Object.entries(groups).forEach(([groupName, group]) => {
            const avgRating = Utils.calculateAverage(group, 'rating');
            const card = this.createGroupCard(groupName, group, avgRating);
            container.appendChild(card);
        });
    }

    static createGroupCard(groupName, group, avgRating) {
        const card = document.createElement('div');
        card.className = 'group-card';

        const subjectListHTML = group.map(item => `
            <li>
                <div>
                    <div class="subject-name">${item.subject}</div>
                    <div class="professor-name">
                        <i class="fas fa-user"></i> ${item.professor}
                    </div>
                </div>
                <div class="rating">${item.rating}</div>
            </li>
        `).join('');

        card.innerHTML = `
            <h4><i class="fas fa-users"></i> Grupo ${groupName}</h4>
            <ul class="subject-list">${subjectListHTML}</ul>
            <div style="display: flex; justify-content: space-between; align-items: center; margin-top: 15px;">
                <strong><i class="fas fa-star"></i> Promedio: ${avgRating}</strong>
                <button class="btn btn-secondary" onclick="ModalManager.showGroupDetail('${groupName}')">
                    <i class="fas fa-eye"></i> Ver Detalle
                </button>
            </div>
        `;

        return card;
    }

    static async createGroup(formData) {
        try {
            const data = await APIManager.post('', {
                action: 'crear',
                nombreGrupo: formData.groupName,
                estrategia: formData.strategy,
                algoritmo: formData.algorithm
            });

            return data;
        } catch (error) {
            throw new Error(`Error creando grupo: ${error.message}`);
        }
    }

    static async createFixedGroup(formData) {
        try {
            const data = await APIManager.post('', {
                action: 'crear-fijado',
                nombreGrupo: formData.groupName,
                profesoresFijados: JSON.stringify(formData.assignments)
            });

            return data;
        } catch (error) {
            throw new Error(`Error creando grupo fijado: ${error.message}`);
        }
    }

    static showLoading(element, show) {
        if (element) {
            element.style.display = show ? 'block' : 'none';
        }
    }

    static showError(container, message) {
        if (container) {
            container.innerHTML = `<div class="alert alert-error">${message}</div>`;
        }
    }
}

// ==================== GESTIÓN DE HORARIOS ====================

class ScheduleManager {
    // ... (mantén todas las funciones existentes y AGREGA estas que faltan)
    
    static async getGroupSchedule(groupName) {
        try {
            const url = `?action=horario&grupo=${encodeURIComponent(groupName)}`;
            const data = await APIManager.get(url);
            
            return data.success && data.horario ? data.horario : null;
        } catch (error) {
            console.error('Error obteniendo horario:', error);
            return null;
        }
    }

    static calculateSessionsPerWeek(duration) {
        const standardDuration = 90; // minutes per session
        
        // Calculate sessions based on total duration
        if (duration >= 270) { // 4.5 hours or more
            return 3; // Three sessions per week
        } else if (duration >= 180) { // 3 hours
            return 2; // Two sessions per week  
        } else if (duration >= standardDuration) { // 1.5 hours
            return 2; // Two sessions per week minimum
        } else {
            return 1; // One session per week for short subjects
        }
    }

    static generateTimeSlots(config) {
        return [
            { start: '07:00', end: '08:30' },
            { start: '08:30', end: '10:00' },
            { start: '10:00', end: '11:30' },
            { start: '11:30', end: '13:00' },
            { start: '14:30', end: '16:00' }, // Skip lunch time
            { start: '16:00', end: '17:30' },
            { start: '17:30', end: '19:00' },
            { start: '19:00', end: '20:30' },
            { start: '20:30', end: '22:00' }
        ];
    }

    static sortSchedule(schedule) {
        return schedule.sort((a, b) => {
            const dayOrder = { 'Lunes': 1, 'Martes': 2, 'Miércoles': 3, 'Jueves': 4, 'Viernes': 5, 'Sábado': 6 };
            const dayDiff = (dayOrder[a.dia] || 7) - (dayOrder[b.dia] || 7);
            if (dayDiff !== 0) return dayDiff;
            
            return Utils.timeToMinutes(a.horaInicio) - Utils.timeToMinutes(b.horaInicio);
        });
    }

    static generateScheduleTable(schedule) {
        if (!schedule || schedule.length === 0) {
            return '<div class="alert alert-warning"><i class="fas fa-exclamation-triangle"></i> No hay horario disponible</div>';
        }

        const days = ['Lunes', 'Martes', 'Miércoles', 'Jueves', 'Viernes'];
        const uniqueTimes = [...new Set(schedule.map(item => item.horaInicio))].sort((a, b) => 
            Utils.timeToMinutes(a) - Utils.timeToMinutes(b)
        );

        let tableHTML = `
            <div class="schedule-info" style="margin-bottom: 15px; padding: 10px; background: #f8fafc; border-radius: 8px; border-left: 4px solid #4299e1;">
                <strong><i class="fas fa-info-circle"></i> Horario Generado:</strong>
                <small style="display: block; margin-top: 5px;">
                    • Total de clases programadas: ${schedule.length}<br>
                    • Horario disponible: 07:00 - 22:00<br>
                    • Días de clase: Lunes a Viernes
                </small>
            </div>
            <table class="schedule-table" style="width: 100%; border-collapse: collapse; background: white; border-radius: 10px; overflow: hidden; box-shadow: 0 4px 15px rgba(0,0,0,0.1);">
                <thead>
                    <tr style="background: linear-gradient(135deg, #667eea, #764ba2); color: white;">
                        <th style="padding: 12px; font-weight: 600; text-align: center; min-width: 100px;">Hora</th>
                        ${days.map(day => `<th style="padding: 12px; font-weight: 600; text-align: center;">${day}</th>`).join('')}
                    </tr>
                </thead>
                <tbody>
        `;

        uniqueTimes.forEach((time, timeIndex) => {
            const isEvenRow = timeIndex % 2 === 0;
            const rowBg = isEvenRow ? '#f8fafc' : '#ffffff';
            
            tableHTML += `<tr style="background: ${rowBg};">`;
            tableHTML += `<td style="padding: 15px; font-weight: bold; text-align: center; border-right: 2px solid #e2e8f0;">${time}</td>`;
            
            days.forEach(day => {
                tableHTML += '<td style="padding: 8px; vertical-align: top; border: 1px solid #e2e8f0;">';
                
                const daySubjects = schedule.filter(item => 
                    item.dia === day && item.horaInicio === time
                );

                daySubjects.forEach(subject => {
                    const typeColor = subject.tipo === 'Asignación forzada' ? '#7c3aed' : '#059669';
                    // Calculate actual duration based on time slot (not the total subject duration)
                    const startTime = Utils.timeToMinutes(subject.horaInicio);
                    const endTime = Utils.timeToMinutes(subject.horaFin);
                    const actualSlotDuration = endTime - startTime;
                    
                    tableHTML += `
                        <div class="schedule-subject" style="background: white; border-left: 4px solid ${typeColor}; border-radius: 6px; padding: 10px; margin-bottom: 5px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">
                            <div style="font-weight: bold; font-size: 0.9em; color: #1a202c; margin-bottom: 4px;">
                                ${subject.materia}
                                ${subject.tipo === 'Asignación forzada' ? '<span style="background: #7c3aed; color: white; padding: 2px 6px; border-radius: 10px; font-size: 0.7em; margin-left: 5px;">FIJO</span>' : ''}
                            </div>
                            <div style="font-size: 0.8em; color: #4a5568; margin-bottom: 3px;">
                                <i class="fas fa-user"></i> ${subject.profesor}
                            </div>
                            <div style="font-size: 0.75em; color: #6b7280; display: flex; justify-content: space-between; align-items: center;">
                                <span><i class="fas fa-clock"></i> ${subject.horaInicio} - ${subject.horaFin}</span>
                                <span style="background: #10b981; color: white; padding: 2px 6px; border-radius: 8px; font-size: 0.7em;">${actualSlotDuration}min</span>
                            </div>
                            <div style="font-size: 0.75em; color: #6b7280; margin-top: 3px;">
                                <i class="fas fa-star"></i> Calificación: ${subject.calificacion}
                            </div>
                        </div>
                    `;
                });

                tableHTML += '</td>';
            });

            tableHTML += '</tr>';
        });

        tableHTML += `
                </tbody>
            </table>
            <style>
                .schedule-table .schedule-subject:hover {
                    transform: translateY(-1px);
                    box-shadow: 0 4px 8px rgba(0,0,0,0.15);
                    transition: all 0.2s ease;
                }
                .schedule-table td {
                    min-height: 60px;
                }
            </style>
        `;

        return tableHTML;
    }
    
    // REEMPLAZAR la función getScheduleFromOriginGroups en ScheduleManager con esta versión corregida:

static async getScheduleFromOriginGroups(materias, prioritizeFixed = false) {
    console.log('=== DEBUG: MATERIAS RECIBIDAS ===');
    materias.forEach((m, i) => {
        console.log(`${i+1}. ${m.subject} - Prof: ${m.professor} - Origen: ${m.origin}`);
    });
    
    // Cargar grupos si no están disponibles
    if (!AppState.dataCache.groups) {
        try {
            const data = await APIManager.get('?action=grupos');
            if (data.success) AppState.dataCache.groups = data.grupos;
        } catch (error) {
            console.log('Error cargando grupos:', error);
            return null;
        }
    }
    
    console.log('=== DEBUG: GRUPOS DISPONIBLES ===');
    Object.keys(AppState.dataCache.groups).forEach(groupName => {
        console.log(`Grupo: ${groupName}`);
    });
    
    const schedule = [];
    
    // Procesar cada materia individualmente con debug completo
    for (const materia of materias) {
        console.log(`\n=== PROCESANDO: ${materia.subject} ===`);
        console.log(`Profesor asignado: ${materia.professor}`);
        console.log(`Origen: ${materia.origin}`);
        
        let targetGroup = null;
        let targetProfessor = materia.professor; // Por defecto usar el profesor asignado
        
        if (materia.origin === 'Asignación forzada') {
            console.log('>> ES MATERIA FIJADA - Usando profesor fijado directamente');
            
            // Para materias fijadas, NO necesitamos buscar el profesor original
            // Usamos directamente el profesor fijado y encontramos un grupo que tenga esta materia
            for (const [groupName, groupMaterias] of Object.entries(AppState.dataCache.groups)) {
                console.log(`  Buscando materia en grupo ${groupName}...`);
                
                const found = groupMaterias.find(gm => {
                    const match = gm.subject.toLowerCase().trim() === materia.subject.toLowerCase().trim();
                    if (match) {
                        console.log(`    ✓ Materia encontrada en grupo ${groupName}`);
                    }
                    return match;
                });
                
                if (found) {
                    targetGroup = groupName;
                    // MANTENER el profesor fijado, no el original del grupo
                    console.log(`  >> TARGET GROUP: ${targetGroup}`);
                    console.log(`  >> PROFESOR FIJADO: ${targetProfessor}`);
                    break;
                }
            }
        } else if (materia.origin && materia.origin !== 'Asignación automática') {
            console.log('>> ES MATERIA NORMAL - Usando grupo de origen directo');
            targetGroup = materia.origin;
            console.log(`  >> TARGET GROUP: ${targetGroup}`);
        }
        
        if (!targetGroup) {
            console.log(`❌ NO SE ENCONTRÓ TARGET GROUP para ${materia.subject}`);
            continue;
        }
        
        // Obtener horario del grupo objetivo
        console.log(`\n>> OBTENIENDO HORARIO DEL GRUPO: ${targetGroup}`);
        
        try {
            const groupSchedule = await this.getGroupSchedule(targetGroup);
            
            if (!groupSchedule || groupSchedule.length === 0) {
                console.log(`❌ NO HAY HORARIO para el grupo ${targetGroup}`);
                continue;
            }
            
            console.log(`✓ Horario obtenido: ${groupSchedule.length} entradas`);
            
            // Para materias fijadas: buscar PRIMERO el horario del profesor fijado específicamente
            if (materia.origin === 'Asignación forzada') {
                console.log(`\n>> BUSCANDO HORARIO PARA MATERIA FIJADA: ${materia.subject}`);
                console.log(`>> PROFESOR FIJADO: ${targetProfessor}`);
                
                // ESTRATEGIA 1: Buscar en TODOS los grupos el horario del profesor fijado para esta materia
                let foundScheduleForFixedProfessor = [];
                
                for (const [searchGroupName, searchGroup] of Object.entries(AppState.dataCache.groups)) {
                    console.log(`  Buscando horario del prof. fijado en grupo: ${searchGroupName}`);
                    
                    // Verificar si este grupo tiene la materia con el profesor fijado
                    const hasFixedProfSubject = searchGroup.some(item => 
                        item.subject.toLowerCase().trim() === materia.subject.toLowerCase().trim() &&
                        item.professor.toLowerCase().trim() === targetProfessor.toLowerCase().trim()
                    );
                    
                    if (hasFixedProfSubject) {
                        console.log(`  ✓ Grupo ${searchGroupName} tiene la materia con profesor fijado`);
                        
                        // Obtener horario de este grupo
                        try {
                            const searchGroupSchedule = await this.getGroupSchedule(searchGroupName);
                            
                            if (searchGroupSchedule && searchGroupSchedule.length > 0) {
                                // Buscar sesiones específicas del profesor fijado
                                const fixedProfSchedules = searchGroupSchedule.filter(entry => {
                                    const subjectMatch = entry.materia.toLowerCase().trim() === materia.subject.toLowerCase().trim();
                                    const professorMatch = entry.profesor.toLowerCase().trim() === targetProfessor.toLowerCase().trim();
                                    
                                    console.log(`    Verificando: "${entry.materia}" + "${entry.profesor}"`);
                                    console.log(`    Matches: subject=${subjectMatch}, prof=${professorMatch}`);
                                    
                                    return subjectMatch && professorMatch;
                                });
                                
                                if (fixedProfSchedules.length > 0) {
                                    console.log(`  ✅ ENCONTRADO! Horario del prof. fijado en grupo ${searchGroupName}: ${fixedProfSchedules.length} sesiones`);
                                    foundScheduleForFixedProfessor = fixedProfSchedules;
                                    break; // Encontramos lo que buscamos, salir del loop
                                }
                            }
                        } catch (error) {
                            console.log(`  Error obteniendo horario de ${searchGroupName}:`, error);
                        }
                    }
                }
                
                // Si encontramos el horario del profesor fijado, usarlo
                if (foundScheduleForFixedProfessor.length > 0) {
                    console.log(`>> USANDO HORARIO DEL PROFESOR FIJADO: ${foundScheduleForFixedProfessor.length} sesiones`);
                    
                    foundScheduleForFixedProfessor.forEach((session, i) => {
                        console.log(`  ${i+1}. Usando sesión CORRECTA: ${session.dia} ${session.horaInicio}-${session.horaFin} con ${session.profesor}`);
                        
                        const finalSession = {
                            ...session,
                            profesor: targetProfessor, // Asegurar que sea el profesor fijado
                            materia: materia.subject,
                            calificacion: materia.rating,
                            tipo: 'Asignación forzada',
                            duracionMinutos: session.duracionMinutos || 90
                        };
                        
                        schedule.push(finalSession);
                        console.log(`    ✓ Agregado CORRECTO: ${finalSession.dia} ${finalSession.horaInicio}-${finalSession.horaFin} con ${finalSession.profesor}`);
                    });
                } else {
                    // ESTRATEGIA 2: Si no encontramos el horario del profesor fijado, usar cualquier horario como plantilla
                    console.log(`>> NO se encontró horario del prof. fijado, buscando plantilla...`);
                    
                    const materiaSchedules = groupSchedule.filter(entry => {
                        const subjectMatch = entry.materia.toLowerCase().trim() === materia.subject.toLowerCase().trim();
                        console.log(`  Plantilla: "${entry.materia}" vs "${materia.subject}" = ${subjectMatch}`);
                        return subjectMatch;
                    });
                    
                    if (materiaSchedules.length > 0) {
                        console.log(`>> Usando ${materiaSchedules.length} sesiones como plantilla para profesor fijado`);
                        
                        materiaSchedules.forEach((session, i) => {
                            console.log(`  ${i+1}. Plantilla: ${session.dia} ${session.horaInicio}-${session.horaFin} (era ${session.profesor})`);
                            
                            const finalSession = {
                                ...session,
                                profesor: targetProfessor, // REEMPLAZAR con profesor fijado
                                materia: materia.subject,
                                calificacion: materia.rating,
                                tipo: 'Asignación forzada',
                                duracionMinutos: session.duracionMinutos || 90
                            };
                            
                            schedule.push(finalSession);
                            console.log(`    ✓ Plantilla adaptada: ${finalSession.dia} ${finalSession.horaInicio}-${finalSession.horaFin} con FIJADO ${finalSession.profesor}`);
                        });
                    } else {
                        // ESTRATEGIA 3: Generar horario básico
                        console.log(`>> GENERANDO HORARIO BÁSICO para materia fijada`);
                        const basicSchedule = this.generateBasicScheduleForSubject(materia, targetProfessor);
                        if (basicSchedule) {
                            schedule.push(...basicSchedule);
                            console.log(`    ✓ Horario básico generado para ${materia.subject} con ${targetProfessor}`);
                        }
                    }
                }
            } else {
                // Para materias normales: buscar por materia Y profesor
                console.log(`\n>> BUSCANDO SESIONES NORMALES DE: ${materia.subject}`);
                console.log(`>> CON PROFESOR: ${targetProfessor}`);
                
                const foundSessions = groupSchedule.filter(entry => {
                    const subjectMatch = entry.materia.toLowerCase().trim() === materia.subject.toLowerCase().trim();
                    const professorMatch = entry.profesor.toLowerCase().trim() === targetProfessor.toLowerCase().trim();
                    
                    console.log(`  Comparando: "${entry.materia}" vs "${materia.subject}" = ${subjectMatch}`);
                    console.log(`  Profesor: "${entry.profesor}" vs "${targetProfessor}" = ${professorMatch}`);
                    
                    return subjectMatch && professorMatch;
                });
                
                console.log(`>> SESIONES ENCONTRADAS: ${foundSessions.length}`);
                
                if (foundSessions.length > 0) {
                    foundSessions.forEach((session, i) => {
                        console.log(`  ${i+1}. ${session.dia} ${session.horaInicio}-${session.horaFin}`);
                        
                        const finalSession = {
                            ...session,
                            profesor: targetProfessor,
                            calificacion: materia.rating,
                            tipo: session.tipo || 'Clase teórica'
                        };
                        
                        schedule.push(finalSession);
                        console.log(`    ✓ Agregado con profesor: ${finalSession.profesor}`);
                    });
                } else {
                    console.log(`❌ NO SE ENCONTRARON SESIONES`);
                }
            }
            
        } catch (error) {
            console.log(`❌ ERROR obteniendo horario de ${targetGroup}:`, error);
        }
    }
    
    console.log(`\n=== RESULTADO FINAL ===`);
    console.log(`Total sesiones en horario final: ${schedule.length}`);
    
    if (schedule.length > 0) {
        schedule.forEach((s, i) => {
            console.log(`${i+1}. ${s.materia} - ${s.profesor} - ${s.dia} ${s.horaInicio} (${s.tipo})`);
        });
        
        return this.sortSchedule(schedule);
    }
    
    console.log('❌ NO SE GENERÓ HORARIO');
    return null;
}

// TAMBIÉN AGREGAR esta función auxiliar al ScheduleManager:

static generateBasicScheduleForSubject(materia, professor) {
    const schedule = [];
    const days = ['Lunes', 'Miércoles', 'Viernes'];
    const times = [
        { start: '08:00', end: '09:30' },
        { start: '10:00', end: '11:30' },
        { start: '14:00', end: '15:30' }
    ];
    
    // Crear 2-3 sesiones por semana para la materia
    const sessionsNeeded = this.calculateSessionsPerWeek(materia.minutes || 270);
    
    for (let i = 0; i < Math.min(sessionsNeeded, days.length); i++) {
        const timeSlot = times[i % times.length];
        
        schedule.push({
            materia: materia.subject,
            profesor: professor,
            dia: days[i],
            horaInicio: timeSlot.start,
            horaFin: timeSlot.end,
            calificacion: materia.rating,
            duracionMinutos: 90,
            tipo: 'Asignación forzada'
        });
    }
    
    return schedule;
}


}

// ==================== GESTIÓN DE MODALES ====================

class ModalManager {
    static async showGroupDetail(groupName) {
        if (!AppState.dataCache.groups?.[groupName]) {
            alert('Datos del grupo no disponibles');
            return;
        }

        const group = AppState.dataCache.groups[groupName];
        const schedule = await ScheduleManager.getGroupSchedule(groupName);
        
        const modal = this.createModal({
            title: `Detalle del Grupo ${groupName}`,
            content: this.generateGroupDetailContent(group, schedule, groupName),
            size: 'large'
        });

        this.showModal(modal);
    }

    static generateGroupDetailContent(group, schedule, groupName) {
        const avgRating = Utils.calculateAverage(group, 'rating');
        
        const groupDetailsHTML = group.map(item => `
            <div style="display: flex; justify-content: space-between; align-items: center; padding: 15px; background: #f8fafc; border-radius: 10px; border: 1px solid #e2e8f0;">
                <div>
                    <strong><i class="fas fa-book"></i> ${item.subject}</strong><br>
                    <small style="color: #718096;"><i class="fas fa-user"></i> ${item.professor}</small><br>
                    <small style="color: #718096;"><i class="fas fa-clock"></i> ${item.minutes || 'N/A'} minutos</small>
                </div>
                <div class="rating">${item.rating}</div>
            </div>
        `).join('');

        let scheduleHTML = '';
        if (schedule && schedule.length > 0) {
            scheduleHTML = `
                <div style="margin-top: 30px;">
                    <h3 style="margin-bottom: 20px;"><i class="fas fa-calendar-week"></i> Horario del Grupo</h3>
                    ${ScheduleManager.generateScheduleTable(schedule)}
                </div>
            `;
        } else {
            scheduleHTML = `
                <div style="margin-top: 30px;">
                    <h3 style="margin-bottom: 20px;"><i class="fas fa-calendar-week"></i> Horario del Grupo</h3>
                    <div class="alert alert-warning">
                        <i class="fas fa-info-circle"></i> 
                        <strong>Información de horario no disponible</strong><br>
                        El horario puede no estar generado aún o el endpoint no está disponible.
                    </div>
                </div>
            `;
        }

        return `
            <div style="margin-bottom: 20px;">
                <h3 style="margin: 20px 0 10px 0;"><i class="fas fa-list"></i> Lista de Materias</h3>
                <div style="display: grid; gap: 10px; margin-bottom: 20px;">
                    ${groupDetailsHTML}
                </div>
                <div style="padding-top: 20px; border-top: 1px solid #e2e8f0;">
                    <strong><i class="fas fa-star"></i> Promedio del Grupo: ${avgRating}</strong>
                </div>
                ${scheduleHTML}
            </div>
        `;
    }

    static createModal({ title, content, size = 'medium' }) {
        const modal = document.createElement('div');
        modal.className = 'modal-overlay';
        modal.innerHTML = `
            <div class="modal-content ${size}">
                <button class="modal-close" onclick="ModalManager.closeModal()">&times;</button>
                <h2>${title}</h2>
                ${content}
            </div>
        `;
        return modal;
    }

    static showModal(modal) {
        if (AppState.currentModal) {
            AppState.currentModal.remove();
        }
        
        AppState.currentModal = modal;
        document.body.appendChild(modal);
        
        // Event listeners
        modal.addEventListener('click', (e) => {
            if (e.target.classList.contains('modal-overlay')) {
                this.closeModal();
            }
        });

        document.addEventListener('keydown', this.handleKeydown);
    }

    static closeModal() {
        if (AppState.currentModal) {
            AppState.currentModal.remove();
            AppState.currentModal = null;
            document.removeEventListener('keydown', this.handleKeydown);
        }
    }

    static handleKeydown = (e) => {
        if (e.key === 'Escape') {
            ModalManager.closeModal();
        }
    }
}

// ==================== GESTIÓN DE FORMULARIOS ====================

class FormManager {
    // ... (mantén todas las funciones existentes y AGREGA estas)
    
    static async handleCreateGroup(e) {
        e.preventDefault();
        
        const formData = {
            groupName: document.getElementById('group-name')?.value,
            strategy: document.getElementById('evaluation-strategy')?.value,
            algorithm: document.getElementById('algorithm-type')?.value
        };

        if (!this.validateCreateGroupForm(formData)) return;

        const loadingElement = document.getElementById('create-loading');
        const resultsElement = document.getElementById('create-results');

        try {
            this.showLoading(loadingElement, true);
            resultsElement.innerHTML = '';

            const data = await GroupManager.createGroup(formData);
            await this.displayCreateResult(data, formData.groupName);

        } catch (error) {
            this.showError(resultsElement, error.message);
        } finally {
            this.showLoading(loadingElement, false);
        }
    }

    static async handleFixedProfessorsForm(e) {
    e.preventDefault();

    const groupName = document.getElementById('fixed-group-name')?.value;
    const assignments = this.collectAssignments();

    // Validaciones básicas
    if (!groupName || groupName.trim() === '') {
        alert('Debe proporcionar un nombre para el grupo');
        return;
    }

    if (Object.keys(assignments).length === 0) {
        alert('Debe realizar al menos una asignación de profesor a materia');
        return;
    }

    // Validación de duplicados usando ProfessorManager
    const validation = ProfessorManager.validateAssignments();
    if (!validation.valid) {
        alert(`Error en las asignaciones:\n${validation.error}`);
        return;
    }

    const loadingElement = document.getElementById('fixed-loading');
    const resultsElement = document.getElementById('fixed-results');

    try {
        this.showLoading(loadingElement, true);
        resultsElement.innerHTML = '';

        console.log('Enviando asignaciones validadas:', assignments);

        const data = await GroupManager.createFixedGroup({ groupName, assignments });
        await this.displayFixedResult(data, groupName);

    } catch (error) {
        this.showError(resultsElement, error.message);
    } finally {
        this.showLoading(loadingElement, false);
    }
}

    static validateCreateGroupForm(formData) {
        const requiredFields = ['groupName', 'strategy', 'algorithm'];
        
        for (const field of requiredFields) {
            if (!formData[field]) {
                alert(`El campo ${field} es requerido`);
                return false;
            }
        }
        
        return true;
    }

    static collectAssignments() {
        const assignments = {};
        
        document.querySelectorAll('.professor-assignment').forEach(assignment => {
            const subject = assignment.querySelector('.subject-select')?.value;
            const professor = assignment.querySelector('.professor-select')?.value;
            
            if (subject && professor) {
                assignments[subject] = professor;
            }
        });
        
        return assignments;
    }

    static async displayCreateResult(data, groupName) {
        const container = document.getElementById('create-results');
        
        if (data.success && data.materias) {
            // Update cache
            if (!AppState.dataCache.groups) AppState.dataCache.groups = {};
            AppState.dataCache.groups[groupName] = data.materias.map(subject => ({
                subject: subject.subject,
                professor: subject.professor,
                rating: subject.rating,
                minutes: subject.minutes || 60
            }));

            const materiasHTML = this.generateSubjectsHTML(data.materias);
            const schedule = await this.getScheduleForGroup(groupName, data.materias);
            const scheduleHTML = this.generateScheduleHTML(schedule);

            container.innerHTML = this.createSuccessResultHTML({
                groupName: data.nombreGrupo,
                score: data.puntuacion,
                totalSubjects: data.totalMaterias,
                algorithm: data.algoritmo,
                executionTime: data.tiempoEjecucion,
                subjects: materiasHTML,
                schedule: scheduleHTML
            });
        } else {
            this.showError(container, data.error || 'No se pudo crear el grupo');
        }
    }

    static generateSubjectsHTML(materias) {
        return materias.map(subject => `
            <div style="display: flex; justify-content: space-between; align-items: center; padding: 12px; background: white; border-radius: 10px; border: 1px solid #e2e8f0;">
                <div>
                    <strong>${subject.subject}</strong><br>
                    <small><i class="fas fa-user"></i> Prof: ${subject.professor} | <i class="fas fa-home"></i> Origen: ${subject.origin}</small>
                </div>
                <div class="rating">${subject.rating}</div>
            </div>
        `).join('');
    }

    static generateScheduleHTML(schedule) {
        if (schedule && schedule.length > 0) {
            return `
                <div class="schedule-container">
                    <h4><i class="fas fa-calendar-week"></i> Horario del Grupo</h4>
                    <div class="alert alert-success" style="margin-bottom: 15px;">
                        <i class="fas fa-check-circle"></i> Horario generado exitosamente.
                    </div>
                    ${ScheduleManager.generateScheduleTable(schedule)}
                </div>
            `;
        } else {
            return `
                <div class="schedule-container">
                    <h4><i class="fas fa-calendar-week"></i> Horario del Grupo</h4>
                    <div class="alert alert-warning">
                        <i class="fas fa-info-circle"></i> 
                        <strong>Grupo creado exitosamente</strong><br>
                        El horario se generará automáticamente por el sistema.
                    </div>
                </div>
            `;
        }
    }

    static createSuccessResultHTML(data) {
        return `
            <div style="background: linear-gradient(135deg, #f7fafc, #edf2f7); border-radius: 15px; padding: 25px; margin-bottom: 20px; border-left: 4px solid #48bb78;">
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
                    <div style="color: #2d3748; font-size: 1.4rem; font-weight: 700;">
                        <i class="fas fa-check-circle"></i> Grupo ${data.groupName} Creado
                    </div>
                    <div style="background: linear-gradient(135deg, #48bb78, #38a169); color: white; padding: 8px 16px; border-radius: 20px; font-weight: 600;">
                        <i class="fas fa-star"></i> Puntuación: ${data.score}
                    </div>
                </div>
                <div class="alert alert-success">
                    <i class="fas fa-check"></i> Grupo creado exitosamente con ${data.totalSubjects} materias<br>
                    <i class="fas fa-microchip"></i> Algoritmo: ${CONFIG.ALGORITHM_NAMES[data.algorithm] || data.algorithm}<br>
                    <i class="fas fa-clock"></i> Tiempo de ejecución: ${data.executionTime}ms
                </div>
                <h4><i class="fas fa-list"></i> Materias Asignadas:</h4>
                <div style="display: grid; gap: 10px; margin: 15px 0;">
                    ${data.subjects}
                </div>
                ${data.schedule}
            </div>
        `;
    }

    static async getScheduleForGroup(groupName, materias) {
        // Try to get real schedule first
        let schedule = await ScheduleManager.getGroupSchedule(groupName);
        
        if (!schedule) {
            // Try to get schedule from origin groups
            schedule = await ScheduleManager.getScheduleFromOriginGroups(materias);
        }
        
        if (!schedule) {
            // Generate basic schedule as fallback
            const normalizedMaterias = materias.map(m => ({
                subject: m.subject,
                professor: m.professor,
                rating: m.rating,
                minutes: m.minutes || 60,
                origin: m.origin
            }));
            
            const isFixed = materias.some(m => m.origin === 'Asignación forzada');
            schedule = ScheduleManager.generateBasicSchedule(normalizedMaterias, isFixed);
        }
        
        return schedule;
    }

    static showLoading(element, show) {
        if (element) {
            element.classList.toggle('active', show);
        }
    }

    static showError(container, message) {
        if (container) {
            container.innerHTML = `<div class="alert alert-error"><i class="fas fa-exclamation-triangle"></i> ${message}</div>`;
        }
    }
    
    // AGREGAR ESTAS FUNCIONES A TU CLASE FormManager

static async displayFixedResult(data, groupName) {
    const container = document.getElementById('fixed-results');
    
    if (data.success && data.materias) {
        console.log('=== MOSTRANDO RESULTADO DE GRUPO FIJADO ===');
        console.log('Datos recibidos:', data);
        
        // Actualizar cache
        if (!AppState.dataCache.groups) AppState.dataCache.groups = {};
        AppState.dataCache.groups[groupName] = data.materias.map(subject => ({
            subject: subject.subject,
            professor: subject.professor,
            rating: subject.rating,
            minutes: subject.minutes || 270,
            origin: subject.origin
        }));

        console.log('Cache actualizado:', AppState.dataCache.groups[groupName]);

        // Generar HTML para asignaciones fijadas
        let assignedProfessorsHTML = '';
        const professorsKeys = Object.keys(data.profesoresFijados || {});
        console.log('Profesores fijados recibidos:', data.profesoresFijados);
        
        for (let i = 0; i < professorsKeys.length; i++) {
            const subject = professorsKeys[i];
            const professor = data.profesoresFijados[subject];
            assignedProfessorsHTML += `
                <div style="display: flex; justify-content: space-between; align-items: center; padding: 8px 0; border-bottom: 1px solid #e2e8f0;">
                    <span><i class="fas fa-book"></i> ${subject}</span>
                    <strong><i class="fas fa-user"></i> ${professor}</strong>
                </div>
            `;
        }

        // Generar HTML para todas las materias con mejor debugging
        const allSubjectsHTML = data.materias.map(subject => {
            console.log('Procesando materia para mostrar:', subject);
            const bgColor = subject.origin === 'Asignación forzada' ? '#fff5f5' : 'white';
            return `
                <div style="display: flex; justify-content: space-between; align-items: center; padding: 12px; background: ${bgColor}; border-radius: 10px; border: 1px solid #e2e8f0;">
                    <div>
                        <strong>${subject.subject}</strong><br>
                        <small><i class="fas fa-user"></i> Prof: ${subject.professor} | <i class="fas fa-info-circle"></i> ${subject.origin}</small>
                    </div>
                    <div class="rating">${subject.rating}</div>
                </div>
            `;
        }).join('');

        // Obtener horario para el grupo
        console.log('Obteniendo horario para grupo fijado...');
        const schedule = await this.getScheduleForGroup(groupName, data.materias);
        console.log('Horario obtenido:', schedule);
        
        const scheduleHTML = schedule && schedule.length > 0 ? `
            <div class="schedule-container">
                <h4><i class="fas fa-calendar-week"></i> Horario del Grupo</h4>
                <div class="alert alert-success" style="margin-bottom: 15px;">
                    <i class="fas fa-check-circle"></i> Horario generado respetando las asignaciones obligatorias.
                </div>
                ${ScheduleManager.generateScheduleTable(schedule)}
            </div>
        ` : `
            <div class="schedule-container">
                <h4><i class="fas fa-calendar-week"></i> Horario del Grupo</h4>
                <div class="alert alert-warning">
                    <i class="fas fa-exclamation-triangle"></i> No se pudo generar el horario automáticamente.
                    <button type="button" class="btn btn-primary" onclick="window.forceGenerateSchedule('${groupName}', 'fixed-results', event)" style="margin-top: 10px;">
                        <i class="fas fa-calendar-plus"></i> Generar Horario
                    </button>
                </div>
            </div>
        `;

        container.innerHTML = `
            <div style="background: linear-gradient(135deg, #f7fafc, #edf2f7); border-radius: 15px; padding: 25px; margin-bottom: 20px; border-left: 4px solid #48bb78;">
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
                    <div style="color: #2d3748; font-size: 1.4rem; font-weight: 700;">
                        <i class="fas fa-lock"></i> Grupo ${data.nombreGrupo} - Profesores Fijados
                    </div>
                    <div style="background: linear-gradient(135deg, #48bb78, #38a169); color: white; padding: 8px 16px; border-radius: 20px; font-weight: 600;">
                        <i class="fas fa-star"></i> Puntuación: ${data.puntuacion}
                    </div>
                </div>
                <div class="alert alert-success">
                    <i class="fas fa-check"></i> Grupo creado con ${professorsKeys.length} asignaciones obligatorias<br>
                    <i class="fas fa-list"></i> Total de materias: ${data.totalMaterias}<br>
                    <i class="fas fa-clock"></i> Tiempo de ejecución: ${data.tiempoEjecucion}ms
                </div>
                <div style="background: #fff5f5; border: 2px dashed #feb2b2; border-radius: 10px; padding: 20px; margin: 20px 0;">
                    <h4><i class="fas fa-lock"></i> Asignaciones Obligatorias:</h4>
                    ${assignedProfessorsHTML}
                </div>
                <h4 style="margin-top: 20px;"><i class="fas fa-list"></i> Todas las Materias:</h4>
                <div style="display: grid; gap: 10px; margin: 15px 0;">
                    ${allSubjectsHTML}
                </div>
                ${scheduleHTML}
            </div>
        `;
    } else {
        console.error('Error en respuesta del servidor:', data);
        this.showError(container, data.error || 'No se pudo crear el grupo');
    }
}

// TAMBIÉN NECESITAS ESTA FUNCIÓN:
static async getScheduleForGroup(groupName, materias) {
    console.log('=== OBTENIENDO HORARIO PARA GRUPO ===');
    console.log('Grupo:', groupName);
    console.log('Materias:', materias.length);
    
    // 1. PRIMERA PRIORIDAD: Intentar obtener el horario real del servidor
    console.log('1. Intentando obtener horario real del servidor...');
    let schedule = await ScheduleManager.getGroupSchedule(groupName);
    
    if (schedule && schedule.length > 0) {
        console.log('✅ Horario real obtenido del servidor:', schedule.length, 'entradas');
        return schedule;
    }
    
    // 2. SEGUNDA PRIORIDAD: Obtener horarios desde grupos de origen
    console.log('2. Intentando obtener horario desde grupos de origen...');
    const hasOriginGroups = materias.some(m => m.origin && 
        m.origin !== 'Asignación automática' && 
        m.origin !== 'Asignación forzada');
        
    if (hasOriginGroups) {
        const isFixed = materias.some(m => m.origin === 'Asignación forzada');
        schedule = await ScheduleManager.getScheduleFromOriginGroups(materias, isFixed);
        
        if (schedule && schedule.length > 0) {
            console.log('✅ Horario obtenido desde grupos origen:', schedule.length, 'entradas');
            return schedule;
        }
    }
    
    // 3. ÚLTIMA OPCIÓN: Generar horario básico
    console.log('3. Generando horario básico como último recurso...');
    const normalizedMaterias = materias.map(m => ({
        subject: m.subject,
        professor: m.professor,
        rating: m.rating,
        minutes: m.minutes || 270,
        origin: m.origin
    }));
    
    const isFixed = materias.some(m => m.origin === 'Asignación forzada');
    schedule = ScheduleManager.generateBasicSchedule(normalizedMaterias, isFixed);
    
    console.log('⚠️ Usando horario básico generado:', schedule ? schedule.length : 0, 'entradas');
    return schedule;
}
}

// ==================== NAVEGACIÓN Y UI ====================

class NavigationManager {
    static showTab(tabName, event) {
        // Hide all tabs
        document.querySelectorAll('.tab-content').forEach(tab => {
            tab.classList.remove('active');
        });
        document.querySelectorAll('.tab').forEach(tab => {
            tab.classList.remove('active');
        });

        // Show selected tab
        const tabContent = document.getElementById(tabName + '-tab');
        if (tabContent) tabContent.classList.add('active');
        if (event?.target) event.target.classList.add('active');

        // Load specific content
        switch (tabName) {
            case 'existing':
                GroupManager.loadExistingGroups();
                break;
            case 'manage':
                DataManager.showDataSection('statistics');
                break;
            case 'fixed':
                ProfessorManager.initializeFixedProfessorsTab();
                break;
        }
    }
}

// ==================== GESTIÓN DE DATOS ====================

class DataManager {
    static async showDataSection(section) {
        const container = document.getElementById('data-display');
        if (!container) return;

        try {
            switch (section) {
                case 'professors':
                    await this.showProfessors(container);
                    break;
                case 'subjects':
                    await this.showSubjects(container);
                    break;
                case 'statistics':
                    await this.showStatistics(container);
                    break;
            }
        } catch (error) {
            container.innerHTML = `<div class="alert alert-error">Error: ${error.message}</div>`;
        }
    }

    static async showProfessors(container) {
        const data = await APIManager.get('?action=profesores');
        
        if (data.success) {
            const professorsTableHTML = data.profesores.map(prof => `
                <tr style="border-bottom: 1px solid #e2e8f0;" onmouseover="this.style.background='#f8fafc'" onmouseout="this.style.background='white'">
                    <td style="padding: 15px;"><strong><i class="fas fa-user"></i> ${prof.name}</strong></td>
                    <td style="padding: 15px;"><span class="rating">${prof.rating}</span></td>
                    <td style="padding: 15px;">${prof.subjects.join(', ')}</td>
                </tr>
            `).join('');

            container.innerHTML = `
                <div class="form-section">
                    <h4><i class="fas fa-user-tie"></i> Lista de Profesores</h4>
                    <table style="width: 100%; border-collapse: collapse; margin: 20px 0; background: white; border-radius: 10px; overflow: hidden; box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);">
                        <thead>
                            <tr style="background: linear-gradient(135deg, #667eea, #764ba2); color: white;">
                                <th style="padding: 15px; text-align: left; font-weight: 600;">Nombre</th>
                                <th style="padding: 15px; text-align: left; font-weight: 600;">Calificación</th>
                                <th style="padding: 15px; text-align: left; font-weight: 600;">Materias que Imparte</th>
                            </tr>
                        </thead>
                        <tbody>
                            ${professorsTableHTML}
                        </tbody>
                    </table>
                </div>
            `;
        } else {
            throw new Error(data.error);
        }
    }

    static async showSubjects(container) {
        const data = await APIManager.get('?action=materias');
        
        if (data.success) {
            const subjectsHTML = data.materias.map(materia => `
                <div style="background: white; padding: 20px; border-radius: 15px; border: 1px solid #e2e8f0;">
                    <h5 style="color: #667eea; margin-bottom: 10px;">
                        <i class="fas fa-book-open"></i> ${materia}
                    </h5>
                </div>
            `).join('');

            container.innerHTML = `
                <div class="form-section">
                    <h4><i class="fas fa-book"></i> Materias del Plan de Estudios</h4>
                    <div style="display: grid; gap: 15px;">
                        ${subjectsHTML}
                    </div>
                </div>
            `;
        } else {
            throw new Error(data.error);
        }
    }

    static async showStatistics(container) {
        const data = await APIManager.get('?action=estadisticas');
        
        if (data.success) {
            const stats = data.estadisticas;
            container.innerHTML = `
                <div class="form-section">
                    <h4><i class="fas fa-chart-pie"></i> Estadísticas del Sistema</h4>
                    <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 20px;">
                        <div style="background: linear-gradient(135deg, #667eea, #764ba2); color: white; padding: 20px; border-radius: 15px; text-align: center;">
                            <h3><i class="fas fa-user-tie"></i></h3>
                            <h3>${stats.totalProfesores}</h3>
                            <p>Profesores Registrados</p>
                        </div>
                        <div style="background: linear-gradient(135deg, #48bb78, #38a169); color: white; padding: 20px; border-radius: 15px; text-align: center;">
                            <h3><i class="fas fa-users"></i></h3>
                            <h3>${stats.totalGrupos}</h3>
                            <p>Grupos Existentes</p>
                        </div>
                        <div style="background: linear-gradient(135deg, #f093fb, #f5576c); color: white; padding: 20px; border-radius: 15px; text-align: center;">
                            <h3><i class="fas fa-book"></i></h3>
                            <h3>${stats.totalMaterias}</h3>
                            <p>Materias del Plan</p>
                        </div>
                        <div style="background: linear-gradient(135deg, #ffeaa7, #fdcb6e); color: white; padding: 20px; border-radius: 15px; text-align: center;">
                            <h3><i class="fas fa-star"></i></h3>
                            <h3>${stats.calificacionPromedio}</h3>
                            <p>Calificación Promedio</p>
                        </div>
                    </div>
                </div>
            `;
        } else {
            throw new Error(data.error);
        }
    }

    static async validateSchedules() {
        const container = document.getElementById('data-display');
        if (!container) return;

        try {
            const data = await APIManager.post('', { action: 'validar' });

            if (data.success) {
                if (data.valido) {
                    container.innerHTML = `
                        <div class="alert alert-success">
                            <i class="fas fa-check-circle"></i> ✓ Sin conflictos en los grupos existentes
                        </div>
                    `;
                } else {
                    const conflictosHTML = data.conflictos.map(conflicto => `<li>${conflicto}</li>`).join('');
                    container.innerHTML = `
                        <div class="alert alert-warning">
                            <i class="fas fa-exclamation-triangle"></i> ⚠️ Conflictos encontrados:
                            <ul style="margin-top: 10px; padding-left: 20px;">
                                ${conflictosHTML}
                            </ul>
                        </div>
                    `;
                }
            } else {
                throw new Error(data.error);
            }
        } catch (error) {
            container.innerHTML = `<div class="alert alert-error">Error: ${error.message}</div>`;
        }
    }
}

// ==================== GESTIÓN DE PROFESORES ====================

// REEMPLAZA completamente la clase ProfessorManager en tu código con esta versión:

class ProfessorManager {
    static async initializeFixedProfessorsTab() {
        try {
            const professorsBySubject = await this.loadProfessorsBySubject();
            const subjects = Object.keys(professorsBySubject).sort();

            const firstAssignment = document.querySelector('.professor-assignment');
            if (firstAssignment) {
                this.setupAssignment(firstAssignment, subjects, professorsBySubject);
                
                // Configurar el evento para el primer select también
                const firstSubjectSelect = firstAssignment.querySelector('.subject-select');
                if (firstSubjectSelect) {
                    const originalOnChange = firstSubjectSelect.onchange;
                    firstSubjectSelect.onchange = () => {
                        if (originalOnChange) originalOnChange();
                        this.updateAllSubjectSelects();
                    };
                }
            }

            // Mostrar información inicial
            this.showAvailableSubjectsInfo(subjects.length, 0);

        } catch (error) {
            console.error('Error initializing fixed professors tab:', error);
        }
    }

    static async loadProfessorsBySubject() {
        try {
            if (!AppState.dataCache.groups) {
                const data = await APIManager.get('?action=grupos');
                if (data.success) {
                    AppState.dataCache.groups = data.grupos;
                } else {
                    throw new Error(data.error);
                }
            }

            const professorsBySubject = {};

            Object.values(AppState.dataCache.groups).forEach(group => {
                group.forEach(item => {
                    const subject = item.subject;
                    const professor = item.professor;
                    const rating = item.rating;

                    if (!professorsBySubject[subject]) {
                        professorsBySubject[subject] = [];
                    }

                    const exists = professorsBySubject[subject].some(p => p.name === professor);
                    if (!exists) {
                        professorsBySubject[subject].push({ name: professor, rating });
                    }
                });
            });

            return professorsBySubject;
        } catch (error) {
            console.error('Error loading professors by subject:', error);
            return {};
        }
    }

    static setupAssignment(assignment, subjects, professorsBySubject) {
        const subjectSelect = assignment.querySelector('.subject-select');
        const professorSelect = assignment.querySelector('.professor-select');

        if (subjectSelect && professorSelect) {
            subjectSelect.innerHTML = '<option value="">Seleccionar materia...</option>' +
                subjects.map(subject => `<option value="${subject}">${subject}</option>`).join('');

            subjectSelect.onchange = () => {
                this.updateProfessorsForSubject(subjectSelect, professorsBySubject);
                this.updateAllSubjectSelects(); // Actualizar todos los selects cuando cambie uno
            };
            
            professorSelect.innerHTML = '<option value="">Primero selecciona una materia</option>';
            professorSelect.disabled = true;
        }
    }

    static updateProfessorsForSubject(selectElement, professorsBySubject) {
        const selectedSubject = selectElement.value;
        const professorSelect = selectElement.parentElement.querySelector('.professor-select');

        professorSelect.innerHTML = '<option value="">Seleccionar profesor...</option>';

        if (selectedSubject && professorsBySubject[selectedSubject]) {
            professorSelect.disabled = false;
            
            professorsBySubject[selectedSubject].forEach(prof => {
                const option = document.createElement('option');
                option.value = prof.name;
                option.textContent = `${prof.name} (${prof.rating})`;
                professorSelect.appendChild(option);
            });
        } else {
            professorSelect.disabled = true;
        }
    }

    static async addAssignment() {
        try {
            const professorsBySubject = await this.loadProfessorsBySubject();
            const subjects = Object.keys(professorsBySubject).sort();

            const container = document.getElementById('professor-assignments');
            if (!container) return;

            // Validar materias ya asignadas
            const alreadyAssigned = this.getAlreadyAssignedSubjects();
            const availableSubjects = subjects.filter(subject => !alreadyAssigned.includes(subject));

            if (availableSubjects.length === 0) {
                alert('⚠️ No hay más materias disponibles para asignar.\n\nTodas las materias ya han sido asignadas a profesores.');
                return;
            }

            const newAssignment = document.createElement('div');
            newAssignment.className = 'professor-assignment';

            const subjectOptions = '<option value="">Seleccionar materia...</option>' +
                availableSubjects.map(subject => `<option value="${subject}">${subject}</option>`).join('');

            newAssignment.innerHTML = `
                <select class="form-control subject-select">
                    ${subjectOptions}
                </select>
                <select class="form-control professor-select" disabled>
                    <option value="">Primero selecciona una materia</option>
                </select>
                <button type="button" class="btn btn-danger" onclick="ProfessorManager.removeAssignment(this)">
                    <i class="fas fa-times"></i>
                </button>
            `;

            container.appendChild(newAssignment);

            // Setup event listener for the new assignment
            const subjectSelect = newAssignment.querySelector('.subject-select');
            subjectSelect.onchange = () => {
                this.updateProfessorsForSubject(subjectSelect, professorsBySubject);
                this.updateAllSubjectSelects(); // Actualizar otros selects
            };

            // Mostrar información sobre materias disponibles
            this.showAvailableSubjectsInfo(availableSubjects.length, alreadyAssigned.length);

        } catch (error) {
            console.error('Error adding assignment:', error);
        }
    }

    static removeAssignment(button) {
        const assignment = button.closest('.professor-assignment');
        if (assignment) {
            assignment.remove();
            
            // Actualizar selects después de remover
            this.updateAllSubjectSelects();
            
            // Actualizar información de disponibilidad
            this.loadProfessorsBySubject().then(data => {
                const totalSubjects = Object.keys(data).length;
                const assignedSubjects = this.getAlreadyAssignedSubjects().length;
                this.showAvailableSubjectsInfo(totalSubjects - assignedSubjects, assignedSubjects);
            });
        }
    }

    // NUEVAS FUNCIONES PARA EVITAR DUPLICADOS
    static getAlreadyAssignedSubjects() {
        const assignedSubjects = [];
        
        document.querySelectorAll('.professor-assignment .subject-select').forEach(select => {
            const value = select.value;
            if (value && value !== '') {
                assignedSubjects.push(value);
            }
        });
        
        return assignedSubjects;
    }

    static updateAllSubjectSelects() {
        const alreadyAssigned = this.getAlreadyAssignedSubjects();
        
        document.querySelectorAll('.professor-assignment .subject-select').forEach(select => {
            const currentValue = select.value;
            
            // Deshabilitar opciones ya seleccionadas en otros selects
            Array.from(select.options).forEach(option => {
                if (option.value !== '' && option.value !== currentValue) {
                    option.disabled = alreadyAssigned.includes(option.value);
                    option.style.color = option.disabled ? '#999' : '';
                    option.title = option.disabled ? 'Esta materia ya ha sido asignada' : '';
                }
            });
        });
    }

    static showAvailableSubjectsInfo(available, assigned) {
        // Mostrar o actualizar información sobre materias disponibles
        let infoDiv = document.getElementById('subjects-availability-info');
        
        if (!infoDiv) {
            infoDiv = document.createElement('div');
            infoDiv.id = 'subjects-availability-info';
            infoDiv.className = 'alert alert-info';
            
            const container = document.getElementById('professor-assignments');
            if (container && container.parentNode) {
                container.parentNode.insertBefore(infoDiv, container);
            }
        }
        
        const totalSubjects = available + assigned;
        
        if (assigned === 0) {
            infoDiv.innerHTML = `
                <i class="fas fa-info-circle"></i> 
                <strong>Materias disponibles:</strong> ${totalSubjects} materias pueden ser asignadas a profesores específicos.
            `;
            infoDiv.className = 'alert alert-info';
        } else if (available === 0) {
            infoDiv.innerHTML = `
                <i class="fas fa-check-circle"></i> 
                <strong>Todas las materias asignadas:</strong> Las ${assigned} materias disponibles ya han sido asignadas.
            `;
            infoDiv.className = 'alert alert-success';
        } else {
            infoDiv.innerHTML = `
                <i class="fas fa-list-ul"></i> 
                <strong>Estado de asignaciones:</strong> ${assigned} materias asignadas, ${available} disponibles de ${totalSubjects} totales.
            `;
            infoDiv.className = 'alert alert-warning';
        }
    }

    static validateAssignments() {
        const assignments = this.collectAssignments();
        const assignedSubjects = Object.keys(assignments);
        
        // Verificar duplicados
        const duplicates = assignedSubjects.filter((subject, index) => 
            assignedSubjects.indexOf(subject) !== index
        );
        
        if (duplicates.length > 0) {
            return {
                valid: false,
                error: `Materias duplicadas encontradas: ${duplicates.join(', ')}`
            };
        }
        
        // Verificar que todas las asignaciones tengan profesor
        const incompleteAssignments = assignedSubjects.filter(subject => !assignments[subject]);
        
        if (incompleteAssignments.length > 0) {
            return {
                valid: false,
                error: `Asignaciones incompletas: ${incompleteAssignments.join(', ')}`
            };
        }
        
        return { valid: true };
    }

    static collectAssignments() {
        const assignments = {};
        
        document.querySelectorAll('.professor-assignment').forEach(assignment => {
            const subject = assignment.querySelector('.subject-select')?.value;
            const professor = assignment.querySelector('.professor-select')?.value;
            
            if (subject && professor) {
                assignments[subject] = professor;
            }
        });
        
        return assignments;
    }
}

// ==================== COMPARACIÓN DE ALGORITMOS ====================

class ComparisonManager {
    static async handleCompareForm(e) {
        e.preventDefault();

        const groupName = document.getElementById('compare-group-name')?.value;
        const selectedAlgorithms = Array.from(document.querySelectorAll('input[name="algorithms"]:checked'))
            .map(cb => cb.value);

        if (!groupName || selectedAlgorithms.length === 0) {
            alert('Debe proporcionar un nombre de grupo y seleccionar al menos un algoritmo');
            return;
        }

        const loadingElement = document.getElementById('compare-loading');
        const resultsElement = document.getElementById('compare-results');

        try {
            FormManager.showLoading(loadingElement, true);
            resultsElement.innerHTML = '';

            const data = await APIManager.post('', {
                action: 'comparar',
                nombreGrupo: groupName,
                algoritmos: JSON.stringify(selectedAlgorithms)
            });

            ComparisonManager.displayComparisonResults(data);

        } catch (error) {
            FormManager.showError(resultsElement, error.message);
        } finally {
            FormManager.showLoading(loadingElement, false);
        }
    }

    static displayComparisonResults(data) {
        const container = document.getElementById('compare-results');
        if (!container) return;

        if (data.success) {
            const results = data.resultados || [];
            const successfulResults = results.filter(r => r.success);

            let fastest = null, bestQuality = null;
            if (successfulResults.length > 0) {
                fastest = successfulResults.reduce((min, current) => 
                    current.tiempoEjecucion < min.tiempoEjecucion ? current : min
                );
                bestQuality = successfulResults.reduce((max, current) => 
                    current.puntuacion > max.puntuacion ? current : max
                );
            }

            const tableRowsHTML = results.map(result => {
                const statusIcon = result.success ? 
                    '<i class="fas fa-check-circle" style="color: #48bb78;"></i> Éxito' : 
                    '<i class="fas fa-times-circle" style="color: #f56565;"></i> Falló';

                return `
                    <tr style="border-bottom: 1px solid #e2e8f0;" onmouseover="this.style.background='#f8fafc'" onmouseout="this.style.background='white'">
                        <td style="padding: 15px;"><strong>${result.nombre}</strong></td>
                        <td style="padding: 15px;">${result.success ? result.tiempoEjecucion : '-'}</td>
                        <td style="padding: 15px;">${result.success ? result.totalMaterias : '0'}</td>
                        <td style="padding: 15px;">${result.success ? result.puntuacion.toFixed(2) : '0.00'}</td>
                        <td style="padding: 15px;">${statusIcon}</td>
                    </tr>
                `;
            }).join('');

            const statsCards = this.generateStatsCards(fastest, bestQuality, results, successfulResults);

            container.innerHTML = `
                <div style="background: linear-gradient(135deg, #f7fafc, #edf2f7); border-radius: 15px; padding: 25px; margin-bottom: 20px; border-left: 4px solid #48bb78;">
                    <div style="color: #2d3748; font-size: 1.4rem; font-weight: 700; margin-bottom: 20px;">
                        <i class="fas fa-chart-line"></i> Comparación de Algoritmos
                    </div>
                    <table style="width: 100%; border-collapse: collapse; margin: 20px 0; background: white; border-radius: 10px; overflow: hidden; box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);">
                        <thead>
                            <tr style="background: linear-gradient(135deg, #667eea, #764ba2); color: white;">
                                <th style="padding: 15px; text-align: left; font-weight: 600;">Algoritmo</th>
                                <th style="padding: 15px; text-align: left; font-weight: 600;">Tiempo (ms)</th>
                                <th style="padding: 15px; text-align: left; font-weight: 600;">Materias</th>
                                <th style="padding: 15px; text-align: left; font-weight: 600;">Puntuación</th>
                                <th style="padding: 15px; text-align: left; font-weight: 600;">Estado</th>
                            </tr>
                        </thead>
                        <tbody>
                            ${tableRowsHTML}
                        </tbody>
                    </table>
                    <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(250px, 1fr)); gap: 20px; margin-top: 30px;">
                        ${statsCards}
                    </div>
                </div>
            `;
        } else {
            FormManager.showError(container, data.error || 'Error en la comparación');
        }
    }

    static generateStatsCards(fastest, bestQuality, allResults, successfulResults) {
        let cards = '';

        if (fastest) {
            cards += `
                <div style="background: linear-gradient(135deg, #48bb78, #38a169); color: white; padding: 20px; border-radius: 15px; text-align: center;">
                    <h4><i class="fas fa-bolt"></i> Más Rápido</h4>
                    <p><strong>${fastest.nombre}</strong></p>
                    <p>${fastest.tiempoEjecucion}ms</p>
                </div>
            `;
        }

        if (bestQuality) {
            cards += `
                <div style="background: linear-gradient(135deg, #667eea, #764ba2); color: white; padding: 20px; border-radius: 15px; text-align: center;">
                    <h4><i class="fas fa-trophy"></i> Mejor Calidad</h4>
                    <p><strong>${bestQuality.nombre}</strong></p>
                    <p>${bestQuality.puntuacion.toFixed(2)} puntos</p>
                </div>
            `;
        }

        const successRate = allResults.length > 0 ? Math.round(successfulResults.length / allResults.length * 100) : 0;
        cards += `
            <div style="background: linear-gradient(135deg, #f093fb, #f5576c); color: white; padding: 20px; border-radius: 15px; text-align: center;">
                <h4><i class="fas fa-chart-pie"></i> Análisis</h4>
                <p>Algoritmos ejecutados: ${allResults.length}</p>
                <p>Tasa de éxito: ${successRate}%</p>
            </div>
        `;

        return cards;
    }
}

// ==================== INICIALIZACIÓN Y EVENT LISTENERS ====================

class AppInitializer {
    static initialize() {
        document.addEventListener('DOMContentLoaded', this.onDOMContentLoaded.bind(this));
        this.setupGlobalErrorHandling();
    }

    static onDOMContentLoaded() {
        console.log('Initializing Schedule Management System...');
        console.log('API_BASE URL:', CONFIG.API_BASE);

        // Load initial data
        GroupManager.loadExistingGroups();

        // Setup form event listeners
        this.setupFormEventListeners();

        // Setup global click handlers
        this.setupGlobalClickHandlers();
    }

    static setupFormEventListeners() {
        const forms = [
            { id: 'create-group-form', handler: FormManager.handleCreateGroup },
            { id: 'fixed-professors-form', handler: FormManager.handleFixedProfessorsForm },
            { id: 'compare-form', handler: ComparisonManager.handleCompareForm }
        ];

        forms.forEach(({ id, handler }) => {
            const form = document.getElementById(id);
            if (form) {
                form.addEventListener('submit', handler.bind(FormManager));
            }
        });
    }

    static setupGlobalClickHandlers() {
        // Modal close on overlay click
        document.addEventListener('click', (e) => {
            if (e.target.classList.contains('modal-overlay')) {
                ModalManager.closeModal();
            }
        });
    }

    static setupGlobalErrorHandling() {
        window.addEventListener('error', (e) => {
            console.error('Global error:', e.error);
        });

        window.addEventListener('unhandledrejection', (e) => {
            console.error('Unhandled promise rejection:', e.reason);
        });
    }
}

// ==================== FUNCIONES GLOBALES PARA COMPATIBILIDAD ====================

// Export functions to global scope for onclick handlers
window.showTab = (tabName, event) => NavigationManager.showTab(tabName, event);
window.showGroupDetail = (groupName) => ModalManager.showGroupDetail(groupName);
window.closeModal = () => ModalManager.closeModal();
window.addAssignment = () => ProfessorManager.addAssignment();
window.removeAssignment = (button) => ProfessorManager.removeAssignment(button);
window.updateProfessorsForSubject = (selectElement, professorsBySubject) => 
    ProfessorManager.updateProfessorsForSubject(selectElement, professorsBySubject);
window.showDataSection = (section) => DataManager.showDataSection(section);
window.validateSchedules = () => DataManager.validateSchedules();

// Utility functions
window.getAlgorithmName = (algorithm) => CONFIG.ALGORITHM_NAMES[algorithm] || algorithm;

// New functions for schedule generation
window.forceGenerateSchedule = function(groupName, containerId, event) {
    // PREVENIR SCROLL - ESTO ES LO MÁS IMPORTANTE
    if (event) {
        event.preventDefault();
        event.stopPropagation();
    }
    
    console.log('Forzando generación de horario para:', groupName);
    
    // Encontrar el botón
    const button = event ? event.target : document.querySelector(`button[onclick*="forceGenerateSchedule('${groupName}', '${containerId}')"]`);
    let originalHTML = '';
    
    if (button) {
        originalHTML = button.innerHTML;
        button.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Generando horario...';
        button.disabled = true;
    }
    
    // Obtener datos del grupo
    const groupData = AppState.dataCache.groups?.[groupName];
    if (!groupData) {
        console.error('Datos del grupo no disponibles');
        if (button) {
            button.innerHTML = originalHTML;
            button.disabled = false;
        }
        return;
    }
    
    // Generar horario
    const schedule = ScheduleManager.generateBasicSchedule(groupData, true);
    
    if (schedule && schedule.length > 0) {
        // Actualizar contenedor
        const container = document.getElementById(containerId);
        const scheduleContainer = container?.querySelector('.schedule-container');
        
        if (scheduleContainer) {
            scheduleContainer.innerHTML = `
                <h4><i class="fas fa-calendar-week"></i> Horario del Grupo</h4>
                <div class="alert alert-success" style="margin-bottom: 15px;">
                    <i class="fas fa-check-circle"></i> 
                    <strong>¡Horario generado exitosamente!</strong><br>
                    • Total de clases programadas: ${schedule.length}<br>
                    • Horario sintético generado localmente<br>
                    • Las asignaciones fijadas han sido respetadas
                </div>
                ${ScheduleManager.generateScheduleTable(schedule)}
            `;
            
            // Scroll suave al horario
            scheduleContainer.scrollIntoView({ 
                behavior: 'smooth', 
                block: 'start' 
            });
        }
        
        // Actualizar botón
        if (button) {
            button.innerHTML = '<i class="fas fa-check"></i> Horario Generado';
            button.disabled = true;
            button.style.background = '#48bb78';
            button.style.color = 'white';
        }
    } else {
        console.error('No se pudo generar horario');
        if (button) {
            button.innerHTML = originalHTML;
            button.disabled = false;
        }
    }
};

class EventManager {
    // Método para agregar event listeners que eviten el scroll
    static addNoScrollClickListener(element, callback) {
        if (element) {
            element.addEventListener('click', (event) => {
                event.preventDefault();
                event.stopPropagation();
                callback(event);
            });
        }
    }

    // Método para crear botones que no causen scroll
    static createNonScrollButton(text, onclick, className = 'btn btn-primary') {
        const button = document.createElement('button');
        button.type = 'button';  // Importante: tipo button, no submit
        button.className = className;
        button.innerHTML = text;
        
        this.addNoScrollClickListener(button, onclick);
        
        return button;
    }
}

const additionalStyles = `
    <style>
        /* Evitar que los botones causen scroll no deseado */
        .btn {
            cursor: pointer;
            transition: all 0.2s ease;
        }
        
        .btn:disabled {
            cursor: not-allowed;
            opacity: 0.6;
        }
        
        .btn:hover:not(:disabled) {
            transform: translateY(-1px);
            box-shadow: 0 4px 8px rgba(0,0,0,0.15);
        }
        
        /* Smooth scroll para navegación */
        html {
            scroll-behavior: smooth;
        }
        
        /* Mejorar el loading de botones */
        .btn .fa-spinner {
            animation: spin 1s linear infinite;
        }
        
        @keyframes spin {
            from { transform: rotate(0deg); }
            to { transform: rotate(360deg); }
        }
    </style>
`;

if (!document.querySelector('#additional-button-styles')) {
    const styleElement = document.createElement('div');
    styleElement.id = 'additional-button-styles';
    styleElement.innerHTML = additionalStyles;
    document.head.appendChild(styleElement);
}

AppInitializer.initialize();ignment = () => ProfessorManager.addAssignment();
window.removeAssignment = (button) => ProfessorManager.removeAssignment(button);
window.updateProfessorsForSubject = (selectElement, professorsBySubject) => 
    ProfessorManager.updateProfessorsForSubject(selectElement, professorsBySubject);
window.showDataSection = (section) => DataManager.showDataSection(section);
window.validateSchedules = () => DataManager.validateSchedules();

window.getAlgorithmName = (algorithm) => CONFIG.ALGORITHM_NAMES[algorithm] || algorithm;

window.forceGenerateSchedule = (groupName, containerId) => FormManager.forceGenerateSchedule(groupName, containerId);

AppInitializer.initialize();ignment = () => ProfessorManager.addAssignment();
window.removeAssignment = (button) => ProfessorManager.removeAssignment(button);
window.updateProfessorsForSubject = (selectElement, professorsBySubject) => 
    ProfessorManager.updateProfessorsForSubject(selectElement, professorsBySubject);
window.showDataSection = (section) => DataManager.showDataSection(section);
window.validateSchedules = () => DataManager.validateSchedules();

window.getAlgorithmName = (algorithm) => CONFIG.ALGORITHM_NAMES[algorithm] || algorithm;

AppInitializer.initialize();
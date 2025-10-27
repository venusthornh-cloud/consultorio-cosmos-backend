package com.consultorio.controller;

import com.consultorio.model.Turno;
import com.consultorio.repository.TurnoRepository;
import com.consultorio.service.TurnoService;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/turnos")
@CrossOrigin(origins = "*")
public class TurnoController {

    @Autowired
    private TurnoService turnoService;

    @Autowired
    private TurnoRepository turnoRepository;

    @Autowired
    private EntityManager entityManager;

    // === ENDPOINT PARA VER CONEXIÓN ACTUAL ===
    @GetMapping("/debug-conexion")
    public ResponseEntity<?> debugConexion() {
        try {
            System.out.println("=== DEBUG CONEXIÓN COMPLETA ===");

            // 1. Ver BD actual
            String currentDB = (String) entityManager.createNativeQuery("SELECT current_database()").getSingleResult();
            String currentUser = (String) entityManager.createNativeQuery("SELECT current_user").getSingleResult();

            System.out.println("🔗 BD CONECTADA: " + currentDB);
            System.out.println("👤 USUARIO: " + currentUser);

            // 2. Ver counts REALES en la BD conectada
            Long countTurnos = (Long) entityManager.createNativeQuery("SELECT COUNT(*) FROM turnos").getSingleResult();
            Long countPacientes = (Long) entityManager.createNativeQuery("SELECT COUNT(*) FROM pacientes").getSingleResult();
            Long countUsuarios = (Long) entityManager.createNativeQuery("SELECT COUNT(*) FROM usuarios").getSingleResult();

            System.out.println("📊 COUNTS EN BD CONECTADA:");
            System.out.println("   Turnos: " + countTurnos);
            System.out.println("   Pacientes: " + countPacientes);
            System.out.println("   Usuarios: " + countUsuarios);

            // 3. Ver datos de ejemplo
            List<Object[]> ejemplos = entityManager.createNativeQuery(
                    "SELECT id, paciente_id, fecha FROM turnos LIMIT 3"
            ).getResultList();

            System.out.println("🔍 Ejemplos de turnos:");
            for (Object[] ejemplo : ejemplos) {
                System.out.println("   🗓️ ID: " + ejemplo[0] + ", PacienteID: " + ejemplo[1] + ", Fecha: " + ejemplo[2]);
            }

            return ResponseEntity.ok(Map.of(
                    "bd_actual", currentDB,
                    "usuario", currentUser,
                    "turnos_count", countTurnos,
                    "pacientes_count", countPacientes,
                    "usuarios_count", countUsuarios,
                    "ejemplos_turnos", ejemplos.size()
            ));

        } catch (Exception e) {
            System.out.println("❌ ERROR: " + e.getMessage());
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    // === ENDPOINT DE EMERGENCIA - MOSTRAR TODOS LOS TURNOS ===
    @GetMapping("/todos")
    public ResponseEntity<List<Turno>> getAllTurnosCompleto() {
        try {
            System.out.println("=== TODOS LOS TURNOS ===");

            // 1. Obtener TODOS los turnos sin filtros
            List<Turno> todosTurnos = turnoRepository.findAll();
            System.out.println("🔍 Total turnos en BD: " + todosTurnos.size());

            // 2. Cargar datos de pacientes para cada turno
            for (Turno turno : todosTurnos) {
                // Consulta directa para obtener datos del paciente
                List<Object[]> resultados = entityManager.createNativeQuery(
                                "SELECT u.nombre, u.apellido, u.dni " +
                                        "FROM pacientes p " +
                                        "JOIN usuarios u ON p.usuario_id = u.id " +
                                        "WHERE p.id = ?1"
                        ).setParameter(1, turno.getPacienteId())
                        .getResultList();

                if (!resultados.isEmpty()) {
                    Object[] datos = resultados.get(0);
                    turno.setNombrePaciente((String) datos[0]);
                    turno.setApellidoPaciente((String) datos[1]);
                    turno.setDniPaciente((String) datos[2]);

                    System.out.println("   ✅ Turno ID: " + turno.getId() +
                            " - Fecha: " + turno.getFecha() +
                            " - Paciente: " + turno.getNombreCompletoPaciente());
                } else {
                    System.out.println("   ❌ Turno ID: " + turno.getId() + " - SIN DATOS DE PACIENTE");
                }
            }

            return ResponseEntity.ok(todosTurnos);

        } catch (Exception e) {
            System.out.println("❌ ERROR: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    // === ENDPOINT PARA VER FECHAS REALES ===
    @GetMapping("/debug-fechas")
    public ResponseEntity<?> debugFechas() {
        try {
            System.out.println("=== DEBUG FECHAS ===");

            // Ver fechas REALES en la BD
            List<Object[]> fechasTurnos = entityManager.createNativeQuery(
                    "SELECT id, paciente_id, fecha, estado FROM turnos ORDER BY fecha LIMIT 10"
            ).getResultList();

            System.out.println("🔍 Fechas REALES en BD:");
            for (Object[] turno : fechasTurnos) {
                System.out.println("   📅 ID: " + turno[0] +
                        ", Paciente: " + turno[1] +
                        ", Fecha: " + turno[2] +
                        ", Estado: " + turno[3]);
            }

            // Ver fecha actual del sistema
            LocalDate hoy = LocalDate.now();
            System.out.println("📅 Fecha actual del sistema: " + hoy);

            return ResponseEntity.ok(Map.of(
                    "fecha_actual_sistema", hoy.toString(),
                    "total_turnos_en_bd", fechasTurnos.size(),
                    "turnos_muestra", fechasTurnos
            ));

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    // === ENDPOINT NUEVO CORREGIDO ===
    @GetMapping("/proximos-corregidos")
    public ResponseEntity<List<Turno>> getProximosTurnosCorregidos() {
        try {
            System.out.println("=== PROXIMOS TURNOS CORREGIDOS ===");
            List<Turno> turnos = turnoService.getProximosTurnos();

            System.out.println("🎯 RESULTADO: " + turnos.size() + " turnos cargados");
            for (Turno turno : turnos) {
                System.out.println("   ✅ " + turno.getId() + " - " +
                        turno.getNombreCompletoPaciente() + " - " +
                        turno.getFecha());
            }

            return ResponseEntity.ok(turnos);
        } catch (Exception e) {
            System.out.println("❌ ERROR: " + e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    // === ENDPOINT DE DEBUG MEJORADO ===
    @GetMapping("/debug-final")
    public ResponseEntity<?> debugFinal() {
        try {
            System.out.println("=== DEBUG FINAL INICIADO ===");

            // 1. Verificar datos REALES con JOIN
            List<Object[]> turnosConDatos = entityManager.createNativeQuery(
                    "SELECT t.id, t.paciente_id, t.fecha, t.estado, " +
                            "u.nombre, u.apellido, u.dni " +
                            "FROM turnos t " +
                            "LEFT JOIN pacientes p ON t.paciente_id = p.id " +
                            "LEFT JOIN usuarios u ON p.usuario_id = u.id " +
                            "WHERE t.fecha >= CURRENT_DATE " +
                            "ORDER BY t.fecha, t.hora_inicio " +
                            "LIMIT 10"
            ).getResultList();

            System.out.println("🔍 Turnos con datos REALES (JOIN directo):");
            for (Object[] turno : turnosConDatos) {
                System.out.println("   🗓️ Turno ID: " + turno[0] +
                        ", PacienteID: " + turno[1] +
                        ", Fecha: " + turno[2] +
                        ", Estado: " + turno[3] +
                        ", Nombre: " + turno[4] + " " + turno[5] +
                        ", DNI: " + turno[6]);
            }

            // 2. Probar el servicio corregido
            List<Turno> turnosServicio = turnoService.getProximosTurnos();
            System.out.println("🔍 Turnos desde Servicio: " + turnosServicio.size());

            return ResponseEntity.ok(Map.of(
                    "turnos_directos_bd", turnosConDatos.size(),
                    "turnos_servicio", turnosServicio.size(),
                    "estado", "DEBUG COMPLETADO"
            ));

        } catch (Exception e) {
            System.out.println("❌ ERROR en debug-final: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    // === ENDPOINT ORIGINAL DE DEBUG ===
    @GetMapping("/debug-connection")
    public String debugConnection() {
        try {
            System.out.println("=== DEBUG CONEXIÓN INICIADO ===");

            // 1. Consulta nativa COUNT
            Long countNative = turnoRepository.countNative();
            System.out.println("🔍 Consulta nativa COUNT: " + countNative);

            // 2. Consulta nativa para ver datos
            List<Object[]> turnosNativos = turnoRepository.findFirstFiveNative();
            System.out.println("🔍 Consulta nativa datos: " + turnosNativos.size() + " registros");

            // 3. Mostrar datos nativos
            for (Object[] turno : turnosNativos) {
                System.out.println("   📊 Turno nativo: " + Arrays.toString(turno));
            }

            // 4. Consulta JPA
            List<Turno> turnosJPA = turnoRepository.findAll();
            System.out.println("🔍 Consulta JPA findAll: " + turnosJPA.size() + " registros");

            String resultado = "=== DEBUG CONEXIÓN ===\n" +
                    "Consulta nativa COUNT: " + countNative + "\n" +
                    "Consulta nativa datos: " + turnosNativos.size() + " registros\n" +
                    "Consulta JPA findAll: " + turnosJPA.size() + " registros\n" +
                    "¿Ambas consultas devuelven 0? " + (countNative == 0 && turnosJPA.size() == 0);

            System.out.println("🎯 RESULTADO: " + resultado);
            return resultado;

        } catch (Exception e) {
            String error = "❌ ERROR: " + e.getMessage();
            System.out.println(error);
            e.printStackTrace();
            return error;
        }
    }

    // === ENDPOINTS ORIGINALES ===
    @GetMapping
    public List<Turno> getAllTurnos() {
        return turnoService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Turno> getTurnoById(@PathVariable Long id) {
        Optional<Turno> turno = turnoService.findById(id);
        return turno.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/proximos")
    public List<Turno> getProximosTurnos() {
        return turnoService.getProximosTurnos();
    }

    @GetMapping("/fecha/{fecha}")
    public List<Turno> getTurnosPorFecha(@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return turnoService.getTurnosPorFecha(fecha);
    }

    @GetMapping("/paciente/{pacienteId}")
    public List<Turno> getTurnosPorPaciente(@PathVariable Long pacienteId) {
        return turnoService.getTurnosPorPaciente(pacienteId);
    }

    @PostMapping
    public Turno createTurno(@RequestBody Turno turno) {
        return turnoService.save(turno);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Turno> updateTurno(@PathVariable Long id, @RequestBody Turno turno) {
        try {
            Turno turnoActualizado = turnoService.update(id, turno);
            return ResponseEntity.ok(turnoActualizado);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<Turno> cambiarEstado(@PathVariable Long id, @RequestParam Turno.EstadoTurno estado) {
        try {
            Turno turno = turnoService.cambiarEstado(id, estado);
            return ResponseEntity.ok(turno);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTurno(@PathVariable Long id) {
        try {
            turnoService.delete(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/disponibilidad")
    public ResponseEntity<Boolean> verificarDisponibilidad(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime horaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime horaFin) {
        boolean disponible = turnoService.estaDisponible(fecha, horaInicio, horaFin);
        return ResponseEntity.ok(disponible);
    }
}
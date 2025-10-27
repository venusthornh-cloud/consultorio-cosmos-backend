package com.consultorio.service;

import com.consultorio.model.Turno;
import com.consultorio.repository.TurnoRepository;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class TurnoService {

    @Autowired
    private TurnoRepository turnoRepository;

    @Autowired
    private EntityManager entityManager;

    // ✅ MÉTODO MEJORADO para cargar datos de pacientes
    private void cargarDatosPaciente(Turno turno) {
        if (turno.getTipo() == Turno.TipoTurno.NORMAL) {
            try {
                // Consulta nativa para obtener datos del paciente
                List<Object[]> resultados = entityManager.createNativeQuery(
                                "SELECT u.nombre, u.apellido, u.dni " +
                                        "FROM turnos t " +
                                        "JOIN pacientes p ON t.paciente_id = p.id " +
                                        "JOIN usuarios u ON p.usuario_id = u.id " +
                                        "WHERE t.id = ?1"
                        ).setParameter(1, turno.getId())
                        .getResultList();

                if (!resultados.isEmpty()) {
                    Object[] datos = resultados.get(0);
                    turno.setNombrePaciente((String) datos[0]);
                    turno.setApellidoPaciente((String) datos[1]);
                    turno.setDniPaciente((String) datos[2]);
                }
            } catch (Exception e) {
                System.out.println("⚠️ Error cargando datos paciente para turno " + turno.getId() + ": " + e.getMessage());
            }
        }
    }

    // ✅ MÉTODO MEJORADO para obtener todos los turnos
    public List<Turno> findAll() {
        List<Turno> turnos = turnoRepository.findAll();
        for (Turno turno : turnos) {
            cargarDatosPaciente(turno);
        }
        return turnos;
    }

    // ✅ MÉTODO MEJORADO para próximos turnos
    public List<Turno> getProximosTurnos() {
        try {
            System.out.println("=== GET PROXIMOS TURNOS INICIADO ===");

            // Obtener turnos futuros
            LocalDate hoy = LocalDate.now();
            List<Turno> turnosFuturos = turnoRepository.findProximosTurnos(hoy);

            System.out.println("🔍 Turnos futuros encontrados: " + turnosFuturos.size());

            // Cargar datos de pacientes para cada turno
            for (Turno turno : turnosFuturos) {
                cargarDatosPaciente(turno);
                System.out.println("   ✅ Turno ID: " + turno.getId() +
                        ", Paciente: " + turno.getNombreCompletoPaciente() +
                        ", Fecha: " + turno.getFecha());
            }

            return turnosFuturos;

        } catch (Exception e) {
            System.out.println("❌ ERROR en getProximosTurnos: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // Resto de métodos se mantienen igual
    public Optional<Turno> findById(Long id) {
        Optional<Turno> turno = turnoRepository.findById(id);
        turno.ifPresent(this::cargarDatosPaciente);
        return turno;
    }

    public Turno save(Turno turno) {
        Turno saved = turnoRepository.save(turno);
        cargarDatosPaciente(saved);
        return saved;
    }

    public Turno update(Long id, Turno turnoActualizado) {
        return turnoRepository.findById(id)
                .map(turnoExistente -> {
                    turnoExistente.setFecha(turnoActualizado.getFecha());
                    turnoExistente.setHoraInicio(turnoActualizado.getHoraInicio());
                    turnoExistente.setHoraFin(turnoActualizado.getHoraFin());
                    turnoExistente.setEstado(turnoActualizado.getEstado());
                    turnoExistente.setMonto(turnoActualizado.getMonto());
                    turnoExistente.setPagoConfirmado(turnoActualizado.getPagoConfirmado());
                    turnoExistente.setActualizadoPor("sistema");
                    Turno updated = turnoRepository.save(turnoExistente);
                    cargarDatosPaciente(updated);
                    return updated;
                })
                .orElseThrow(() -> new RuntimeException("Turno no encontrado con ID: " + id));
    }

    public void delete(Long id) {
        Turno turno = turnoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Turno no encontrado con ID: " + id));
        turnoRepository.delete(turno);
    }

    public List<Turno> getTurnosPorFecha(LocalDate fecha) {
        List<Turno> turnos = turnoRepository.findByFecha(fecha);
        for (Turno turno : turnos) {
            cargarDatosPaciente(turno);
        }
        return turnos;
    }

    public List<Turno> getTurnosPorPaciente(Long pacienteId) {
        List<Turno> turnos = turnoRepository.findByPacienteId(pacienteId);
        for (Turno turno : turnos) {
            cargarDatosPaciente(turno);
        }
        return turnos;
    }

    public Turno cambiarEstado(Long id, Turno.EstadoTurno estado) {
        return turnoRepository.findById(id)
                .map(turno -> {
                    turno.setEstado(estado);
                    turno.setActualizadoPor("sistema");
                    Turno updated = turnoRepository.save(turno);
                    cargarDatosPaciente(updated);
                    return updated;
                })
                .orElseThrow(() -> new RuntimeException("Turno no encontrado con ID: " + id));
    }

    public boolean estaDisponible(LocalDate fecha, LocalTime horaInicio, LocalTime horaFin) {
        Long count = turnoRepository.countTurnosEnHorario(fecha, horaInicio, horaFin);
        return count == 0;
    }
}
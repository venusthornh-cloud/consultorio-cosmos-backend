package com.consultorio.repository;

import com.consultorio.model.Turno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface TurnoRepository extends JpaRepository<Turno, Long> {

    // Encontrar turnos por fecha
    List<Turno> findByFecha(LocalDate fecha);

    // Encontrar turnos por paciente
    List<Turno> findByPacienteId(Long pacienteId);

    // Encontrar turnos por estado
    List<Turno> findByEstado(Turno.EstadoTurno estado);

    // Encontrar próximos turnos (fecha >= hoy)
    @Query("SELECT t FROM Turno t WHERE t.fecha >= :fechaHoy  ORDER BY t.fecha, t.horaInicio")
    List<Turno> findProximosTurnos(@Param("fechaHoy") LocalDate fechaHoy);

    // Encontrar turnos en un rango de fechas
    @Query("SELECT t FROM Turno t WHERE t.fecha BETWEEN :fechaInicio AND :fechaFin ORDER BY t.fecha, t.horaInicio")
    List<Turno> findByFechaBetween(@Param("fechaInicio") LocalDate fechaInicio,
                                   @Param("fechaFin") LocalDate fechaFin);

    // Verificar disponibilidad de horario
    @Query("SELECT COUNT(t) FROM Turno t WHERE t.fecha = :fecha AND " +
            "((t.horaInicio <= :horaFin AND t.horaFin >= :horaInicio) AND t.estado IN ('CONFIRMADO', 'ASISTIDO'))")
    Long countTurnosEnHorario(@Param("fecha") LocalDate fecha,
                              @Param("horaInicio") LocalTime horaInicio,
                              @Param("horaFin") LocalTime horaFin);

    @Query(value = "SELECT COUNT(*) FROM turnos", nativeQuery = true)
    Long countNative();

    @Query(value = "SELECT * FROM turnos LIMIT 5", nativeQuery = true)
    List<Object[]> findFirstFiveNative();
}
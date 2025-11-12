package com.contactoprofesionales.dao;

import com.contactoprofesionales.dto.ServiciosProfesionalCompleto;
import com.contactoprofesionales.model.*;
import com.contactoprofesionales.util.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementación del DAO para gestionar servicios profesionales.
 * Maneja operaciones transaccionales complejas.
 */
public class ServiciosProfesionalDAOImpl implements ServiciosProfesionalDAO {

    private static final Logger logger = LoggerFactory.getLogger(ServiciosProfesionalDAOImpl.class);

    @Override
    public boolean guardarServiciosProfesional(Integer profesionalId,
                                              List<EspecialidadProfesional> especialidades,
                                              AreaServicio areaServicio,
                                              DisponibilidadHoraria disponibilidad) throws Exception {

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Iniciar transacción

            // Validaciones
            if (especialidades == null || especialidades.isEmpty()) {
                throw new IllegalArgumentException("Debe proporcionar al menos una especialidad");
            }
            if (especialidades.size() > 3) {
                throw new IllegalArgumentException("No puede registrar más de 3 especialidades");
            }
            if (areaServicio == null) {
                throw new IllegalArgumentException("Debe configurar el área de servicio");
            }
            if (disponibilidad == null) {
                throw new IllegalArgumentException("Debe configurar la disponibilidad horaria");
            }

            // 1. Guardar especialidades
            guardarEspecialidadesInterno(conn, profesionalId, especialidades);

            // 2. Guardar área de servicio
            areaServicio.setProfesionalId(profesionalId);
            guardarAreaServicioInterno(conn, areaServicio);

            // 3. Guardar disponibilidad
            disponibilidad.setProfesionalId(profesionalId);
            guardarDisponibilidadInterno(conn, disponibilidad);

            conn.commit(); // Confirmar transacción
            logger.info("Servicios guardados exitosamente para profesional {}", profesionalId);
            return true;

        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback(); // Revertir cambios
                    logger.error("Rollback realizado para profesional {}", profesionalId);
                } catch (SQLException ex) {
                    logger.error("Error en rollback", ex);
                }
            }
            logger.error("Error guardando servicios profesionales", e);
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    logger.error("Error restaurando autocommit", e);
                }
            }
        }
    }

    @Override
    public boolean actualizarServiciosProfesional(Integer profesionalId,
                                                 List<EspecialidadProfesional> especialidades,
                                                 AreaServicio areaServicio,
                                                 DisponibilidadHoraria disponibilidad) throws Exception {

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // Eliminar datos existentes
            eliminarEspecialidadesPorProfesionalInterno(conn, profesionalId);
            eliminarAreaServicioPorProfesionalInterno(conn, profesionalId);
            eliminarDisponibilidadPorProfesionalInterno(conn, profesionalId);

            // Guardar nuevos datos
            if (especialidades != null && !especialidades.isEmpty()) {
                guardarEspecialidadesInterno(conn, profesionalId, especialidades);
            }
            if (areaServicio != null) {
                areaServicio.setProfesionalId(profesionalId);
                guardarAreaServicioInterno(conn, areaServicio);
            }
            if (disponibilidad != null) {
                disponibilidad.setProfesionalId(profesionalId);
                guardarDisponibilidadInterno(conn, disponibilidad);
            }

            conn.commit();
            logger.info("Servicios actualizados exitosamente para profesional {}", profesionalId);
            return true;

        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    logger.error("Error en rollback", ex);
                }
            }
            logger.error("Error actualizando servicios profesionales", e);
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    logger.error("Error restaurando autocommit", e);
                }
            }
        }
    }

    @Override
    public ServiciosProfesionalCompleto obtenerServiciosProfesional(Integer profesionalId) throws Exception {
        try {
            ServiciosProfesionalCompleto servicios = new ServiciosProfesionalCompleto();
            servicios.setProfesionalId(profesionalId);

            // Obtener especialidades
            List<EspecialidadProfesional> especialidades = obtenerEspecialidadesPorProfesional(profesionalId);
            servicios.setEspecialidades(especialidades);

            // Obtener área de servicio
            AreaServicio areaServicio = obtenerAreaServicioPorProfesional(profesionalId);
            servicios.setAreaServicio(areaServicio);

            // Obtener disponibilidad
            DisponibilidadHoraria disponibilidad = obtenerDisponibilidadPorProfesional(profesionalId);
            servicios.setDisponibilidad(disponibilidad);

            return servicios;

        } catch (Exception e) {
            logger.error("Error obteniendo servicios del profesional {}", profesionalId, e);
            throw e;
        }
    }

    @Override
    public boolean tieneServiciosConfigurados(Integer profesionalId) throws Exception {
        String sql = "SELECT COUNT(*) FROM especialidades_profesional WHERE profesional_id = ? AND activo = TRUE";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, profesionalId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;

        } catch (SQLException e) {
            logger.error("Error verificando servicios del profesional {}", profesionalId, e);
            throw e;
        }
    }

    @Override
    public boolean eliminarServiciosProfesional(Integer profesionalId) throws Exception {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            eliminarEspecialidadesPorProfesionalInterno(conn, profesionalId);
            eliminarAreaServicioPorProfesionalInterno(conn, profesionalId);
            eliminarDisponibilidadPorProfesionalInterno(conn, profesionalId);

            conn.commit();
            logger.info("Servicios eliminados para profesional {}", profesionalId);
            return true;

        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    logger.error("Error en rollback", ex);
                }
            }
            logger.error("Error eliminando servicios", e);
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    logger.error("Error restaurando autocommit", e);
                }
            }
        }
    }

    // =====================================================================
    // ESPECIALIDADES
    // =====================================================================

    @Override
    public boolean guardarEspecialidades(Integer profesionalId, List<EspecialidadProfesional> especialidades) throws Exception {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return guardarEspecialidadesInterno(conn, profesionalId, especialidades);
        }
    }

    private boolean guardarEspecialidadesInterno(Connection conn, Integer profesionalId,
                                                List<EspecialidadProfesional> especialidades) throws SQLException {

        String sql = "INSERT INTO especialidades_profesional " +
                    "(profesional_id, nombre_especialidad, descripcion, incluye_materiales, " +
                    "costo, tipo_costo, es_principal, orden) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            for (EspecialidadProfesional esp : especialidades) {
                esp.setProfesionalId(profesionalId);

                stmt.setInt(1, profesionalId);
                stmt.setString(2, esp.getNombreEspecialidad());
                stmt.setString(3, esp.getDescripcion());
                stmt.setBoolean(4, esp.getIncluyeMateriales());
                stmt.setDouble(5, esp.getCosto());
                stmt.setString(6, esp.getTipoCosto());
                stmt.setBoolean(7, esp.getEsPrincipal());
                stmt.setInt(8, esp.getOrden());

                stmt.executeUpdate();

                // Obtener ID generado
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    esp.setId(rs.getInt(1));
                }
            }
            return true;
        }
    }

    @Override
    public List<EspecialidadProfesional> obtenerEspecialidadesPorProfesional(Integer profesionalId) throws Exception {
        String sql = "SELECT * FROM especialidades_profesional " +
                    "WHERE profesional_id = ? AND activo = TRUE " +
                    "ORDER BY orden ASC";

        List<EspecialidadProfesional> especialidades = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, profesionalId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                especialidades.add(mapearEspecialidad(rs));
            }

            return especialidades;

        } catch (SQLException e) {
            logger.error("Error obteniendo especialidades", e);
            throw e;
        }
    }

    @Override
    public boolean eliminarEspecialidadesPorProfesional(Integer profesionalId) throws Exception {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return eliminarEspecialidadesPorProfesionalInterno(conn, profesionalId);
        }
    }

    private boolean eliminarEspecialidadesPorProfesionalInterno(Connection conn, Integer profesionalId) throws SQLException {
        String sql = "UPDATE especialidades_profesional SET activo = FALSE WHERE profesional_id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, profesionalId);
            stmt.executeUpdate();
            return true;
        }
    }

    private EspecialidadProfesional mapearEspecialidad(ResultSet rs) throws SQLException {
        EspecialidadProfesional esp = new EspecialidadProfesional();
        esp.setId(rs.getInt("id"));
        esp.setProfesionalId(rs.getInt("profesional_id"));
        esp.setNombreEspecialidad(rs.getString("nombre_especialidad"));
        esp.setDescripcion(rs.getString("descripcion"));
        esp.setIncluyeMateriales(rs.getBoolean("incluye_materiales"));
        esp.setCosto(rs.getDouble("costo"));
        esp.setTipoCosto(rs.getString("tipo_costo"));
        esp.setEsPrincipal(rs.getBoolean("es_principal"));
        esp.setOrden(rs.getInt("orden"));
        esp.setFechaCreacion(rs.getTimestamp("fecha_creacion").toLocalDateTime());
        esp.setFechaActualizacion(rs.getTimestamp("fecha_actualizacion").toLocalDateTime());
        esp.setActivo(rs.getBoolean("activo"));
        return esp;
    }

    // =====================================================================
    // ÁREA DE SERVICIO
    // =====================================================================

    @Override
    public boolean guardarAreaServicio(AreaServicio areaServicio) throws Exception {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                guardarAreaServicioInterno(conn, areaServicio);
                conn.commit();
                return true;
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    private boolean guardarAreaServicioInterno(Connection conn, AreaServicio areaServicio) throws SQLException {
        // 1. Insertar área de servicio
        String sqlArea = "INSERT INTO areas_servicio (profesional_id, todo_pais) VALUES (?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sqlArea, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, areaServicio.getProfesionalId());
            stmt.setBoolean(2, areaServicio.getTodoPais());
            stmt.executeUpdate();

            // Obtener ID generado
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                areaServicio.setId(rs.getInt(1));
            }
        }

        // 2. Si no es todo el país, insertar ubicaciones específicas
        if (!areaServicio.getTodoPais() && areaServicio.getUbicaciones() != null && !areaServicio.getUbicaciones().isEmpty()) {
            String sqlUbicacion = "INSERT INTO ubicaciones_servicio " +
                                 "(area_servicio_id, tipo_ubicacion, departamento, provincia, distrito, orden) " +
                                 "VALUES (?, ?, ?, ?, ?, ?)";

            try (PreparedStatement stmt = conn.prepareStatement(sqlUbicacion, Statement.RETURN_GENERATED_KEYS)) {
                for (UbicacionServicio ubicacion : areaServicio.getUbicaciones()) {
                    ubicacion.setAreaServicioId(areaServicio.getId());

                    stmt.setInt(1, areaServicio.getId());
                    stmt.setString(2, ubicacion.getTipoUbicacion());
                    stmt.setString(3, ubicacion.getDepartamento());
                    stmt.setString(4, ubicacion.getProvincia());
                    stmt.setString(5, ubicacion.getDistrito());
                    stmt.setInt(6, ubicacion.getOrden());
                    stmt.executeUpdate();

                    ResultSet rs = stmt.getGeneratedKeys();
                    if (rs.next()) {
                        ubicacion.setId(rs.getInt(1));
                    }
                }
            }
        }

        return true;
    }

    @Override
    public AreaServicio obtenerAreaServicioPorProfesional(Integer profesionalId) throws Exception {
        String sql = "SELECT * FROM areas_servicio WHERE profesional_id = ? AND activo = TRUE";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, profesionalId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                AreaServicio areaServicio = mapearAreaServicio(rs);

                // Obtener ubicaciones si no es todo el país
                if (!areaServicio.getTodoPais()) {
                    List<UbicacionServicio> ubicaciones = obtenerUbicacionesPorArea(areaServicio.getId());
                    areaServicio.setUbicaciones(ubicaciones);
                }

                return areaServicio;
            }

            return null;

        } catch (SQLException e) {
            logger.error("Error obteniendo área de servicio", e);
            throw e;
        }
    }

    private List<UbicacionServicio> obtenerUbicacionesPorArea(Integer areaServicioId) throws SQLException {
        String sql = "SELECT * FROM ubicaciones_servicio " +
                    "WHERE area_servicio_id = ? AND activo = TRUE " +
                    "ORDER BY orden ASC";

        List<UbicacionServicio> ubicaciones = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, areaServicioId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                ubicaciones.add(mapearUbicacion(rs));
            }
        }

        return ubicaciones;
    }

    @Override
    public boolean eliminarAreaServicioPorProfesional(Integer profesionalId) throws Exception {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return eliminarAreaServicioPorProfesionalInterno(conn, profesionalId);
        }
    }

    private boolean eliminarAreaServicioPorProfesionalInterno(Connection conn, Integer profesionalId) throws SQLException {
        // Primero, obtener el ID del área de servicio
        String sqlGetId = "SELECT id FROM areas_servicio WHERE profesional_id = ? AND activo = TRUE";
        Integer areaServicioId = null;

        try (PreparedStatement stmt = conn.prepareStatement(sqlGetId)) {
            stmt.setInt(1, profesionalId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                areaServicioId = rs.getInt("id");
            }
        }

        if (areaServicioId != null) {
            // Desactivar ubicaciones
            String sqlUbicaciones = "UPDATE ubicaciones_servicio SET activo = FALSE WHERE area_servicio_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sqlUbicaciones)) {
                stmt.setInt(1, areaServicioId);
                stmt.executeUpdate();
            }

            // Desactivar área de servicio
            String sqlArea = "UPDATE areas_servicio SET activo = FALSE WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sqlArea)) {
                stmt.setInt(1, areaServicioId);
                stmt.executeUpdate();
            }
        }

        return true;
    }

    private AreaServicio mapearAreaServicio(ResultSet rs) throws SQLException {
        AreaServicio area = new AreaServicio();
        area.setId(rs.getInt("id"));
        area.setProfesionalId(rs.getInt("profesional_id"));
        area.setTodoPais(rs.getBoolean("todo_pais"));
        area.setFechaCreacion(rs.getTimestamp("fecha_creacion").toLocalDateTime());
        area.setFechaActualizacion(rs.getTimestamp("fecha_actualizacion").toLocalDateTime());
        area.setActivo(rs.getBoolean("activo"));
        return area;
    }

    private UbicacionServicio mapearUbicacion(ResultSet rs) throws SQLException {
        UbicacionServicio ubicacion = new UbicacionServicio();
        ubicacion.setId(rs.getInt("id"));
        ubicacion.setAreaServicioId(rs.getInt("area_servicio_id"));
        ubicacion.setTipoUbicacion(rs.getString("tipo_ubicacion"));
        ubicacion.setDepartamento(rs.getString("departamento"));
        ubicacion.setProvincia(rs.getString("provincia"));
        ubicacion.setDistrito(rs.getString("distrito"));
        ubicacion.setOrden(rs.getInt("orden"));
        ubicacion.setFechaCreacion(rs.getTimestamp("fecha_creacion").toLocalDateTime());
        ubicacion.setActivo(rs.getBoolean("activo"));
        return ubicacion;
    }

    // =====================================================================
    // DISPONIBILIDAD HORARIA
    // =====================================================================

    @Override
    public boolean guardarDisponibilidad(DisponibilidadHoraria disponibilidad) throws Exception {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                guardarDisponibilidadInterno(conn, disponibilidad);
                conn.commit();
                return true;
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    private boolean guardarDisponibilidadInterno(Connection conn, DisponibilidadHoraria disponibilidad) throws SQLException {
        // 1. Insertar disponibilidad
        String sqlDisp = "INSERT INTO disponibilidad_horaria (profesional_id, todo_tiempo) VALUES (?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sqlDisp, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, disponibilidad.getProfesionalId());
            stmt.setBoolean(2, disponibilidad.getTodoTiempo());
            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                disponibilidad.setId(rs.getInt(1));
            }
        }

        // 2. Si no es todo el tiempo, insertar horarios específicos
        if (!disponibilidad.getTodoTiempo() && disponibilidad.getHorariosDias() != null && !disponibilidad.getHorariosDias().isEmpty()) {
            String sqlHorario = "INSERT INTO horarios_dia " +
                               "(disponibilidad_id, dia_semana, tipo_jornada, hora_inicio, hora_fin) " +
                               "VALUES (?, ?, ?, ?, ?)";

            try (PreparedStatement stmt = conn.prepareStatement(sqlHorario, Statement.RETURN_GENERATED_KEYS)) {
                for (HorarioDia horario : disponibilidad.getHorariosDias()) {
                    horario.setDisponibilidadId(disponibilidad.getId());

                    stmt.setInt(1, disponibilidad.getId());
                    stmt.setString(2, horario.getDiaSemana());
                    stmt.setString(3, horario.getTipoJornada());

                    if (horario.getHoraInicio() != null) {
                        stmt.setTime(4, Time.valueOf(horario.getHoraInicio()));
                    } else {
                        stmt.setNull(4, Types.TIME);
                    }

                    if (horario.getHoraFin() != null) {
                        stmt.setTime(5, Time.valueOf(horario.getHoraFin()));
                    } else {
                        stmt.setNull(5, Types.TIME);
                    }

                    stmt.executeUpdate();

                    ResultSet rs = stmt.getGeneratedKeys();
                    if (rs.next()) {
                        horario.setId(rs.getInt(1));
                    }
                }
            }
        }

        return true;
    }

    @Override
    public DisponibilidadHoraria obtenerDisponibilidadPorProfesional(Integer profesionalId) throws Exception {
        String sql = "SELECT * FROM disponibilidad_horaria WHERE profesional_id = ? AND activo = TRUE";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, profesionalId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                DisponibilidadHoraria disponibilidad = mapearDisponibilidad(rs);

                // Obtener horarios si no es todo el tiempo
                if (!disponibilidad.getTodoTiempo()) {
                    List<HorarioDia> horarios = obtenerHorariosPorDisponibilidad(disponibilidad.getId());
                    disponibilidad.setHorariosDias(horarios);
                }

                return disponibilidad;
            }

            return null;

        } catch (SQLException e) {
            logger.error("Error obteniendo disponibilidad", e);
            throw e;
        }
    }

    private List<HorarioDia> obtenerHorariosPorDisponibilidad(Integer disponibilidadId) throws SQLException {
        String sql = "SELECT * FROM horarios_dia WHERE disponibilidad_id = ? AND activo = TRUE";

        List<HorarioDia> horarios = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, disponibilidadId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                horarios.add(mapearHorarioDia(rs));
            }
        }

        return horarios;
    }

    @Override
    public boolean eliminarDisponibilidadPorProfesional(Integer profesionalId) throws Exception {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return eliminarDisponibilidadPorProfesionalInterno(conn, profesionalId);
        }
    }

    private boolean eliminarDisponibilidadPorProfesionalInterno(Connection conn, Integer profesionalId) throws SQLException {
        // Obtener ID de disponibilidad
        String sqlGetId = "SELECT id FROM disponibilidad_horaria WHERE profesional_id = ? AND activo = TRUE";
        Integer disponibilidadId = null;

        try (PreparedStatement stmt = conn.prepareStatement(sqlGetId)) {
            stmt.setInt(1, profesionalId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                disponibilidadId = rs.getInt("id");
            }
        }

        if (disponibilidadId != null) {
            // Desactivar horarios
            String sqlHorarios = "UPDATE horarios_dia SET activo = FALSE WHERE disponibilidad_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sqlHorarios)) {
                stmt.setInt(1, disponibilidadId);
                stmt.executeUpdate();
            }

            // Desactivar disponibilidad
            String sqlDisp = "UPDATE disponibilidad_horaria SET activo = FALSE WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sqlDisp)) {
                stmt.setInt(1, disponibilidadId);
                stmt.executeUpdate();
            }
        }

        return true;
    }

    private DisponibilidadHoraria mapearDisponibilidad(ResultSet rs) throws SQLException {
        DisponibilidadHoraria disp = new DisponibilidadHoraria();
        disp.setId(rs.getInt("id"));
        disp.setProfesionalId(rs.getInt("profesional_id"));
        disp.setTodoTiempo(rs.getBoolean("todo_tiempo"));
        disp.setFechaCreacion(rs.getTimestamp("fecha_creacion").toLocalDateTime());
        disp.setFechaActualizacion(rs.getTimestamp("fecha_actualizacion").toLocalDateTime());
        disp.setActivo(rs.getBoolean("activo"));
        return disp;
    }

    private HorarioDia mapearHorarioDia(ResultSet rs) throws SQLException {
        HorarioDia horario = new HorarioDia();
        horario.setId(rs.getInt("id"));
        horario.setDisponibilidadId(rs.getInt("disponibilidad_id"));
        horario.setDiaSemana(rs.getString("dia_semana"));
        horario.setTipoJornada(rs.getString("tipo_jornada"));

        Time horaInicio = rs.getTime("hora_inicio");
        if (horaInicio != null) {
            horario.setHoraInicio(horaInicio.toLocalTime());
        }

        Time horaFin = rs.getTime("hora_fin");
        if (horaFin != null) {
            horario.setHoraFin(horaFin.toLocalTime());
        }

        horario.setActivo(rs.getBoolean("activo"));
        return horario;
    }
}

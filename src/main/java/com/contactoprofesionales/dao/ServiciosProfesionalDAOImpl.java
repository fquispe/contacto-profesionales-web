package com.contactoprofesionales.dao;

import com.contactoprofesionales.dto.ServiciosProfesionalCompleto;
import com.contactoprofesionales.model.*;
import com.contactoprofesionales.util.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
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

    // ✅ ACTUALIZADO: Lógica de actualización dinámica con soft delete (actualizado: 2025-11-15)
    @Override
    public boolean actualizarServiciosProfesional(Integer profesionalId,
                                                 List<EspecialidadProfesional> especialidades,
                                                 AreaServicio areaServicio,
                                                 DisponibilidadHoraria disponibilidad) throws Exception {

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // ✅ CAMBIO IMPORTANTE: Actualización inteligente de especialidades con soft delete
            // En lugar de eliminar todo y reinsertar, ahora:
            // 1. Marca como inactivas las que ya no vienen en la lista
            // 2. Actualiza las existentes que vienen con ID
            // 3. Inserta las nuevas que no tienen ID
            if (especialidades != null && !especialidades.isEmpty()) {
                actualizarEspecialidadesInterno(conn, profesionalId, especialidades);
            } else {
                // Si no vienen especialidades, marcar todas como inactivas
                desactivarTodasEspecialidadesInterno(conn, profesionalId);
            }

            // Para área de servicio y disponibilidad, mantener lógica de reemplazo total
            // (ya que no tienen el concepto de múltiples registros como especialidades)
            eliminarAreaServicioPorProfesionalInterno(conn, profesionalId);
            eliminarDisponibilidadPorProfesionalInterno(conn, profesionalId);

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
                    logger.error("Rollback realizado para profesional {}", profesionalId);
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

		// ✅ SQL ACTUALIZADO: Añadido campos servicio_profesional, trabajo_remoto y trabajo_presencial (actualizado: 2025-11-14)
		String sql = "INSERT INTO especialidades_profesional " +
		"(profesional_id, categoria_id, servicio_profesional, descripcion, incluye_materiales, " +
		"costo, tipo_costo, es_principal, orden, trabajo_remoto, trabajo_presencial, " +
		"fecha_creacion, fecha_actualizacion, activo) " +
		"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW(), true)";

		try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			for (EspecialidadProfesional esp : especialidades) {
				esp.setProfesionalId(profesionalId);

				stmt.setInt(1, profesionalId);
				stmt.setInt(2, esp.getCategoriaId());
				stmt.setString(3, esp.getServicioProfesional());  // Campo servicio_profesional
				stmt.setString(4, esp.getDescripcion());
				stmt.setBoolean(5, esp.getIncluyeMateriales());
				stmt.setDouble(6, esp.getCosto());
				stmt.setString(7, esp.getTipoCosto());
				stmt.setBoolean(8, esp.getEsPrincipal());
				stmt.setInt(9, esp.getOrden());
				// ✅ NUEVOS CAMPOS - Tipo de prestación de trabajo (añadido: 2025-11-14)
				stmt.setBoolean(10, esp.getTrabajoRemoto() != null ? esp.getTrabajoRemoto() : false);
				stmt.setBoolean(11, esp.getTrabajoPresencial() != null ? esp.getTrabajoPresencial() : false);

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
    	// ✅ SQL ACTUALIZADO: Añadido campos servicio_profesional, trabajo_remoto y trabajo_presencial (actualizado: 2025-11-14)
    	String sql = "SELECT e.id, e.profesional_id, e.categoria_id, e.servicio_profesional, e.descripcion, " +
                "e.incluye_materiales, e.costo, e.tipo_costo, e.es_principal, e.orden, " +
                "e.trabajo_remoto, e.trabajo_presencial, " +  // ✅ NUEVOS CAMPOS (2025-11-14)
                "e.fecha_creacion, e.fecha_actualizacion, e.activo, " +
                "c.nombre AS categoria_nombre, c.descripcion AS categoria_descripcion, " +
                "c.icono AS categoria_icono, c.color AS categoria_color " +
                "FROM especialidades_profesional e " +
                "INNER JOIN categorias_servicio c ON e.categoria_id = c.id " +
                "WHERE e.profesional_id = ? AND e.activo = TRUE " +
                "ORDER BY e.orden ASC";

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

    // ✅ ACTUALIZADO: Soft delete - marca especialidades como inactivas en lugar de eliminarlas (actualizado: 2025-11-15)
    // ✅ ACTUALIZADO: También limpiar orden al desactivar para evitar conflictos (actualizado: 2025-11-15)
    private boolean eliminarEspecialidadesPorProfesionalInterno(Connection conn, Integer profesionalId) throws SQLException {
        String sql = "UPDATE especialidades_profesional SET activo = FALSE, orden = NULL, fecha_actualizacion = NOW() WHERE profesional_id = ? AND activo = TRUE";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, profesionalId);
            int rowsUpdated = stmt.executeUpdate();
            logger.debug("Desactivadas {} especialidades del profesional {}", rowsUpdated, profesionalId);
            return true;
        }
    }

    // ✅ NUEVO: Actualización inteligente de especialidades (añadido: 2025-11-15)
    // Permite actualizar dinámicamente especialidades sin perder IDs existentes
    private boolean actualizarEspecialidadesInterno(Connection conn, Integer profesionalId,
                                                     List<EspecialidadProfesional> especialidades) throws SQLException {

        // 1. Obtener IDs de especialidades que vienen en la solicitud
        java.util.Set<Integer> idsEnviados = new java.util.HashSet<>();
        for (EspecialidadProfesional esp : especialidades) {
            if (esp.getId() != null && esp.getId() > 0) {
                idsEnviados.add(esp.getId());
            }
        }

        // 2. Desactivar especialidades que ya NO vienen en la lista (soft delete)
        // ✅ ACTUALIZADO: También limpiar orden al desactivar para evitar conflictos (actualizado: 2025-11-15)
        String sqlDesactivar = "UPDATE especialidades_profesional " +
                              "SET activo = FALSE, orden = NULL, fecha_actualizacion = NOW() " +
                              "WHERE profesional_id = ? AND activo = TRUE";

        if (!idsEnviados.isEmpty()) {
            // Solo desactivar las que NO están en la lista de IDs enviados
            sqlDesactivar += " AND id NOT IN (" +
                           String.join(",", java.util.Collections.nCopies(idsEnviados.size(), "?")) +
                           ")";
        }

        try (PreparedStatement stmt = conn.prepareStatement(sqlDesactivar)) {
            stmt.setInt(1, profesionalId);
            int paramIndex = 2;
            for (Integer id : idsEnviados) {
                stmt.setInt(paramIndex++, id);
            }
            int desactivadas = stmt.executeUpdate();
            logger.debug("Desactivadas {} especialidades que ya no están en la lista", desactivadas);
        }

        // 3. Procesar cada especialidad: actualizar si existe o insertar si es nueva
        for (int i = 0; i < especialidades.size(); i++) {
            EspecialidadProfesional esp = especialidades.get(i);
            esp.setProfesionalId(profesionalId);
            esp.setOrden(i + 1); // Asignar orden basado en posición en la lista

            if (esp.getId() != null && esp.getId() > 0) {
                // Actualizar especialidad existente
                actualizarEspecialidadExistente(conn, esp);
            } else {
                // Insertar nueva especialidad
                insertarNuevaEspecialidad(conn, esp);
            }
        }

        logger.debug("Actualización de especialidades completada para profesional {}", profesionalId);
        return true;
    }

    // ✅ NUEVO: Actualizar una especialidad existente (añadido: 2025-11-15)
    private void actualizarEspecialidadExistente(Connection conn, EspecialidadProfesional esp) throws SQLException {
        String sql = "UPDATE especialidades_profesional SET " +
                    "categoria_id = ?, " +
                    "servicio_profesional = ?, " +
                    "descripcion = ?, " +
                    "incluye_materiales = ?, " +
                    "costo = ?, " +
                    "tipo_costo = ?, " +
                    "es_principal = ?, " +
                    "orden = ?, " +
                    "trabajo_remoto = ?, " +
                    "trabajo_presencial = ?, " +
                    "fecha_actualizacion = NOW(), " +
                    "activo = TRUE " + // Reactivar si estaba inactiva
                    "WHERE id = ? AND profesional_id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, esp.getCategoriaId());
            stmt.setString(2, esp.getServicioProfesional());
            stmt.setString(3, esp.getDescripcion());
            stmt.setBoolean(4, esp.getIncluyeMateriales() != null ? esp.getIncluyeMateriales() : false);
            stmt.setDouble(5, esp.getCosto());
            stmt.setString(6, esp.getTipoCosto());
            stmt.setBoolean(7, esp.getEsPrincipal() != null ? esp.getEsPrincipal() : false);
            stmt.setInt(8, esp.getOrden());
            stmt.setBoolean(9, esp.getTrabajoRemoto() != null ? esp.getTrabajoRemoto() : false);
            stmt.setBoolean(10, esp.getTrabajoPresencial() != null ? esp.getTrabajoPresencial() : false);
            stmt.setInt(11, esp.getId());
            stmt.setInt(12, esp.getProfesionalId());

            int updated = stmt.executeUpdate();
            logger.debug("Especialidad {} actualizada: {} filas afectadas", esp.getId(), updated);
        }
    }

    // ✅ NUEVO: Insertar una nueva especialidad (añadido: 2025-11-15)
    private void insertarNuevaEspecialidad(Connection conn, EspecialidadProfesional esp) throws SQLException {
        String sql = "INSERT INTO especialidades_profesional " +
                    "(profesional_id, categoria_id, servicio_profesional, descripcion, incluye_materiales, " +
                    "costo, tipo_costo, es_principal, orden, trabajo_remoto, trabajo_presencial, " +
                    "fecha_creacion, fecha_actualizacion, activo) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW(), true)";

        try (PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, esp.getProfesionalId());
            stmt.setInt(2, esp.getCategoriaId());
            stmt.setString(3, esp.getServicioProfesional());
            stmt.setString(4, esp.getDescripcion());
            stmt.setBoolean(5, esp.getIncluyeMateriales() != null ? esp.getIncluyeMateriales() : false);
            stmt.setDouble(6, esp.getCosto());
            stmt.setString(7, esp.getTipoCosto());
            stmt.setBoolean(8, esp.getEsPrincipal() != null ? esp.getEsPrincipal() : false);
            stmt.setInt(9, esp.getOrden());
            stmt.setBoolean(10, esp.getTrabajoRemoto() != null ? esp.getTrabajoRemoto() : false);
            stmt.setBoolean(11, esp.getTrabajoPresencial() != null ? esp.getTrabajoPresencial() : false);

            stmt.executeUpdate();

            // Obtener el ID generado
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                esp.setId(rs.getInt(1));
                logger.debug("Nueva especialidad insertada con ID: {}", esp.getId());
            }
        }
    }

    // ✅ NUEVO: Desactivar todas las especialidades de un profesional (añadido: 2025-11-15)
    // ✅ ACTUALIZADO: También limpiar orden al desactivar para evitar conflictos (actualizado: 2025-11-15)
    private boolean desactivarTodasEspecialidadesInterno(Connection conn, Integer profesionalId) throws SQLException {
        String sql = "UPDATE especialidades_profesional SET activo = FALSE, orden = NULL, fecha_actualizacion = NOW() " +
                    "WHERE profesional_id = ? AND activo = TRUE";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, profesionalId);
            int rowsUpdated = stmt.executeUpdate();
            logger.debug("Desactivadas todas ({}) las especialidades del profesional {}", rowsUpdated, profesionalId);
            return true;
        }
    }

    private EspecialidadProfesional mapearEspecialidad(ResultSet rs) throws SQLException {
        EspecialidadProfesional esp = new EspecialidadProfesional();

        // Campos de la tabla especialidades_profesional
        esp.setId(rs.getInt("id"));
        esp.setProfesionalId(rs.getInt("profesional_id"));
        esp.setCategoriaId(rs.getInt("categoria_id"));
        esp.setServicioProfesional(rs.getString("servicio_profesional"));
        esp.setDescripcion(rs.getString("descripcion"));
        esp.setIncluyeMateriales(rs.getBoolean("incluye_materiales"));
        esp.setCosto(rs.getDouble("costo"));
        esp.setTipoCosto(rs.getString("tipo_costo"));
        esp.setEsPrincipal(rs.getBoolean("es_principal"));
        esp.setOrden(rs.getInt("orden"));

        // ✅ NUEVOS CAMPOS - Tipo de prestación de trabajo (añadido: 2025-11-14)
        try {
            esp.setTrabajoRemoto(rs.getBoolean("trabajo_remoto"));
            esp.setTrabajoPresencial(rs.getBoolean("trabajo_presencial"));
        } catch (SQLException e) {
            // Si los campos no existen en el ResultSet, usar valores por defecto
            logger.debug("Campos de tipo de prestación no disponibles, usando valores por defecto");
            esp.setTrabajoRemoto(false);
            esp.setTrabajoPresencial(false);
        }

        esp.setFechaCreacion(rs.getTimestamp("fecha_creacion").toLocalDateTime());
        esp.setFechaActualizacion(rs.getTimestamp("fecha_actualizacion").toLocalDateTime());
        esp.setActivo(rs.getBoolean("activo"));

        // Campos transientes de categoría (del JOIN)
        try {
            esp.setCategoriaNombre(rs.getString("categoria_nombre"));
            esp.setCategoriaDescripcion(rs.getString("categoria_descripcion"));
            esp.setCategoriaIcono(rs.getString("categoria_icono"));
            esp.setCategoriaColor(rs.getString("categoria_color"));
        } catch (SQLException e) {
            // Estos campos podrían no estar si no se hizo JOIN
            logger.debug("Campos de categoría no disponibles en ResultSet");
        }

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

    // ✅ ACTUALIZACIÓN: DELETE físico para evitar conflictos con restricciones de unicidad
    private boolean eliminarAreaServicioPorProfesionalInterno(Connection conn, Integer profesionalId) throws SQLException {
        // Primero, obtener el ID del área de servicio
        String sqlGetId = "SELECT id FROM areas_servicio WHERE profesional_id = ?";
        Integer areaServicioId = null;

        try (PreparedStatement stmt = conn.prepareStatement(sqlGetId)) {
            stmt.setInt(1, profesionalId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                areaServicioId = rs.getInt("id");
            }
        }

        if (areaServicioId != null) {
            // Eliminar ubicaciones físicamente
            String sqlUbicaciones = "DELETE FROM ubicaciones_servicio WHERE area_servicio_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sqlUbicaciones)) {
                stmt.setInt(1, areaServicioId);
                int rowsDeleted = stmt.executeUpdate();
                logger.debug("Eliminadas {} ubicaciones del área de servicio {}", rowsDeleted, areaServicioId);
            }

            // Eliminar área de servicio físicamente
            String sqlArea = "DELETE FROM areas_servicio WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sqlArea)) {
                stmt.setInt(1, areaServicioId);
                stmt.executeUpdate();
                logger.debug("Eliminada área de servicio {} del profesional {}", areaServicioId, profesionalId);
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
                    stmt.setString(2, horario.getDiaSemana().toLowerCase());
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

    // ✅ ACTUALIZACIÓN: DELETE físico para evitar conflictos con restricciones de unicidad
    private boolean eliminarDisponibilidadPorProfesionalInterno(Connection conn, Integer profesionalId) throws SQLException {
        // Obtener ID de disponibilidad
        String sqlGetId = "SELECT id FROM disponibilidad_horaria WHERE profesional_id = ?";
        Integer disponibilidadId = null;

        try (PreparedStatement stmt = conn.prepareStatement(sqlGetId)) {
            stmt.setInt(1, profesionalId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                disponibilidadId = rs.getInt("id");
            }
        }

        if (disponibilidadId != null) {
            // Eliminar horarios físicamente
            String sqlHorarios = "DELETE FROM horarios_dia WHERE disponibilidad_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sqlHorarios)) {
                stmt.setInt(1, disponibilidadId);
                int rowsDeleted = stmt.executeUpdate();
                logger.debug("Eliminados {} horarios de la disponibilidad {}", rowsDeleted, disponibilidadId);
            }

            // Eliminar disponibilidad físicamente
            String sqlDisp = "DELETE FROM disponibilidad_horaria WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sqlDisp)) {
                stmt.setInt(1, disponibilidadId);
                stmt.executeUpdate();
                logger.debug("Eliminada disponibilidad {} del profesional {}", disponibilidadId, profesionalId);
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

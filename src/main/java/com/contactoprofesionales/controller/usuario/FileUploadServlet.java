package com.contactoprofesionales.controller.usuario;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@WebServlet(name = "FileUploadServlet", urlPatterns = {"/api/upload-image"})
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024,    // 1 MB
    maxFileSize = 1024 * 1024 * 5,      // 5 MB
    maxRequestSize = 1024 * 1024 * 10   // 10 MB
)
public class FileUploadServlet extends HttpServlet {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LoggerFactory.getLogger(FileUploadServlet.class);
    private static final String UPLOAD_DIRECTORY = "uploads/profiles";
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        try {
            // Obtener el archivo
            Part filePart = request.getPart("file");
            
            if (filePart == null) {
                sendError(response, "No se recibió ningún archivo");
                return;
            }
            
            String fileName = getFileName(filePart);
            String contentType = filePart.getContentType();
            
            // Validar que sea una imagen
            if (!contentType.startsWith("image/")) {
                sendError(response, "El archivo debe ser una imagen");
                return;
            }
            
            // Validar tamaño (máx 5MB)
            if (filePart.getSize() > 5 * 1024 * 1024) {
                sendError(response, "La imagen no debe superar los 5MB");
                return;
            }
            
            // Crear directorio si no existe
            String applicationPath = request.getServletContext().getRealPath("");
            String uploadPath = applicationPath + File.separator + UPLOAD_DIRECTORY;
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }
            
            // Generar nombre único
            String fileExtension = getFileExtension(fileName);
            String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
            
            // Guardar archivo
            Path filePath = Paths.get(uploadPath, uniqueFileName);
            try (InputStream fileContent = filePart.getInputStream()) {
                Files.copy(fileContent, filePath, StandardCopyOption.REPLACE_EXISTING);
            }
            
            // Construir URL de acceso
            String contextPath = request.getContextPath();
            String fileUrl = contextPath + "/" + UPLOAD_DIRECTORY + "/" + uniqueFileName;
            
            logger.info("Imagen guardada exitosamente: {}", fileUrl);
            
            // Responder con la URL
            sendSuccess(response, fileUrl);
            
        } catch (Exception e) {
            logger.error("Error al subir imagen", e);
            sendError(response, "Error al subir la imagen: " + e.getMessage());
        }
    }
    
    private String getFileName(Part part) {
        String contentDisposition = part.getHeader("content-disposition");
        for (String token : contentDisposition.split(";")) {
            if (token.trim().startsWith("filename")) {
                return token.substring(token.indexOf('=') + 1).trim().replace("\"", "");
            }
        }
        return "unknown";
    }
    
    private String getFileExtension(String fileName) {
        int lastIndexOf = fileName.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return ".jpg"; // extensión por defecto
        }
        return fileName.substring(lastIndexOf);
    }
    
    private void sendSuccess(HttpServletResponse response, String fileUrl) throws IOException {
        String json = String.format("{\"success\": true, \"message\": \"Imagen subida exitosamente\", \"data\": {\"url\": \"%s\"}}", fileUrl);
        response.getWriter().write(json);
    }
    
    private void sendError(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        String json = String.format("{\"success\": false, \"message\": \"%s\", \"data\": null}", message);
        response.getWriter().write(json);
    }
}

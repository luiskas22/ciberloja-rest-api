package com.luis.ciberloja;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import com.luis.ciberloja.conf.ConfigurationParametersManager;
import com.luis.ciberloja.service.FileService;
import com.luis.ciberloja.service.impl.FileServiceImpl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

@Path("/file")
public class FileResource {

    private FileService fileService = null;
    private static Logger logger = LogManager.getLogger(FileResource.class);

    public FileResource() {
        fileService = new FileServiceImpl();
    }

    @POST
    @Path("/upload/producto/{productoId}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Subir una imagen para un producto", description = "Sube una imagen asociada a un producto específico usando su ID.")
    public Response uploadImage(
            @Parameter(description = "ID del producto al que se subirá la imagen", required = true) @PathParam("productoId") String productoId,
            @Parameter(description = "Archivo de imagen a subir", required = true) @FormDataParam("file") InputStream fileInputStream,
            @FormDataParam("file") FormDataContentDisposition fileDetail) {

        try {
            if (productoId == null) {
                return Response.status(Status.BAD_REQUEST).entity("ID de producto inválido").build();
            }
            if (fileInputStream == null) {
                return Response.status(Status.BAD_REQUEST).entity("No se proporcionó archivo").build();
            }

            String fileName = fileDetail != null ? fileDetail.getFileName() : "uploaded_file";
            fileService.uploadImage(productoId, fileInputStream, fileName);

            List<String> imageUrls = getImageUrls(productoId);
            return Response.ok(imageUrls).build();
        } catch (Exception e) {
            logger.error("Error al subir archivo para el producto {}: {}", productoId, e.getMessage(), e);
            return Response.serverError().entity("Error al subir archivo: " + e.getMessage()).build();
        }
    }

    @GET
    @Path("/producto/{productoId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Obtener imágenes por ID de producto", description = "Recupera la lista de URLs de imágenes asociadas a un producto por su ID.")
    public Response getImagesByProductoId(
            @Parameter(description = "ID del producto para buscar sus imágenes", required = true) @PathParam("productoId") String productoId) {
        try {
            if (productoId == null) {
                logger.warn("ID de producto inválido: {}", productoId);
                return Response.status(Status.BAD_REQUEST).entity("El ID del producto debe ser un número positivo").build();
            }

            logger.info("Buscando imágenes para el producto ID: {}", productoId);
            List<String> imageUrls = getImageUrls(productoId);
            return Response.ok(imageUrls).build();
        } catch (Exception e) {
            logger.error("Error al obtener imágenes para el producto {}: {}", productoId, e.getMessage(), e);
            return Response.status(Status.INTERNAL_SERVER_ERROR)
                    .entity("Error al obtener las imágenes: " + e.getMessage()).build();
        }
    }

    @GET
    @Path("/images/{productoId}/{fileName}")
    @Produces("image/*")
    @Operation(summary = "Servir imagen de producto", description = "Devuelve el archivo de imagen para un producto específico.")
    public Response getImageFile(
            @Parameter(description = "ID del producto", required = true) @PathParam("productoId") String productoId,
            @Parameter(description = "Nombre del archivo de imagen", required = true) @PathParam("fileName") String fileName) {
        try {
            String basePath = ConfigurationParametersManager.getParameterValue("base.image.path");
            File imageFile = new File(basePath + File.separator + productoId + File.separator + fileName);
            if (!imageFile.exists()) {
                logger.warn("Imagen no encontrada: {}", imageFile.getAbsolutePath());
                return Response.status(Status.NOT_FOUND).entity("Imagen no encontrada").build();
            }
            String mediaType = fileName.endsWith(".png") ? "image/png" : "image/jpeg";
            return Response.ok(new FileInputStream(imageFile)).type(mediaType).build();
        } catch (Exception e) {
            logger.error("Error al servir imagen para producto {}: {}", productoId, e.getMessage(), e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Error al servir la imagen: " + e.getMessage()).build();
        }
    }

    private List<String> getImageUrls(String productoId) {
        List<File> images = fileService.getImagesByProductoId(productoId);
        return images.stream()
                .map(file -> "/ciberloja-rest-api/api/file/images/" + productoId + "/" + file.getName())
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());
    }
}
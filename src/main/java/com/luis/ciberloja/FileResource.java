package com.luis.ciberloja;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import com.luis.ciberloja.service.FileService;
import com.luis.ciberloja.service.impl.FileServiceImpl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
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
	@Operation(summary = "Subir una imagen para un producto", description = "Sube una imagen asociada a un producto específico usando su ID.", responses = {
			@ApiResponse(responseCode = "200", description = "Imagen subida correctamente", content = @Content(schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "400", description = "Datos inválidos o archivo demasiado grande"),
			@ApiResponse(responseCode = "500", description = "Error interno al subir la imagen") })
	public Response uploadImage(
			@Parameter(description = "ID del producto al que se subirá la imagen", required = true) @PathParam("productoId") Long productoId,
			@Parameter(description = "Archivo de imagen a subir", required = true) @FormDataParam("file") InputStream fileInputStream,
			@FormDataParam("file") FormDataContentDisposition fileDetail) {

		try {
			if (productoId == null || productoId <= 0) {
				return Response.status(Status.BAD_REQUEST).entity("ID de producto inválido").build();
			}
			if (fileInputStream == null) {
				return Response.status(Status.BAD_REQUEST).entity("No se proporcionó archivo").build();
			}

			String fileName = fileDetail != null ? fileDetail.getFileName() : "uploaded_file";
			fileService.uploadImage(productoId, fileInputStream, fileName);

			return Response.ok(fileService.getImagesByProductoId(productoId)).build();
		} catch (Exception e) {
			return Response.serverError().entity("Error al subir archivo: " + e.getMessage()).build();
		}
	}

	@GET
	@Path("/producto/{productoId}")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Obtener imágenes por ID de producto", operationId = "getImagesByProductoId", description = "Recupera la lista de imágenes asociadas a un producto por su ID.", responses = {
			@ApiResponse(responseCode = "200", description = "Imágenes encontradas", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = File[].class))),
			@ApiResponse(responseCode = "204", description = "No se encontraron imágenes para el producto"),
			@ApiResponse(responseCode = "400", description = "ID de producto inválido"),
			@ApiResponse(responseCode = "500", description = "Error interno al recuperar las imágenes") })
	public Response getImagesByProductoId(
			@Parameter(description = "ID del producto para buscar sus imágenes", required = true) @PathParam("productoId") Long productoId) {
		try {
			if (productoId == null || productoId <= 0) {
				logger.warn("ID de producto inválido: {}", productoId);
				return Response.status(Status.BAD_REQUEST).entity("El ID del producto debe ser un número positivo")
						.build();
			}

			logger.info("Buscando imágenes para el producto ID: {}", productoId);
			List<File> images = fileService.getImagesByProductoId(productoId);

			if (images.isEmpty()) {
				logger.info("No se encontraron imágenes para el producto ID: {}", productoId);
				return Response.status(Status.NO_CONTENT)
						.entity("No se encontraron imágenes para el producto ID: " + productoId).build();
			}

			File imageFile = images.get(0);
			InputStream fileStream = new FileInputStream(imageFile);
			String mediaType = getMediaType(imageFile.getName());

			return Response.ok(fileStream).type(mediaType).build();

		} catch (IOException e) {
			return Response.status(Response.Status.NOT_FOUND).entity("Error al leer la imagen: " + e.getMessage())
					.build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity("Error al obtener la imagen: " + e.getMessage()).build();
		}
	}

	private String getMediaType(String fileName) {
		if (fileName.endsWith(".png")) {
			return "image/png";
		} else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
			return "image/jpeg";
		}
		return MediaType.APPLICATION_OCTET_STREAM;
	}
}
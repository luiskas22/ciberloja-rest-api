package com.luis.ciberloja;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.luis.ciberloja.model.Concelho;
import com.luis.ciberloja.model.Distrito;
import com.luis.ciberloja.service.DistritoService;
import com.luis.ciberloja.service.impl.DistritoServiceImpl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

@Path("/distrito")

public class DistritoResource {

	private DistritoService distritoService = null;
	private static final Logger logger = LogManager.getLogger(DistritoResource.class);

	public DistritoResource() {
		distritoService = new DistritoServiceImpl();
	}

	@GET
	@Path("/findAll")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Obtener todas los distritos", operationId = "findAllDistritos", description = "Recupera una lista de todas los distritos disponibles en la base de datos", responses = {
			@ApiResponse(responseCode = "200", description = "Lista de distritos recuperada exitosamente", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = Distrito.class, type = "array"))),
			@ApiResponse(responseCode = "500", description = "Error interno al recuperar los distritos") })
	public Response findAll() {
		try {
			List<Distrito> distritos = distritoService.findAll();
			logger.info("Retrieved {} distritos", distritos.size());
			return Response.status(Response.Status.OK).entity(distritos).build();
		} catch (DataException e) {
			logger.error("Error retrieving distritos: {}", e.getMessage(), e);
			return Response.status(Status.INTERNAL_SERVER_ERROR)
					.entity("Error al recuperar los distritos: " + e.getMessage()).build();
		}
	}

	@GET
	@Path("/find/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Búsqueda por ID de distritos", operationId = "findDistritoById", description = "Recupera todos los datos de un distrito específica por su ID", responses = {
			@ApiResponse(responseCode = "200", description = "distrito encontrado", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = Distrito.class))),
			@ApiResponse(responseCode = "400", description = "ID no proporcionado o inválido"),
			@ApiResponse(responseCode = "404", description = "distrito no encontrado"),
			@ApiResponse(responseCode = "500", description = "Error interno al recuperar el distrito") })
	public Response findById(@PathParam("id") int id) {
		try {
			if (id <= 0) {
				logger.warn("Invalid ID provided: {}", id);
				return Response.status(Status.BAD_REQUEST).entity("ID inválido: debe ser un número positivo").build();
			}

			Distrito distrito = distritoService.findById(id);
			if (distrito == null) {
				logger.warn("Distrito with ID {} not found", id);
				return Response.status(Status.NOT_FOUND).entity("Distrito con ID " + id + " no encontrado.").build();
			}
			logger.info("Retrieved Distrito with ID {}", id);
			return Response.status(Status.OK).entity(distrito).build();
		} catch (DataException e) {
			logger.error("Error retrieving distrito with ID {}: {}", id, e.getMessage(), e);
			return Response.status(Status.INTERNAL_SERVER_ERROR)
					.entity("Ha ocurrido un error interno al buscar el Distrito: " + e.getMessage()).build();
		}
	}
}

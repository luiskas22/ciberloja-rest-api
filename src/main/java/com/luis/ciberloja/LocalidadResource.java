package com.luis.ciberloja;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.luis.ciberloja.model.Localidad;
import com.luis.ciberloja.service.LocalidadService;
import com.luis.ciberloja.service.impl.LocalidadServiceImpl;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@Path("/localidad")
public class LocalidadResource {
	private LocalidadService localidadService = null;
	private static final Logger logger = LogManager.getLogger(LocalidadResource.class);

	public LocalidadResource() {
		localidadService = new LocalidadServiceImpl();
	}

	@GET
	@Path("/findAll")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Obtener todas las localidades", operationId = "findAllLocalidades", description = "Recupera una lista de todas las localidades disponibles en la base de datos", responses = {
			@ApiResponse(responseCode = "200", description = "Lista de localidades recuperada exitosamente", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = Localidad.class, type = "array"))),
			@ApiResponse(responseCode = "500", description = "Error interno al recuperar las localidades") })
	public Response findAll() {
		try {
			List<Localidad> localidades = localidadService.findAll();
			logger.info("Retrieved {} localidades", localidades.size());
			return Response.status(Response.Status.OK).entity(localidades).build();
		} catch (DataException e) {
			logger.error("Error retrieving localidades: {}", e.getMessage(), e);
			return Response.status(Status.INTERNAL_SERVER_ERROR)
					.entity("Error al recuperar las localidades: " + e.getMessage()).build();
		}
	}

	@GET
	@Path("/find/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Búsqueda por ID de localidad", operationId = "findLocalidadById", description = "Recupera todos los datos de una localidad específica por su ID", responses = {
			@ApiResponse(responseCode = "200", description = "Localidad encontrada", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = Localidad.class))),
			@ApiResponse(responseCode = "400", description = "ID no proporcionado o inválido"),
			@ApiResponse(responseCode = "404", description = "Localidad no encontrada"),
			@ApiResponse(responseCode = "500", description = "Error interno al recuperar la localidad") })
	public Response findById(@PathParam("id") int id) {
		try {
			if (id <= 0) {
				logger.warn("Invalid ID provided: {}", id);
				return Response.status(Status.BAD_REQUEST).entity("ID inválido: debe ser un número positivo").build();
			}

			Localidad localidad = localidadService.findById(id);
			if (localidad == null) {
				logger.warn("Localidad with ID {} not found", id);
				return Response.status(Status.NOT_FOUND).entity("Localidad con ID " + id + " no encontrada.").build();
			}
			logger.info("Retrieved localidad with ID {}", id);
			return Response.status(Status.OK).entity(localidad).build();
		} catch (DataException e) {
			logger.error("Error retrieving localidad with ID {}: {}", id, e.getMessage(), e);
			return Response.status(Status.INTERNAL_SERVER_ERROR)
					.entity("Ha ocurrido un error interno al buscar la localidad: " + e.getMessage()).build();
		}
	}
}
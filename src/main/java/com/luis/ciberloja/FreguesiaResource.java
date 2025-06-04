package com.luis.ciberloja;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.luis.ciberloja.model.Freguesia;
import com.luis.ciberloja.service.FreguesiaService;
import com.luis.ciberloja.service.impl.FreguesiaServiceImpl;

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

@Path("/freguesia")
public class FreguesiaResource {
	private FreguesiaService freguesiaService = null;
	private static final Logger logger = LogManager.getLogger(FreguesiaResource.class);

	public FreguesiaResource() {
		freguesiaService = new FreguesiaServiceImpl();
	}

	@GET
	@Path("/findAll")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Obtener todas las freguesias", operationId = "findAllFreguesias", description = "Recupera una lista de todas las freguesias disponibles en la base de datos", responses = {
			@ApiResponse(responseCode = "200", description = "Lista de freguesias recuperada exitosamente", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = Freguesia.class, type = "array"))),
			@ApiResponse(responseCode = "500", description = "Error interno al recuperar las freguesias") })
	public Response findAll() {
		try {
			List<Freguesia> freguesias = freguesiaService.findAll();
			logger.info("Retrieved {} freguesias", freguesias.size());
			return Response.status(Response.Status.OK).entity(freguesias).build();
		} catch (DataException e) {
			logger.error("Error retrieving freguesias: {}", e.getMessage(), e);
			return Response.status(Status.INTERNAL_SERVER_ERROR)
					.entity("Error al recuperar las freguesias: " + e.getMessage()).build();
		}
	}

	@GET
	@Path("/find/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Búsqueda por ID de freguesia", operationId = "findFreguesiaById", description = "Recupera todos los datos de una freguesia específica por su ID", responses = {
			@ApiResponse(responseCode = "200", description = "freguesia encontrada", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = Freguesia.class))),
			@ApiResponse(responseCode = "400", description = "ID no proporcionado o inválido"),
			@ApiResponse(responseCode = "404", description = "freguesia no encontrada"),
			@ApiResponse(responseCode = "500", description = "Error interno al recuperar la freguesia") })
	public Response findById(@PathParam("id") int id) {
		try {
			if (id <= 0) {
				logger.warn("Invalid ID provided: {}", id);
				return Response.status(Status.BAD_REQUEST).entity("ID inválido: debe ser un número positivo").build();
			}

			Freguesia freguesia = freguesiaService.findById(id);
			if (freguesia == null) {
				logger.warn("freguesia with ID {} not found", id);
				return Response.status(Status.NOT_FOUND).entity("freguesia con ID " + id + " no encontrada.").build();
			}
			logger.info("Retrieved freguesia with ID {}", id);
			return Response.status(Status.OK).entity(freguesia).build();
		} catch (DataException e) {
			logger.error("Error retrieving freguesia with ID {}: {}", id, e.getMessage(), e);
			return Response.status(Status.INTERNAL_SERVER_ERROR)
					.entity("Ha ocurrido un error interno al buscar la freguesia: " + e.getMessage()).build();
		}
	}
}
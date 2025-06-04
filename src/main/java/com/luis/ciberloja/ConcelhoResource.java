package com.luis.ciberloja;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.luis.ciberloja.model.Concelho;
import com.luis.ciberloja.model.Distrito;
import com.luis.ciberloja.service.ConcelhoService;
import com.luis.ciberloja.service.impl.ConcelhoServiceImpl;

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

@Path("/concelho")
public class ConcelhoResource {
	private ConcelhoService concelhoService = null;
	private static final Logger logger = LogManager.getLogger(ConcelhoResource.class);

	public ConcelhoResource() {
		concelhoService = new ConcelhoServiceImpl();
	}

	@GET
	@Path("/findAll")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Obtener todas las concelhos", operationId = "findAllConcelhos", description = "Recupera una lista de todas las concelhos disponibles en la base de datos", responses = {
			@ApiResponse(responseCode = "200", description = "Lista de concelhos recuperada exitosamente", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = Concelho.class, type = "array"))),
			@ApiResponse(responseCode = "500", description = "Error interno al recuperar las concelhos") })
	public Response findAll() {
		try {
			List<Concelho> concelhos = concelhoService.findAll();
			logger.info("Retrieved {} concelhos", concelhos.size());
			return Response.status(Response.Status.OK).entity(concelhos).build();
		} catch (DataException e) {
			logger.error("Error retrieving concelhos: {}", e.getMessage(), e);
			return Response.status(Status.INTERNAL_SERVER_ERROR)
					.entity("Error al recuperar las concelhos: " + e.getMessage()).build();
		}
	}

	@GET
	@Path("/find/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Búsqueda por ID de concelhos", operationId = "findConcelhoById", description = "Recupera todos los datos de una concelhos específica por su ID", responses = {
			@ApiResponse(responseCode = "200", description = "concelhos encontrada", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = Concelho.class))),
			@ApiResponse(responseCode = "400", description = "ID no proporcionado o inválido"),
			@ApiResponse(responseCode = "404", description = "concelhos no encontrada"),
			@ApiResponse(responseCode = "500", description = "Error interno al recuperar el concelho") })
	public Response findById(@PathParam("id") int id) {
		try {
			if (id <= 0) {
				logger.warn("Invalid ID provided: {}", id);
				return Response.status(Status.BAD_REQUEST).entity("ID inválido: debe ser un número positivo").build();
			}

			Concelho provincia = concelhoService.findById(id);
			if (provincia == null) {
				logger.warn("concelho with ID {} not found", id);
				return Response.status(Status.NOT_FOUND).entity("concelho con ID " + id + " no encontraoa.").build();
			}
			logger.info("Retrieved concelho with ID {}", id);
			return Response.status(Status.OK).entity(provincia).build();
		} catch (DataException e) {
			logger.error("Error retrieving concelho with ID {}: {}", id, e.getMessage(), e);
			return Response.status(Status.INTERNAL_SERVER_ERROR)
					.entity("Ha ocurrido un error interno al buscar el concelho: " + e.getMessage()).build();
		}
	}
}
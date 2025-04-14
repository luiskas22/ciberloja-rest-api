package com.luis.ciberloja;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.luis.ciberloja.model.Pais;
import com.luis.ciberloja.service.PaisService;
import com.luis.ciberloja.service.impl.PaisServiceImpl;

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

@Path("/pais")
public class PaisResource {
	private PaisService paisService = null;
	private static final Logger logger = LogManager.getLogger(PaisResource.class);

	public PaisResource() {
		paisService = new PaisServiceImpl();
	}

	@GET
	@Path("/findAll")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Obtener todas las paises", operationId = "findAllPaises", description = "Recupera una lista de todas las paises disponibles en la base de datos", responses = {
			@ApiResponse(responseCode = "200", description = "Lista de paises recuperada exitosamente", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = Pais.class, type = "array"))),
			@ApiResponse(responseCode = "500", description = "Error interno al recuperar las paises") })
	public Response findAll() {
		try {
			List<Pais> pais = paisService.findAll();
			logger.info("Retrieved {} paises", pais.size());
			return Response.status(Response.Status.OK).entity(pais).build();
		} catch (DataException e) {
			logger.error("Error retrieving paises: {}", e.getMessage(), e);
			return Response.status(Status.INTERNAL_SERVER_ERROR)
					.entity("Error al recuperar las paises: " + e.getMessage()).build();
		}
	}

	@GET
	@Path("/find/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Búsqueda por ID de paises", operationId = "findPaisesById", description = "Recupera todos los datos de un pais específica por su ID", responses = {
			@ApiResponse(responseCode = "200", description = "Pais encontrado", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = Pais.class))),
			@ApiResponse(responseCode = "400", description = "ID no proporcionado o inválido"),
			@ApiResponse(responseCode = "404", description = "Pais no encontrada"),
			@ApiResponse(responseCode = "500", description = "Error interno al recuperar el pais") })
	public Response findById(@PathParam("id") int id) {
		try {
			if (id <= 0) {
				logger.warn("Invalid ID provided: {}", id);
				return Response.status(Status.BAD_REQUEST).entity("ID inválido: debe ser un número positivo").build();
			}

			Pais pais = paisService.findById(id);
			if (pais == null) {
				logger.warn("pais with ID {} not found", id);
				return Response.status(Status.NOT_FOUND).entity("pais con ID " + id + " no encontrada.").build();
			}
			logger.info("Retrieved pais with ID {}", id);
			return Response.status(Status.OK).entity(pais).build();
		} catch (DataException e) {
			logger.error("Error retrieving pais with ID {}: {}", id, e.getMessage(), e);
			return Response.status(Status.INTERNAL_SERVER_ERROR)
					.entity("Ha ocurrido un error interno al buscar la pais: " + e.getMessage()).build();
		}
	}
}
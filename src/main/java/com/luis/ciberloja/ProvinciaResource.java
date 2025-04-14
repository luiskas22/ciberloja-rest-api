package com.luis.ciberloja;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import com.luis.ciberloja.model.Provincia;
import com.luis.ciberloja.service.ProvinciaService;
import com.luis.ciberloja.service.impl.ProvinciaServiceImpl;

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

@Path("/provincia")
public class ProvinciaResource {
	private ProvinciaService provinciaService = null;
	private static final Logger logger = LogManager.getLogger(ProvinciaResource.class);

	public ProvinciaResource() {
		provinciaService = new ProvinciaServiceImpl();
	}

	@GET
	@Path("/findAll")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Obtener todas las provincias", operationId = "findAllProvincias", description = "Recupera una lista de todas las provincias disponibles en la base de datos", responses = {
			@ApiResponse(responseCode = "200", description = "Lista de provincias recuperada exitosamente", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = Provincia.class, type = "array"))),
			@ApiResponse(responseCode = "500", description = "Error interno al recuperar las provincias") })
	public Response findAll() {
		try {
			List<Provincia> provincias = provinciaService.findAll();
			logger.info("Retrieved {} provincias", provincias.size());
			return Response.status(Response.Status.OK).entity(provincias).build();
		} catch (DataException e) {
			logger.error("Error retrieving provincias: {}", e.getMessage(), e);
			return Response.status(Status.INTERNAL_SERVER_ERROR)
					.entity("Error al recuperar las provincias: " + e.getMessage()).build();
		}
	}

	@GET
	@Path("/find/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Búsqueda por ID de provincias", operationId = "findProvinciaById", description = "Recupera todos los datos de una provincias específica por su ID", responses = {
			@ApiResponse(responseCode = "200", description = "provincias encontrada", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = Provincia.class))),
			@ApiResponse(responseCode = "400", description = "ID no proporcionado o inválido"),
			@ApiResponse(responseCode = "404", description = "provincias no encontrada"),
			@ApiResponse(responseCode = "500", description = "Error interno al recuperar la provincia") })
	public Response findById(@PathParam("id") int id) {
		try {
			if (id <= 0) {
				logger.warn("Invalid ID provided: {}", id);
				return Response.status(Status.BAD_REQUEST).entity("ID inválido: debe ser un número positivo").build();
			}

			Provincia provincia = provinciaService.findById(id);
			if (provincia == null) {
				logger.warn("provincia with ID {} not found", id);
				return Response.status(Status.NOT_FOUND).entity("provincia con ID " + id + " no encontrada.").build();
			}
			logger.info("Retrieved provincia with ID {}", id);
			return Response.status(Status.OK).entity(provincia).build();
		} catch (DataException e) {
			logger.error("Error retrieving provincia with ID {}: {}", id, e.getMessage(), e);
			return Response.status(Status.INTERNAL_SERVER_ERROR)
					.entity("Ha ocurrido un error interno al buscar la provincia: " + e.getMessage()).build();
		}
	}
}
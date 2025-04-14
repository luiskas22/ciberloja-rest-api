package com.luis.ciberloja;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.luis.ciberloja.model.DireccionDTO;
import com.luis.ciberloja.service.DireccionService;
import com.luis.ciberloja.service.impl.DireccionServiceImpl;
import com.luis.ciberloja.DataException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

@Path("/direccion")
public class DireccionResource {
	private DireccionService direccionService = null;
	private static Logger logger = LogManager.getLogger(DireccionResource.class);

	public DireccionResource() {
		direccionService = new DireccionServiceImpl();
	}

	@GET
	@Path("/find/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Búsqueda por ID de dirección", operationId = "findDireccionById", description = "Recupera todos los datos de una dirección por su ID", responses = {
			@ApiResponse(responseCode = "200", description = "Dirección encontrada", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = DireccionDTO.class))),
			@ApiResponse(responseCode = "404", description = "Dirección no encontrada"),
			@ApiResponse(responseCode = "400", description = "Error al recuperar los datos") })
	public Response getById(@PathParam("id") Long id) {
		try {
			if (id == null) {
				return Response.status(Status.BAD_REQUEST).entity("ID no proporcionado").build();
			}
			DireccionDTO direccion = direccionService.findById(id);
			if (direccion == null) {
				return Response.status(Status.NOT_FOUND).entity("Dirección con ID " + id + " no encontrada.").build();
			}
			return Response.status(Status.OK).entity(direccion).build();
		} catch (DataException e) {
			logger.error("Error al buscar la dirección con ID " + id, e);
			return Response.status(Status.INTERNAL_SERVER_ERROR)
					.entity("Ha ocurrido un error interno al buscar la dirección.").build();
		}
	}

	@POST
	@Path("/create")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(operationId = "createDireccion", summary = "Creación de una dirección", description = "Crea una nueva dirección con los datos proporcionados", responses = {
			@ApiResponse(responseCode = "200", description = "Dirección creada correctamente", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = DireccionDTO.class))),
			@ApiResponse(responseCode = "400", description = "Datos introducidos inválidos"),
			@ApiResponse(responseCode = "500", description = "Error en el proceso de creación") })
	public Response create(DireccionDTO direccion) {
		if (direccion == null || direccion.getNombreVia() == null || direccion.getDirVia() == null
				|| direccion.getLocalidadId() == null) {
			return Response.status(Status.BAD_REQUEST).entity("Datos introducidos inválidos").build();
		}

		try {
			Long id = direccionService.create(direccion);
			DireccionDTO createdDireccion = direccionService.findById(id);
			if (createdDireccion == null) {
				return Response.status(Status.INTERNAL_SERVER_ERROR)
						.entity("Dirección creada pero no encontrada tras la creación.").build();
			}
			return Response.status(Status.OK).entity(createdDireccion).build();
		} catch (DataException e) {
			logger.error("Error en el proceso de creación de la dirección", e);
			return Response.status(Status.INTERNAL_SERVER_ERROR)
					.entity("Error interno en el proceso de creación de la dirección.").build();
		}
	}

	@PUT
	@Path("/update")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(operationId = "updateDireccion", summary = "Actualización de una dirección", description = "Actualiza los datos de una dirección existente", responses = {
			@ApiResponse(responseCode = "200", description = "Dirección actualizada correctamente", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = DireccionDTO.class))),
			@ApiResponse(responseCode = "400", description = "Datos introducidos incorrectos o incompletos"),
			@ApiResponse(responseCode = "500", description = "Error en el proceso de actualización") })
	public Response update(DireccionDTO direccion) {
		// Validación inicial de los datos de entrada
		if (direccion == null || direccion.getId() == null || direccion.getNombreVia() == null
				|| direccion.getDirVia() == null || direccion.getLocalidadId() == null) {
			return Response.status(Status.BAD_REQUEST).entity("Datos introducidos inválidos").build();
		}

		try {
			// Intenta actualizar la dirección usando el servicio
			boolean isUpdated = direccionService.update(direccion);
			if (isUpdated) {
				// Si la actualización fue exitosa, busca la dirección actualizada
				DireccionDTO updatedDireccion = direccionService.findById(direccion.getId());
				if (updatedDireccion == null) {
					return Response.status(Status.INTERNAL_SERVER_ERROR)
							.entity("Dirección actualizada pero no encontrada tras la actualización.").build();
				}
				return Response.status(Status.OK).entity(updatedDireccion).build();
			} else {
				// Si no se actualizó (probablemente porque la dirección no existe)
				return Response.status(Status.NOT_FOUND)
						.entity("Dirección con ID " + direccion.getId() + " no encontrada").build();
			}
		} catch (DataException e) {
			// Maneja excepciones lanzadas por el servicio (por ejemplo, errores de base de
			// datos)
			logger.error("Error en el proceso de actualización de la dirección con ID " + direccion.getId(), e);
			return Response.status(Status.INTERNAL_SERVER_ERROR)
					.entity("Error en el proceso de actualización de la dirección").build();
		}
	}

	@DELETE
	@Path("/delete")
	@Consumes(MediaType.APPLICATION_JSON)
	@Operation(operationId = "deleteDireccion", summary = "Eliminación de una dirección", description = "Elimina una dirección a partir de su ID", responses = {
			@ApiResponse(responseCode = "200", description = "Dirección eliminada correctamente", content = @Content(mediaType = MediaType.APPLICATION_JSON)),
			@ApiResponse(responseCode = "400", description = "Datos introducidos inválidos"),
			@ApiResponse(responseCode = "404", description = "Dirección no encontrada"),
			@ApiResponse(responseCode = "500", description = "Error en el proceso de eliminación") })
	public Response delete(@QueryParam("id") Long id) {
		if (id == null) {
			return Response.status(Status.BAD_REQUEST).entity("Datos introducidos inválidos").build();
		}

		try {
			boolean isDeleted = direccionService.delete(id);
			if (isDeleted) {
				logger.info("Direccion con id: " + id + "eliminado correctamente");
				return Response.status(Status.OK).entity(direccionService.findById(id)).build();
			} else {
				return Response.status(Status.NOT_FOUND).entity("Dirección con ID " + id + " no encontrada").build();
			}
		} catch (DataException e) {
			logger.error("Error en el proceso de eliminación de la dirección con ID " + id, e);
			return Response.status(Status.BAD_GATEWAY).entity("Error en el proceso de eliminación de la dirección")
					.build();
		}
	}
}
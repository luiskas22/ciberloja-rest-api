package com.luis.ciberloja;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.luis.ciberloja.model.EmpleadoDTO;
import com.luis.ciberloja.service.EmpleadoService;
import com.luis.ciberloja.service.ServiceException;
import com.luis.ciberloja.service.impl.EmpleadoServiceImpl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

@Path("/empleado")
public class EmpleadoResource {

	private EmpleadoService empleadoService = null;

	private static Logger logger = LogManager.getLogger(EmpleadoResource.class);

	public EmpleadoResource() {
		empleadoService = new EmpleadoServiceImpl();
	}

	@Path("/find/{id}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Búsqueda por id de empleado", operationId = "findEmpleadoById", description = "Recupera todos los datos de un empleado por su id", responses = {
			@ApiResponse(responseCode = "200", description = "Empleado encontrado", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = EmpleadoDTO.class))),
			@ApiResponse(responseCode = "404", description = "Empleado no encontrado"),
			@ApiResponse(responseCode = "500", description = "Error interno del servidor") })
	public Response getEmpleadoById(@PathParam("id") Long id) {
		try {
			EmpleadoDTO empleado = empleadoService.findBy(id);
			if (empleado == null) {
				return Response.status(Status.NOT_FOUND).entity("Empleado con ID " + id + " no encontrado.").build();
			}
			return Response.ok(empleado).build();

		} catch (ServiceException e) {
			return Response.status(Status.INTERNAL_SERVER_ERROR)
					.entity("Error al buscar el empleado: " + e.getMessage()).build();
		} catch (Exception e) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Error inesperado al procesar la solicitud")
					.build();
		}
	}

	@POST
	@Path("/autenticar")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(operationId = "autenticarEmpleado", summary = "Autenticación de un empleado", description = "Autenticación de un empleado mediante su ID y contraseña", responses = {
			@ApiResponse(responseCode = "200", description = "Autenticación exitosa", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = EmpleadoDTO.class))),
			@ApiResponse(responseCode = "401", description = "Credenciales inválidas"),
			@ApiResponse(responseCode = "400", description = "Datos de autenticación incompletos o inválidos"),
			@ApiResponse(responseCode = "500", description = "Error interno del servidor") })
	public Response autenticarEmpleado(EmpleadoDTO credenciales) {
		logger.info("Intento de autenticación de empleado con ID: {}", credenciales.getId());

		try {
			// Validación de datos de entrada
			if (credenciales.getId() == null || StringUtils.isBlank(credenciales.getPassword())) {
				logger.warn("Credenciales incompletas para empleado");
				return Response.status(Response.Status.BAD_REQUEST)
						.entity(Map.of("error", "ID y contraseña son requeridos")).build();
			}

			EmpleadoDTO empleadoAutenticado = empleadoService.autenticar(credenciales.getId(),
					credenciales.getPassword());

			if (empleadoAutenticado == null) {
				logger.warn("Autenticación fallida para empleado ID: {}", credenciales.getId());
				return Response.status(Response.Status.UNAUTHORIZED).entity(Map.of("error", "Credenciales inválidas"))
						.build();
			}

			// Eliminar información sensible antes de retornar
			empleadoAutenticado.setPassword(null);

			logger.info("Autenticación exitosa para empleado ID: {}", empleadoAutenticado.getId());

			return Response.ok(empleadoAutenticado).build();

		} catch (Exception e) {
			logger.error("Error inesperado al autenticar empleado: {}", e.getMessage(), e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(Map.of("error", "Error interno del servidor")).build();
		}
	}

	@POST
	@Path("/create")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(operationId = "createEmpleado", summary = "Crear un nuevo empleado", description = "Crea un nuevo empleado con los datos proporcionados", responses = {
			@ApiResponse(responseCode = "200", description = "Empleado creado exitosamente", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = EmpleadoDTO.class))),
			@ApiResponse(responseCode = "400", description = "Datos de empleado inválidos o incompletos"),
			@ApiResponse(responseCode = "409", description = "Conflicto: el empleado ya existe (e.g., DNI duplicado)"),
			@ApiResponse(responseCode = "500", description = "Error interno del servidor") })
	public Response createEmpleado(EmpleadoDTO empleado) {
		logger.info("Intento de creación de empleado con datos: {}", empleado);

		try {
			// Validación de datos de entrada
			if (empleado == null || StringUtils.isBlank(empleado.getNombre())
					|| StringUtils.isBlank(empleado.getApellido1()) || StringUtils.isBlank(empleado.getDniNie())
					|| StringUtils.isBlank(empleado.getPassword()) || empleado.getTipo_empleado_id() == null) {
				logger.warn("Datos incompletos para crear empleado");
				return Response.status(Response.Status.BAD_REQUEST).entity(
						Map.of("error", "Nombre, apellido1, DNI/NIE, contraseña y tipo de empleado son requeridos"))
						.build();
			}

			// Verificar si el empleado ya existe (por ejemplo, por DNI/NIE)
			EmpleadoDTO existingEmpleado = empleadoService.findBy(empleado.getId());
			if (existingEmpleado != null) {
				logger.warn("Empleado con DNI/NIE {} ya existe", empleado.getDniNie());
				return Response.status(Response.Status.CONFLICT)
						.entity(Map.of("error", "Ya existe un empleado con el DNI/NIE proporcionado")).build();
			}

			// Crear la conexión y delegar la creación
			Long empleadoId = empleadoService.registrar(empleado);
			if (empleadoId == null) {
				throw new ServiceException("No se pudo crear el empleado");
			}

			// Actualizar el ID en el DTO y eliminar información sensible antes de
			// devolverlo
			empleado.setId(empleadoId);
			empleado.setPassword(null); // No devolver la contraseña

			logger.info("Empleado creado exitosamente con ID: {}", empleadoId);

			return Response.ok(empleado).build();

		} catch (ServiceException e) {
			logger.error("Error al crear el empleado: {}", e.getMessage(), e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(Map.of("error", "Error al crear el empleado: " + e.getMessage())).build();
		} catch (Exception e) {
			logger.error("Error inesperado al crear empleado: {}", e.getMessage(), e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(Map.of("error", "Error interno del servidor")).build();
		}
	}
}

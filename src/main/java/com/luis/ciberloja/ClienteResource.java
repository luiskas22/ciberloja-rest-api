package com.luis.ciberloja;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.luis.ciberloja.model.ClienteDTO;
import com.luis.ciberloja.service.ClienteService;
import com.luis.ciberloja.service.ServiceException;
import com.luis.ciberloja.service.impl.ClienteServiceImpl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

@Path("/cliente")
public class ClienteResource {

	private ClienteService clienteService = null;

	private static Logger logger = LogManager.getLogger(ClienteResource.class);

	public ClienteResource() {
		clienteService = new ClienteServiceImpl();
	}

	@Path("/find/{id}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Busqueda por id de cliente", operationId = "findClienteById", description = "Recupera todos los datos de un cliente por su id", responses = {
			@ApiResponse(responseCode = "200", description = "Usuario encontrado", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ClienteDTO.class))),
			@ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
			@ApiResponse(responseCode = "400", description = "Error al recuperar los datos") })
	public Response getById(@PathParam("id") Long id) {

		try {
			ClienteDTO cliente = clienteService.findById(id);
			if (cliente == null) {
				return Response.status(Status.NOT_FOUND).entity("Usuario con ID " + id + " no encontrado.").build();
			}
			return Response.status(Status.OK).entity(cliente).build();
		} catch (DataException e) {
			// Registro del error para depuración interna
			e.getMessage(); // Cambiar por un logger en producción
			return Response.status(Status.INTERNAL_SERVER_ERROR)
					.entity("Ha ocurrido un error interno al buscar el usuario.").build();
		}
	}

	@DELETE
	@Path("/delete")
	@Consumes(MediaType.APPLICATION_JSON)
	@Operation(operationId = "deleteCliente", summary = "Eliminación de cliente", description = "Eliminación de un cliente a partir del id que tiene en base de datos", responses = {
			@ApiResponse(responseCode = "200", description = "Cliente eliminado correctamente", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ClienteDTO.class))),
			@ApiResponse(responseCode = "400", description = "Error en el proceso de eliminación del cliente") })
	public Response delete(@QueryParam("id") Long id) {

		if (id == null) {
			return Response.status(Status.BAD_REQUEST).entity("Datos introducidos inválidos").build();
		}

		boolean isDeleted = false;

		try {
			isDeleted = clienteService.delete(id);
		} catch (ServiceException se) {
			logger.error(se.getMessage(), se);
		} catch (DataException de) {
			logger.error(de.getMessage(), de);
		}

		if (isDeleted) {
			return Response.status(Status.OK).entity("Cliente eliminado correctamente").build();
		} else {
			return Response.status(Status.BAD_GATEWAY).entity("Error en el proceso de eliminación del cliente").build();
		}
	}

	@POST
	@Path("/registrar")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(operationId = "registrarCliente", summary = "Registro de cliente", description = "Registro de un cliente introduciendo todos los datos del mismo", responses = {
			@ApiResponse(responseCode = "200", description = "El cliente ha sido registrado correctamente", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ClienteDTO.class))),
			@ApiResponse(responseCode = "400", description = "Error en el proceso de registro del cliente"),
			@ApiResponse(responseCode = "500", description = "Error interno en el servidor") })
	public Response registrar(@FormParam("nombre") String nombre, @FormParam("nickname") String nickname,
			@FormParam("primerApellido") String primerApellido, @FormParam("segundoApellido") String segundoApellido,
			@FormParam("dni") String dni, @FormParam("email") String email, @FormParam("telefono") String telefono,
			@FormParam("password") String password) {
		// Validación de los parámetros
		if (nombre == null || nombre.isEmpty() || nickname == null || nickname.isEmpty() || primerApellido == null
				|| primerApellido.isEmpty() || dni == null || dni.isEmpty() || email == null || email.isEmpty()
				|| telefono == null || telefono.isEmpty() || password == null || password.isEmpty()) {
			return Response.status(Response.Status.BAD_REQUEST).entity("Todos los campos son obligatorios.").build();
		}

		try {
			// Crear un objeto ClienteDTO
			ClienteDTO clienteDTO = new ClienteDTO();
			clienteDTO.setNombre(nombre);
			clienteDTO.setNickname(nickname);
			clienteDTO.setApellido1(primerApellido);
			clienteDTO.setApellido2(segundoApellido);
			clienteDTO.setDniNie(dni);
			clienteDTO.setEmail(email);
			clienteDTO.setTelefono(telefono);
			clienteDTO.setPassword(password);
			clienteDTO.setRol_id(1l);

			// Registrar cliente y obtener su ID
			Long id = clienteService.registrar(clienteDTO);
			ClienteDTO newCliente = clienteService.findById(id);
			return Response.status(Response.Status.OK).entity(newCliente).build();
		} catch (Exception e) {
			logger.error("Error en el proceso de registro del cliente", e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity("Error interno en el proceso de registro del cliente.").build();
		}
	}

	@POST
	@Path("/autenticar")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(operationId = "autenticarCliente", summary = "Autenticación de un cliente", description = "Autenticación de un cliente introduciendo su corre electrónico y su contraseña", responses = {
			@ApiResponse(responseCode = "200", description = "Proceso de autenticación correcto", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ClienteDTO.class))),
			@ApiResponse(responseCode = "401", description = "Credenciales inválidas"),
			@ApiResponse(responseCode = "400", description = "Error en el proceso de autenticación") })
	public Response autenticar(ClienteDTO credenciales) {
		logger.info("Intento de autenticación para: {}", credenciales.getEmail());

		try {
			// Validate input
			if (credenciales.getEmail() == null || credenciales.getPassword() == null) {
				logger.warn("Credenciales incompletas");
				return Response.status(Response.Status.BAD_REQUEST)
						.entity(Map.of("mensaje", "Credenciales incompletas")).build();
			}

			ClienteDTO clienteAutenticado = clienteService.autenticar(credenciales.getEmail(),
					credenciales.getPassword());

			// Check if authentication failed
			if (clienteAutenticado == null) {
				logger.warn("Autenticación fallida para: {}", credenciales.getEmail());
				return Response.status(Response.Status.UNAUTHORIZED).entity(Map.of("mensaje", "Credenciales inválidas"))
						.build();
			}

			// Optionally, remove sensitive information before returning
			clienteAutenticado.setPassword(null);

			logger.info("Autenticación exitosa para: {}", credenciales.getEmail());
			return Response.ok(clienteAutenticado).build();

		} catch (PinguelaException pe) {
			logger.error("Error en autenticación: {}", pe.getMessage(), pe);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(Map.of("mensaje", "Error interno durante la autenticación")).build();
		} catch (Exception e) {
			logger.error("Error inesperado: {}", e.getMessage(), e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Map.of("mensaje", "Error inesperado"))
					.build();
		}
	}

	@POST
	@Path("/update")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(operationId = "updateCliente", summary = "Actualización de un cliente", description = "Actualiza un cliente introduciendo todos los datos del mismo", responses = {
			@ApiResponse(responseCode = "200", description = "El cliente fue actualizado correctamente", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ClienteDTO.class))),
			@ApiResponse(responseCode = "400", description = "Datos introducidos incorrectos o incompletos"),
			@ApiResponse(responseCode = "500", description = "Error en el proceso de actualización del cliente") })
	public Response update(ClienteDTO cliente) {

		try {

			boolean isUpdated = clienteService.update(cliente);
			if (isUpdated) {
				ClienteDTO clienteActualizado = clienteService.findById(cliente.getId());
				return Response.status(Status.OK).entity(clienteActualizado).build();
			} else {
				return Response.status(Status.BAD_REQUEST).entity("Datos introducidos incorrectos o incompletos")
						.build();
			}

		} catch (PinguelaException pe) {
			logger.error(pe.getMessage(), pe);
			return Response.status(Status.INTERNAL_SERVER_ERROR)
					.entity("Error en el proceso de actualización del cliente").build();
		}
	}

	@POST
	@Path("/forgot-password")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(operationId = "forgotPassword", summary = "Solicitud de restablecimiento de contraseña", description = "Genera un token de restablecimiento de contraseña y envía un correo electrónico con un enlace para restablecer la contraseña", responses = {
			@ApiResponse(responseCode = "200", description = "Enlace de restablecimiento enviado correctamente", content = @Content(mediaType = MediaType.APPLICATION_JSON)),
			@ApiResponse(responseCode = "400", description = "Correo electrónico inválido o no registrado"),
			@ApiResponse(responseCode = "500", description = "Error interno al procesar la solicitud") })
	public Response forgotPassword(Map<String, String> request) {
		String email = request.get("email");
		logger.info("Solicitud de restablecimiento de contraseña para: {}", email);

		if (email == null || email.trim().isEmpty()) {
			logger.warn("Correo electrónico no proporcionado");
			return Response.status(Status.BAD_REQUEST).entity(Map.of("mensaje", "El correo electrónico es obligatorio"))
					.build();
		}

		try {
			String token = clienteService.generatePasswordResetToken(email);
			if (token != null) {
				logger.info("Enlace de restablecimiento enviado a: {}", email);
				return Response.status(Status.OK)
						.entity(Map.of("mensaje", "Enlace de restablecimiento enviado correctamente")).build();
			} else {
				logger.warn("Correo electrónico no encontrado: {}", email);
				return Response.status(Status.BAD_REQUEST).entity(Map.of("mensaje", "Correo electrónico no registrado"))
						.build();
			}
		} catch (DataException e) {
			logger.error("Error al procesar solicitud de restablecimiento para {}: {}", email, e.getMessage(), e);
			return Response.status(Status.BAD_REQUEST).entity(Map.of("mensaje", e.getMessage())).build();
		} catch (Exception e) {
			logger.error("Error inesperado al procesar solicitud de restablecimiento: {}", e.getMessage(), e);
			return Response.status(Status.INTERNAL_SERVER_ERROR)
					.entity(Map.of("mensaje", "Error interno al procesar la solicitud")).build();
		}
	}

	@POST
	@Path("/reset-password")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(operationId = "resetPassword", summary = "Restablecimiento de contraseña", description = "Valida un token de restablecimiento y actualiza la contraseña del cliente", responses = {
			@ApiResponse(responseCode = "200", description = "Contraseña actualizada correctamente", content = @Content(mediaType = MediaType.APPLICATION_JSON)),
			@ApiResponse(responseCode = "400", description = "Token inválido o datos incorrectos"),
			@ApiResponse(responseCode = "500", description = "Error interno al procesar la solicitud") })
	public Response resetPassword(Map<String, String> request) {
		String token = request.get("token");
		String newPassword = request.get("newPassword");
		logger.info("Intento de restablecimiento de contraseña con token: {}", token);

		if (token == null || token.trim().isEmpty() || newPassword == null || newPassword.trim().isEmpty()) {
			logger.warn("Token o nueva contraseña no proporcionados");
			return Response.status(Status.BAD_REQUEST)
					.entity(Map.of("mensaje", "El token y la nueva contraseña son obligatorios")).build();
		}

		try {
			boolean updated = clienteService.validateAndUpdatePassword(token, newPassword);
			if (updated) {
				logger.info("Contraseña actualizada correctamente para token: {}", token);
				return Response.status(Status.OK).entity(Map.of("mensaje", "Contraseña actualizada correctamente"))
						.build();
			} else {
				logger.warn("No se pudo actualizar la contraseña para token: {}", token);
				return Response.status(Status.BAD_REQUEST)
						.entity(Map.of("mensaje", "No se pudo actualizar la contraseña")).build();
			}
		} catch (DataException e) {
			logger.error("Error al restablecer contraseña: {}", e.getMessage(), e);
			return Response.status(Status.BAD_REQUEST).entity(Map.of("mensaje", e.getMessage())).build();
		} catch (Exception e) {
			logger.error("Error inesperado al restablecer contraseña: {}", e.getMessage(), e);
			return Response.status(Status.INTERNAL_SERVER_ERROR)
					.entity(Map.of("mensaje", "Error interno al procesar la solicitud")).build();
		}
	}
}

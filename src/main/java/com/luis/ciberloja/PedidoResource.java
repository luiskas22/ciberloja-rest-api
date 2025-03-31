package com.luis.ciberloja;

import java.text.SimpleDateFormat;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.luis.ciberloja.model.Pedido;
import com.luis.ciberloja.model.PedidoCriteria;

import com.luis.ciberloja.service.MailException;
import com.luis.ciberloja.service.PedidoService;
import com.luis.ciberloja.service.impl.PedidoServiceImpl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

@Path("/pedido")
public class PedidoResource {

	private PedidoService pedidoService = null;

	private static Logger logger = LogManager.getLogger(PedidoResource.class);

	public PedidoResource() {
		pedidoService = new PedidoServiceImpl();
	}

	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Obtener un pedido por ID", operationId = "findPedidoById", description = "Este endpoint permite obtener un pedido del sistema por su ID.", responses = {
			@ApiResponse(responseCode = "200", description = "pedido encontrado", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = Pedido.class))),
			@ApiResponse(responseCode = "404", description = "pedido no encontrado"),
			@ApiResponse(responseCode = "500", description = "Error interno en el servidor al intentar obtener el pedido") })
	public Response findById(
			@Parameter(description = "ID del pedido a buscar", required = true) @PathParam("id") Long id) {

		try {
			logger.info("Buscando pedido con ID: " + id);

			Pedido p = pedidoService.findBy(id);

			if (p == null) {
				logger.warn("pedido con ID " + id + " no encontrado.");
				return Response.status(Status.NOT_FOUND).entity("pedido con ID " + id + " no encontrada.").build();
			}

			logger.info("pedido con ID " + id + " encontrado.");
			return Response.status(Status.OK).entity(p).build();
		} catch (PinguelaException pe) {
			logger.error("Error al buscar pedido con ID: " + id, pe);
			return Response.status(Status.INTERNAL_SERVER_ERROR)
					.entity("Ha ocurrido un error interno al buscar el pedido: " + pe.getMessage()).build();
		} catch (Exception e) {
			logger.error("Error inesperado al buscar el pedido con ID: " + id, e);
			return Response.status(Status.INTERNAL_SERVER_ERROR)
					.entity("Ha ocurrido un error inesperado al buscar el pedido: " + e.getMessage()).build();
		}
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(operationId = "findPedidosByCriteria", summary = "Búsqueda de pedidos por criteria", description = "Búsqueda de pedidos a partir de varios parámetros introducidos", responses = {
			@ApiResponse(responseCode = "200", description = "Pedidos encontrados", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = Pedido[].class))),
			@ApiResponse(responseCode = "400", description = "Datos introducidos incorrectos"),
			@ApiResponse(responseCode = "500", description = "Error al procesar la solicitud") })
	public Response findByCriteria(@QueryParam("id") Long id, @QueryParam("fechaDesde") String fechaDesde,
			@QueryParam("fechaHasta") String fechaHasta, @QueryParam("precioDesde") Double precioDesde,
			@QueryParam("precioHasta") Double precioHasta, @QueryParam("clienteId") Long clienteId,
			@QueryParam("tipoEstadoPedidoId") Integer tipoEstadoPedidoId) {

		PedidoCriteria pedidoCriteria = new PedidoCriteria();
		pedidoCriteria.setId(id);
		pedidoCriteria.setPrecioDesde(precioDesde);
		pedidoCriteria.setPrecioHasta(precioHasta);
		pedidoCriteria.setClienteId(clienteId);
		pedidoCriteria.setTipoEstadoPedidoId(tipoEstadoPedidoId);

		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		try {
			if (fechaDesde != null) {
				pedidoCriteria.setFechaDesde(formatter.parse(fechaDesde));
			}
			if (fechaHasta != null) {
				pedidoCriteria.setFechaHasta(formatter.parse(fechaHasta));
			}
		} catch (Exception pe) {
			logger.error("Error parseando la fecha: " + pe.getMessage(), pe);
			return Response.status(Status.BAD_REQUEST).entity("Formato de fecha inválido. Usa yyyy-MM-dd.").build();
		}

		try {
			List<Pedido> result = pedidoService.findByCriteria(pedidoCriteria, 1, Integer.MAX_VALUE).getPage();
			return Response.status(Status.OK).entity(result).build();
		} catch (DataException de) {
			logger.error("Data error: " + de.getMessage(), de);
			return Response.status(Status.INTERNAL_SERVER_ERROR)
					.entity("Error en el proceso de búsqueda de los pedidos").build();
		}
	}

	@GET
	@Path("/cliente/{clienteId}")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(operationId = "findPedidosByClienteId", summary = "Obtener pedidos por ID de cliente", description = "Este endpoint permite obtener todos los pedidos asociados a un cliente por su ID.", responses = {
			@ApiResponse(responseCode = "200", description = "Pedidos encontrados", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = Pedido[].class))),
			@ApiResponse(responseCode = "204", description = "No se encontraron pedidos para el cliente"),
			@ApiResponse(responseCode = "400", description = "ID de cliente inválido"),
			@ApiResponse(responseCode = "500", description = "Error interno al buscar los pedidos") })
	public Response findPedidosByClienteId(
			@Parameter(description = "ID del cliente para buscar sus pedidos", required = true) @PathParam("clienteId") Long clienteId) {

		try {
			if (clienteId == null || clienteId <= 0) {
				logger.warn("ID de cliente inválido: {}", clienteId);
				return Response.status(Status.BAD_REQUEST).entity("El ID del cliente debe ser un número positivo")
						.build();
			}

			logger.info("Buscando pedidos para el cliente con ID: {}", clienteId);

			List<Pedido> pedidos = pedidoService.findPedidosByClienteId(clienteId).getPage();

			if (pedidos.isEmpty()) {
				logger.info("No se encontraron pedidos para el cliente con ID: {}", clienteId);
				return Response.status(Status.NO_CONTENT)
						.entity("No se encontraron pedidos para el cliente con ID: " + clienteId).build();
			}

			logger.info("Se encontraron {} pedidos para el cliente con ID: {}", pedidos.size(), clienteId);
			return Response.status(Status.OK).entity(pedidos).build();

		} catch (DataException de) {
			logger.error("Error al buscar pedidos para el cliente con ID: {}", clienteId, de);
			return Response.status(Status.INTERNAL_SERVER_ERROR)
					.entity("Error en el proceso de búsqueda de pedidos: " + de.getMessage()).build();
		} catch (Exception e) {
			logger.error("Error inesperado al buscar pedidos para el cliente con ID: {}", clienteId, e);
			return Response.status(Status.INTERNAL_SERVER_ERROR)
					.entity("Ha ocurrido un error inesperado al buscar los pedidos: " + e.getMessage()).build();
		}
	}

	@POST
	@Path("/create")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(operationId = "createPedido", summary = "Creación de un pedido", description = "Crea un pedido introduciendo todos los datos del mismo", responses = {
			@ApiResponse(responseCode = "200", description = "El pedido fue creado correctamente", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = Pedido.class))),
			@ApiResponse(responseCode = "400", description = "Error al enviar el correo de creación del pedido") })
	public Response create(Pedido pedido) {
		try {
			Long id = pedidoService.create(pedido);
			Pedido pedidoCreated = pedidoService.findBy(id);
			return Response.status(Status.OK).entity(pedidoCreated).build();
		} catch (DataException de) {
			logger.error(de.getMessage(), de);
			return Response.status(Status.BAD_REQUEST).entity("Error en el proceso de creación del pedido").build();
		} catch (MailException me) {
			logger.error("Error al enviar el correo electrónico", me.getMessage(), me);
			return Response.status(Status.BAD_REQUEST).entity("Error al enviar el correo de creación de peiddo")
					.build();
		}

	}

	@DELETE
	@Path("/delete")
	@Consumes(MediaType.APPLICATION_JSON)
	@Operation(operationId = "deletePedido", summary = "Eliminación de un pedido", description = "Elimina un pedido a partir del identificador introducido", responses = {
			@ApiResponse(responseCode = "200", description = "Pedido eliminado correctamente"),
			@ApiResponse(responseCode = "400", description = "Error en el proceso de eliminación del pedido") })
	public Response delete(@QueryParam("id") Long id) {

		try {
			pedidoService.delete(id);
			return Response.status(Status.OK).entity("Pedido eliminado correctamente").build();
		} catch (DataException de) {
			logger.error(de.getMessage(), de);
			return Response.status(Status.BAD_REQUEST).entity("Error en el proceso de eliminación del pedido").build();
		}

	}

	@PUT
	@Path("/update")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(operationId = "updatePedido", summary = "Actualización de un pedido", description = "Actualiza todos los datos pertenecientes al pedido", responses = {
			@ApiResponse(responseCode = "200", description = "Pedido actualizado correctamente", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = Pedido.class))),
			@ApiResponse(responseCode = "400", description = "No se pudo actualizar el pedido"),
			@ApiResponse(responseCode = "500", description = "Error interno en el proceso de actualización del pedido") })
	public Response update(Pedido pedido) {

		try {
			if (pedidoService.update(pedido)) {
				Pedido pedidoActualizado = pedidoService.findBy(pedido.getId());
				return Response.ok().entity(pedidoActualizado).build();
			} else {
				return Response.status(Status.BAD_REQUEST).entity("No se ha podido actualizar el pedido").build();
			}

		} catch (PinguelaException pe) {
			logger.error(pe.getMessage(), pe);
			return Response.status(Status.INTERNAL_SERVER_ERROR)
					.entity("Error en el proceso de actualización del pedido").build();
		}
	}
}

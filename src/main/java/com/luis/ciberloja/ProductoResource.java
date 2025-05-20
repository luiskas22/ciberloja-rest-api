package com.luis.ciberloja;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import com.luis.ciberloja.model.ProductoCriteria;
import com.luis.ciberloja.model.ProductoDTO;
import com.luis.ciberloja.model.Results;
import com.luis.ciberloja.service.ProductoService;
import com.luis.ciberloja.service.impl.ArtigosCiberlojaImpl;
import com.luis.ciberloja.service.impl.ProductoServiceImpl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.FormParam;
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

@Component
@Path("/producto")
public class ProductoResource {

	private static Logger logger = LogManager.getLogger(ProductoResource.class);

	private final ProductoService productoService;
	@Inject
	private final ArtigosCiberlojaImpl soapClientService = null;

	public ProductoResource() {
		productoService = new ProductoServiceImpl();
	}

	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Obtener una producto por ID", operationId = "findProductoById", description = "Este endpoint permite obtener un producto del sistema por su ID.", responses = {
			@ApiResponse(responseCode = "200", description = "producto encontrado", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ProductoDTO.class))),
			@ApiResponse(responseCode = "404", description = "producto no encontrado"),
			@ApiResponse(responseCode = "500", description = "Error interno en el servidor al intentar obtener la producto") })
	public Response findById(
			@Parameter(description = "ID del producto a buscar", required = true) @PathParam("id") String id) {

		try {
			logger.info("Buscando producto con ID: " + id);

			ProductoDTO p = productoService.findById(id);

			if (p == null) {
				logger.warn("producto con ID " + id + " no encontrada.");
				return Response.status(Status.NOT_FOUND).entity("producto con ID " + id + " no encontrada.").build();
			}

			logger.info("producto con ID " + id + " encontrado.");
			return Response.status(Status.OK).entity(p).build();
		} catch (PinguelaException pe) {
			logger.error("Error al buscar la producto con ID: " + id, pe);
			return Response.status(Status.INTERNAL_SERVER_ERROR)
					.entity("Ha ocurrido un error interno al buscar el producto: " + pe.getMessage()).build();
		} catch (Exception e) {
			logger.error("Error inesperado al buscar la producto con ID: " + id, e);
			return Response.status(Status.INTERNAL_SERVER_ERROR)
					.entity("Ha ocurrido un error inesperado al buscar el producto: " + e.getMessage()).build();
		}
	}

	@GET
	@Path("/search")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Buscar productos por criterios", operationId = "findProductosByCriteria", description = "Este endpoint permite buscar productos aplicando filtros opcionales como ID, nombre, rango de precios, cantidad de unidades, y localización.", responses = {
			@ApiResponse(responseCode = "200", description = "Productos encontrados", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = Results.class))),
			@ApiResponse(responseCode = "400", description = "Criterios de búsqueda no proporcionados o inválidos"),
			@ApiResponse(responseCode = "404", description = "No se encontraron productos con los criterios proporcionados"),
			@ApiResponse(responseCode = "500", description = "Error interno en el servidor al procesar la búsqueda") })
	public Response findByCriteria(@QueryParam("id") String id, @QueryParam("nombre") String nombre,
			@QueryParam("descripcion") String descripcion, @QueryParam("precioMin") Double precioMin,
			@QueryParam("precioMax") Double precioMax, @QueryParam("stockMin") Integer stockMin,
			@QueryParam("stockMax") Integer stockMax, @QueryParam("nombreCategoria") String nombreCategoria,
			@QueryParam("nombreMarca") String nombreMarca,
			@QueryParam("nombreUnidadMedida") String nombreUnidadMedida) {

		try {
			ProductoCriteria criteria = new ProductoCriteria();

			criteria.setId(id);
			criteria.setNombre(nombre);
			criteria.setDescripcion(descripcion);
			criteria.setPrecioMin(precioMin);
			criteria.setPrecioMax(precioMax);
			criteria.setStockMin(stockMin);
			criteria.setStockMax(stockMax);
			criteria.setNombreCategoria(nombreCategoria);
			criteria.setNombreMarca(nombreMarca);
			criteria.setNombreUnidadMedida(nombreUnidadMedida);

			Results<ProductoDTO> resultados = productoService.findBy(criteria, 1, Integer.MAX_VALUE);

			if (resultados == null) {
				logger.warn("No se encontraron resultados con los criterios: " + criteria);
				return Response.status(Response.Status.NOT_FOUND)
						.entity("No se encontraron productos con los criterios proporcionados.").build();
			}

			return Response.ok(resultados).build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity("Error al buscar productos: " + e.getMessage()).build();
		}
	}

	@POST
	@Path("/create")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Crear un nuevo producto", operationId = "createProducto", description = "Este endpoint permite crear un nuevo producto en el sistema.", responses = {
			@ApiResponse(responseCode = "200", description = "Producto creado exitosamente", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ProductoDTO.class))),
			@ApiResponse(responseCode = "400", description = "Datos de entrada inválidos o incompletos"),
			@ApiResponse(responseCode = "500", description = "Error interno en el servidor al intentar crear el producto") })
	public Response createProducto(
			@Parameter(description = "Nombre del producto", required = true) @FormParam("nombre") String nombre,
			@Parameter(description = "Descripción del producto") @FormParam("descripcion") String descripcion,
			@Parameter(description = "Precio del producto", required = true) @FormParam("precio") Double precio,
			@Parameter(description = "Stock disponible del producto", required = true) @FormParam("stockDisponible") Double stockDisponible,
			@Parameter(description = "ID de la categoría", required = true) @FormParam("idCategoria") Long idCategoria,
			@Parameter(description = "ID de la marca", required = true) @FormParam("idMarca") Long idMarca,
			@Parameter(description = "ID de la unidad de medida", required = true) @FormParam("idUnidadMedida") Long idUnidadMedida) {

		try {
			logger.info("Intentando crear un nuevo producto.");

			// Validar los datos de entrada
			if (nombre == null || precio == null || stockDisponible == null || idCategoria == null || idMarca == null
					|| idUnidadMedida == null) {
				logger.warn("Datos de entrada inválidos o incompletos.");
				return Response.status(Status.BAD_REQUEST).entity("Datos de entrada inválidos o incompletos.").build();
			}

			// Crear el DTO del producto
			ProductoDTO producto = new ProductoDTO();
			producto.setNombre(nombre);
			producto.setPrecio(precio);
			producto.setStockDisponible(stockDisponible);

			// Crear el producto en el DAO
			String id = productoService.create(producto);
			logger.info("Producto creado exitosamente con ID: " + id);
			return Response.status(Status.OK).entity(producto).build();
		} catch (DataException e) {
			logger.error("Error al crear el producto", e);
			return Response.status(Status.INTERNAL_SERVER_ERROR)
					.entity("Ha ocurrido un error interno al crear el producto: " + e.getMessage()).build();
		} catch (Exception e) {
			logger.error("Error inesperado al crear el producto", e);
			return Response.status(Status.INTERNAL_SERVER_ERROR)
					.entity("Ha ocurrido un error inesperado al crear el producto: " + e.getMessage()).build();
		}
	}

	@PUT
	@Path("/update/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Actualizar un producto", operationId = "updateProducto", description = "Este endpoint permite actualizar los detalles de un producto existente en el sistema.", responses = {
			@ApiResponse(responseCode = "200", description = "Producto actualizado exitosamente", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ProductoDTO.class))),
			@ApiResponse(responseCode = "400", description = "Datos de entrada inválidos o incompletos"),
			@ApiResponse(responseCode = "404", description = "Producto no encontrado"),
			@ApiResponse(responseCode = "500", description = "Error interno en el servidor al intentar actualizar el producto") })
	public Response updateProducto(
			@Parameter(description = "ID del producto a actualizar", required = true) @PathParam("id") String id,
			@Parameter(description = "Objeto ProductoDTO con los nuevos datos", required = true) ProductoDTO productoDTO) {

		try {
			logger.info("Intentando actualizar el producto con ID: " + id);

			// Validar los datos de entrada
			if (productoDTO == null || productoDTO.getNombre() == null || productoDTO.getPrecio() == null
					|| productoDTO.getStockDisponible() == null) {
				logger.warn("Datos de entrada inválidos o incompletos.");
				return Response.status(Status.BAD_REQUEST).entity("Datos de entrada inválidos o incompletos.").build();
			}

			// Asegurarse de que el ID en la URL coincide con el del DTO
			if (!id.equals(productoDTO.getId())) {
				logger.warn("El ID en la URL no coincide con el ID del producto en el cuerpo.");
				return Response.status(Status.BAD_REQUEST)
						.entity("El ID en la URL no coincide con el ID del producto en el cuerpo.").build();
			}

			// Buscar el producto existente
			ProductoDTO existingProducto = productoService.findById(id);
			if (existingProducto == null) {
				logger.warn("Producto con ID " + id + " no encontrado.");
				return Response.status(Status.NOT_FOUND).entity("Producto con ID " + id + " no encontrado.").build();
			}

			// Actualizar los valores
			existingProducto.setNombre(productoDTO.getNombre());
			existingProducto.setPrecio(productoDTO.getPrecio());
			existingProducto.setStockDisponible(productoDTO.getStockDisponible());

			// Guardar la actualización
			boolean updated = productoService.update(existingProducto);
			if (!updated) {
				logger.warn("No se pudo actualizar el producto con ID: " + id);
				return Response.status(Status.NOT_FOUND)
						.entity("Producto con ID " + id + " no encontrado o no actualizado.").build();
			}

			logger.info("Producto con ID " + id + " actualizado exitosamente.");
			return Response.status(Status.OK).entity(existingProducto).build();

		} catch (DataException e) {
			logger.error("Error al actualizar el producto con ID: " + id, e);
			return Response.status(Status.INTERNAL_SERVER_ERROR)
					.entity("Error al actualizar el producto: " + e.getMessage()).build();
		}
	}

	@DELETE
	@Path("/delete/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Eliminar un producto", operationId = "deleteProducto", description = "Este endpoint permite eliminar un producto del sistema por su ID.", responses = {
			@ApiResponse(responseCode = "200", description = "Producto eliminado exitosamente", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ProductoDTO.class))),
			@ApiResponse(responseCode = "404", description = "Producto no encontrado"),
			@ApiResponse(responseCode = "500", description = "Error interno en el servidor al intentar eliminar el producto") })
	public Response deleteProducto(
			@Parameter(description = "ID del producto a eliminar", required = true) @PathParam("id") String id) {

		try {
			logger.info("Intentando eliminar el producto con ID: " + id);

			// Intentar eliminar el producto directamente
			boolean deleted = productoService.delete(id);

			if (deleted) {
				logger.info("Producto con ID " + id + " eliminado exitosamente.");
				return Response.status(Status.OK).entity(productoService.findById(id)).build();
			} else {
				logger.warn("Producto con ID " + id + " no encontrado.");
				return Response.status(Status.NOT_FOUND).entity("Producto con ID " + id + " no encontrado.").build();
			}

		} catch (DataException e) {
			logger.error("Error al eliminar el producto con ID: " + id, e);
			return Response.status(Status.INTERNAL_SERVER_ERROR)
					.entity("Ha ocurrido un error interno al eliminar el producto: " + e.getMessage()).build();
		}
	}

	@GET
	@Path("/sync-soap")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Buscar un producto", operationId = "syncProductosFromSoap", description = "Este endpoint permite buscar un producto del sistema ", responses = {
			@ApiResponse(responseCode = "200", description = "Producto buscado exitosamente", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = Results.class))),
			@ApiResponse(responseCode = "404", description = "Producto no encontrado"),
			@ApiResponse(responseCode = "500", description = "Error interno en el servidor al intentar buscar el producto") })
	public Response syncProductosFromSoap(@QueryParam("empresa") String empresa,
			@QueryParam("utilizador") String utilizador, @QueryParam("password") String password,
			@QueryParam("descricao") String nombre, @QueryParam("PVP3Min") Double precioMin,
			@QueryParam("PVP3Max") Double precioMax, @QueryParam("StockMin") Double stockMin,
			@QueryParam("StockMax") Double stockMax) {

		try {
			logger.info(
					"Iniciando sincronización de productos desde el servicio SOAP con filtros: nombre={}, precioMin={}, precioMax={}, stockMin={}, stockMax={}",
					nombre, precioMin, precioMax, stockMin, stockMax);

			// Validate credentials
			if (empresa == null || utilizador == null || password == null || empresa.trim().isEmpty()
					|| utilizador.trim().isEmpty() || password.trim().isEmpty()) {
				logger.warn("Credenciales inválidas o incompletas");
				return Response.status(Status.BAD_REQUEST).entity("Credenciales inválidas o incompletas").build();
			}

			// Validate filter parameters
			if (precioMin != null && precioMax != null && precioMin > precioMax) {
				logger.warn("precioMin ({}) no puede ser mayor que precioMax ({})", precioMin, precioMax);
				return Response.status(Status.BAD_REQUEST).entity("precioMin no puede ser mayor que precioMax").build();
			}
			if (stockMin != null && stockMax != null && stockMin > stockMax) {
				logger.warn("stockMin ({}) no puede ser mayor que stockMax ({})", stockMin, stockMax);
				return Response.status(Status.BAD_REQUEST).entity("stockMin no puede ser mayor que stockMax").build();
			}
			// Fetch products from SOAP service
			List<ProductoDTO> productos;
			try {

				productos = soapClientService.getArtigosCiberlojaSite();
			} catch (Exception e) {
				logger.error("Error al obtener productos del servicio SOAP: {}", e.getMessage(), e);
				return Response.status(Status.INTERNAL_SERVER_ERROR)
						.entity("Error al obtener productos del servicio SOAP: " + e.getMessage()).build();
			}

			// Apply filters
			productos = productos.stream()
					.filter(p -> nombre == null || p.getNombre().toLowerCase().contains(nombre.toLowerCase()))
					.filter(p -> precioMin == null || p.getPrecio() >= precioMin)
					.filter(p -> precioMax == null || p.getPrecio() <= precioMax)
					.filter(p -> stockMin == null || p.getStockDisponible() >= stockMin)
					.filter(p -> stockMax == null || p.getStockDisponible() <= stockMax).collect(Collectors.toList());

			// Check if products were found
			if (productos.isEmpty()) {
				logger.warn("No se encontraron productos que coincidan con los filtros");
				return Response.status(Status.NOT_FOUND).entity("No se encontraron productos").build();
			}

			// Wrap the list in a Results object for the response
			Results<ProductoDTO> resultados = new Results<>();
			resultados.setPage(productos);
			resultados.setTotal(productos.size());

			logger.info("Sincronización exitosa: {} productos obtenidos", resultados.getTotal());
			return Response.status(Status.OK).entity(resultados).build();

		} catch (Exception e) {
			logger.error("Error al sincronizar productos desde el servicio SOAP: {}", e.getMessage(), e);
			return Response.status(Status.INTERNAL_SERVER_ERROR)
					.entity("Error al sincronizar productos desde el servicio SOAP: " + e.getMessage()).build();
		}
	}

	@GET
	@Path("/sync-soap/findById")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Buscar un producto por ID", operationId = "findProductoByIdFromSoap", description = "Este endpoint permite buscar un producto específico por su ID desde el sistema.", responses = {
			@ApiResponse(responseCode = "200", description = "Producto encontrado exitosamente", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ProductoDTO.class))),
			@ApiResponse(responseCode = "400", description = "Credenciales inválidas o ID inválido"),
			@ApiResponse(responseCode = "404", description = "Producto no encontrado"),
			@ApiResponse(responseCode = "500", description = "Error interno en el servidor al intentar buscar el producto") })
	public Response findProductoByIdFromSoap(@QueryParam("empresa") String empresa,
			@QueryParam("utilizador") String utilizador, @QueryParam("password") String password,
			@QueryParam("id") String id) {

		try {
			logger.info("Iniciando búsqueda de producto por ID: id={}", id);

			// Validate credentials
			if (empresa == null || utilizador == null || password == null || empresa.trim().isEmpty()
					|| utilizador.trim().isEmpty() || password.trim().isEmpty()) {
				logger.warn("Credenciales inválidas o incompletas");
				return Response.status(Status.BAD_REQUEST).entity("Credenciales inválidas o incompletas").build();
			}

			// Validate ID
			if (id == null || id.trim().isEmpty()) {
				logger.warn("ID del producto no proporcionado o inválido");
				return Response.status(Status.BAD_REQUEST).entity("ID del producto no proporcionado o inválido")
						.build();
			}

			// Fetch products from SOAP service
			List<ProductoDTO> productos;
			try {

				productos = soapClientService.getArtigosCiberlojaSite();
			} catch (Exception e) {
				logger.error("Error al obtener productos del servicio SOAP: {}", e.getMessage(), e);
				return Response.status(Status.INTERNAL_SERVER_ERROR)
						.entity("Error al obtener productos del servicio SOAP: " + e.getMessage()).build();
			}

			// Find product by ID
			Optional<ProductoDTO> producto = productos.stream().filter(p -> p.getId().equals(id)).findFirst();

			// Check if product was found
			if (producto.isEmpty()) {
				logger.warn("No se encontró un producto con ID: {}", id);
				return Response.status(Status.NOT_FOUND).entity("No se encontró un producto con el ID proporcionado")
						.build();
			}

			logger.info("Producto encontrado exitosamente: id={}", id);
			return Response.status(Status.OK).entity(producto.get()).build();

		} catch (Exception e) {
			logger.error("Error al buscar producto por ID: {}", e.getMessage(), e);
			return Response.status(Status.INTERNAL_SERVER_ERROR)
					.entity("Error al buscar producto por ID: " + e.getMessage()).build();
		}
	}
}
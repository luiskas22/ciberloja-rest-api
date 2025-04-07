package com.luis.ciberloja;

import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;

import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;
import jakarta.ws.rs.ApplicationPath;

@OpenAPIDefinition(info = @Info(title = "CiberLoja API", version = "1.0", description = "API para gestionar tienda ", contact = @Contact(name = "Soporte API", email = "soporte@reflevision.com", url = "https://ciberloja.com"), license = @License(name = "MIT", url = "https://opensource.org/licenses/MIT")), servers = {
		@Server(url = "http://localhost:8080/ciberloja-rest-api/", description = "Servidor Local")
		// Cuando lo subais al hosting
		// @Server(url = "https://api.thegoldenbook.com", description = "Servidor de
		// Producción"),

})

@ApplicationPath("/api")
public class CiberLojaApplication extends ResourceConfig {
	public CiberLojaApplication() {
		packages("com.luis.ciberloja");
		register(MultiPartFeature.class);
		register(OpenApiResource.class);

		// Add these multipart configuration properties
		property("jersey.config.server.multipart.location", System.getProperty("java.io.tmpdir"));
		property("jersey.config.server.multipart.maxFileSize", "10485760"); // 10MB
		property("jersey.config.server.multipart.maxRequestSize", "10485760"); // 10MB
		property("jersey.config.server.multipart.fileSizeThreshold", "1048576"); // 1MB
	}
}
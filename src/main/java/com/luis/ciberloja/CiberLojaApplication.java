package com.luis.ciberloja;

import org.glassfish.jersey.server.ResourceConfig;


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
		packages(CiberLojaApplication.class.getPackage().getName());
		register(io.swagger.v3.jaxrs2.integration.resources.OpenApiResource.class);
	}
}

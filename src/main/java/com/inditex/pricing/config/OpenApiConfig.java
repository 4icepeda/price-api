package com.inditex.pricing.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI / Swagger UI configuration.
 * Accessible at /swagger-ui.html when the application is running.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI priceApiOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Inditex Price API")
                        .description("""
                                REST API para consultar el precio aplicable de un producto para una marca en una fecha dada.
                                Cuando existen varias tarifas solapadas, se aplica la de mayor prioridad.
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Inditex")
                                .url("https://www.inditex.com")));
    }
}

package training.gateway.openapi;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayOpenApiConfig {
  @Bean
  OpenAPI gatewayOpenAPI() {
    return new OpenAPI()
        .info(new Info()
            .title("api-gateway API")
            .version("1.0.0")
            .description("Training entry point for authentication and secured microservice routing. Use /auth/token to get a JWT before calling protected routes."))
        .components(new Components().addSecuritySchemes("bearerAuth", new SecurityScheme()
            .type(SecurityScheme.Type.HTTP)
            .scheme("bearer")
            .bearerFormat("JWT")))
        .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
  }
}

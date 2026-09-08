package training.common.openapi;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class OpenApiConfig {
  @Bean
  OpenAPI serviceOpenAPI(@Value("${spring.application.name:microservice}") String serviceName) {
    return new OpenAPI()
        .info(new Info()
            .title(serviceName + " API")
            .version("1.0.0")
            .description("Training API contract for the secured microservices example. Use the gateway auth endpoint to get a JWT, then authorize Swagger UI with Bearer token."))
        .components(new Components().addSecuritySchemes("bearerAuth", new SecurityScheme()
            .type(SecurityScheme.Type.HTTP)
            .scheme("bearer")
            .bearerFormat("JWT")))
        .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
  }
}

package training.product.api;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
@SpringBootTest(properties = {"spring.application.name=product-service", "security.jwt.secret=test-secret", "security.internal-token=test-internal-token", "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration,org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration", "management.endpoint.health.group.readiness.include=readinessState"})
@AutoConfigureMockMvc
class ProductApiDocumentationAndSecurityTest {
  @Autowired MockMvc mvc;
  @MockitoBean JdbcTemplate db;
  @MockitoBean KafkaTemplate<String, String> kafkaTemplate;

  @Test
  void openApiDocumentIsReadableWithoutTokenAndDeclaresBearerSecurity() throws Exception {
    mvc.perform(get("/v3/api-docs"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.info.title").value("product-service API"))
        .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.scheme").value("bearer"));
  }

  @Test
  void protectedProductEndpointRejectsMissingToken() throws Exception {
    mvc.perform(get("/products"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void authorizedCreateProductStillRunsRequestValidation() throws Exception {
    mvc.perform(post("/products")
            .header("X-Internal-Token", "test-internal-token")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"name\":\"\",\"price\":0}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.detail", containsString("Invalid request")));
  }
}

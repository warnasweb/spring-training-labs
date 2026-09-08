package training.gateway.security;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {
  private final String secret;

  public AuthController(@Value("${security.jwt.secret}") String secret) {
    this.secret = secret;
  }

  @Operation(summary = "Issue training JWT", description = "Returns a short-lived bearer token for local training. The sample security flow is intentionally simple so students can focus on gateway and service authorization behavior.")
  @ApiResponse(responseCode = "200", description = "Token issued")
  @PostMapping("/auth/token")
  Map<String, String> token(@RequestBody(required = false) LoginRequest request) {
    String user = request == null || request.username() == null || request.username().isBlank() ? "trainer" : request.username();
    String token = JwtSupport.issue(user, List.of("USER"), secret, 3600);
    return Map.of("tokenType", "Bearer", "accessToken", token);
  }

  record LoginRequest(String username, String password) {}
}

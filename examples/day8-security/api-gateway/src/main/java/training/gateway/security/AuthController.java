package training.gateway.security;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {
  private final String secret;
  public AuthController(@Value("${security.jwt.secret}") String secret) { this.secret = secret; }
  @PostMapping("/auth/token")
  Map<String, String> token(@RequestBody(required = false) LoginRequest request) {
    String user = request == null || request.username() == null || request.username().isBlank() ? "trainer" : request.username();
    String token = JwtSupport.issue(user, List.of("USER"), secret, 3600);
    return Map.of("tokenType", "Bearer", "accessToken", token);
  }
  record LoginRequest(String username, String password) {}
}

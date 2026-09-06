package training.gateway.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public final class JwtSupport {
  private static final ObjectMapper JSON = new ObjectMapper();
  private JwtSupport() {}
  public static String issue(String subject, List<String> roles, String secret, long ttlSeconds) {
    try {
      String header = b64(JSON.writeValueAsBytes(Map.of("alg", "HS256", "typ", "JWT")));
      Map<String,Object> body = new LinkedHashMap<>();
      body.put("sub", subject); body.put("roles", roles);
      body.put("iat", Instant.now().getEpochSecond()); body.put("exp", Instant.now().plusSeconds(ttlSeconds).getEpochSecond());
      String payload = b64(JSON.writeValueAsBytes(body));
      return header + "." + payload + "." + sign(header + "." + payload, secret);
    } catch (Exception e) { throw new IllegalStateException("Unable to issue token", e); }
  }
  public static Claims verify(String token, String secret) {
    try {
      String[] p = token.split("\\.");
      if (p.length != 3 || !MessageDigestSafe.equals(p[2], sign(p[0] + "." + p[1], secret))) throw new IllegalArgumentException("Bad signature");
      Map<?,?> body = JSON.readValue(Base64.getUrlDecoder().decode(p[1]), Map.class);
      Object expValue = body.get("exp");
      long exp = expValue instanceof Number n ? n.longValue() : 0L;
      if (Instant.now().getEpochSecond() >= exp) throw new IllegalArgumentException("Token expired");
      Object rolesValue = body.get("roles");
      List<String> roles = rolesValue instanceof List<?> list ? list.stream().map(Object::toString).toList() : List.of();
      return new Claims(String.valueOf(body.get("sub")), roles);
    } catch (Exception e) { throw new IllegalArgumentException("Invalid token", e); }
  }
  private static String sign(String data, String secret) throws Exception {
    Mac mac = Mac.getInstance("HmacSHA256");
    mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
    return b64(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
  }
  private static String b64(byte[] bytes) { return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes); }
  public record Claims(String subject, List<String> roles) {}
  static final class MessageDigestSafe {
    static boolean equals(String a, String b) { return java.security.MessageDigest.isEqual(a.getBytes(StandardCharsets.UTF_8), b.getBytes(StandardCharsets.UTF_8)); }
  }
}

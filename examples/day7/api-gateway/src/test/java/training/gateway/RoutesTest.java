package training.gateway;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteLocator;
import static org.junit.jupiter.api.Assertions.*;
import java.time.Duration;
@SpringBootTest
class RoutesTest {
 @Autowired RouteLocator routes;
 @Test void discoversAllFiveBusinessRoutes() {
  var configured=routes.getRoutes().collectList().block(Duration.ofSeconds(5));
  assertNotNull(configured);assertEquals(5,configured.size());
  assertTrue(configured.stream().anyMatch(r->r.getId().equals("orders")));
 }
}

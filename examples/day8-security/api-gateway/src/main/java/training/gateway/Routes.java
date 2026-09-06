package training.gateway;
import org.springframework.context.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.*;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import reactor.core.publisher.Mono;
import java.util.UUID;
@Configuration
public class Routes {
 @Bean RouteLocator shopRoutes(RouteLocatorBuilder b,
 @Value("${PRODUCT_URL:http://localhost:8081}") String product,
 @Value("${INVENTORY_URL:http://localhost:8082}") String inventory,
 @Value("${ORDER_URL:http://localhost:8083}") String order,
 @Value("${PAYMENT_URL:http://localhost:8084}") String payment,
 @Value("${NOTIFICATION_URL:http://localhost:8085}") String notification) {
  return b.routes().route("products",r->r.path("/products/**").uri(product))
   .route("inventory",r->r.path("/inventory/**").uri(inventory))
   .route("orders",r->r.path("/orders/**").uri(order))
   .route("payments",r->r.path("/payments/**").uri(payment))
   .route("notifications",r->r.path("/notifications/**").uri(notification)).build();
 }
 @Bean GlobalFilter correlation() { return (exchange,chain)-> {
  String supplied=exchange.getRequest().getHeaders().getFirst("X-Correlation-ID");
  String id=supplied!=null && supplied.matches("[A-Za-z0-9._-]{1,100}")?supplied:UUID.randomUUID().toString();
  exchange.getResponse().getHeaders().set("X-Correlation-ID",id);
  return chain.filter(exchange.mutate().request(exchange.getRequest().mutate().headers(h->h.set("X-Correlation-ID",id)).build()).build());
 }; }
}

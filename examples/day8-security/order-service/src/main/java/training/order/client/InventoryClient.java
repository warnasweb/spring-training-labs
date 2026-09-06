package training.order.client;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.*;
@Component
public class InventoryClient {
 private final RestClient http;
 private final String internalToken;
 private final java.util.concurrent.ExecutorService executor=java.util.concurrent.Executors.newFixedThreadPool(4);
 private final io.github.resilience4j.timelimiter.TimeLimiter limiter=io.github.resilience4j.timelimiter.TimeLimiter.of(
   io.github.resilience4j.timelimiter.TimeLimiterConfig.custom().timeoutDuration(Duration.ofMillis(2500)).cancelRunningFuture(true).build());
 @jakarta.annotation.PreDestroy void stop() { executor.shutdownNow(); }
 public InventoryClient(RestClient.Builder builder,@Value("${inventory.url:http://localhost:8082}") String url,@Value("${security.internal-token}") String internalToken) {
  var factory=new JdkClientHttpRequestFactory(HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(1)).build());
  factory.setReadTimeout(Duration.ofSeconds(2));this.internalToken=internalToken;http=builder.baseUrl(url).requestFactory(factory).build();
 }
 public record Result(boolean reserved) {}
 @Retry(name="inventory") @CircuitBreaker(name="inventory",fallbackMethod="unavailable")
 public boolean reserve(UUID order,UUID product,int quantity,String correlation) {
  try { return limiter.executeFutureSupplier(()->executor.submit(io.micrometer.context.ContextSnapshot.captureAll().wrap((java.util.concurrent.Callable<Boolean>) ()-> {
  var r=http.post().uri("/inventory/reservations").header("X-Correlation-ID",correlation).header("X-Internal-Token",internalToken)
    .body(Map.of("orderId",order,"productId",product,"quantity",quantity)).retrieve().body(Result.class);
  if(r==null) throw new IllegalStateException("Empty inventory response");return r.reserved();
  }))); } catch(Exception ex) { throw new InventoryUnavailable(ex); }
 }
 public boolean unavailable(UUID o,UUID p,int q,String c,Throwable cause) { throw new InventoryUnavailable(cause); }
 @Retry(name="inventory") @CircuitBreaker(name="inventory")
 public void release(UUID order,String correlation) { http.delete().uri("/inventory/reservations/{id}",order).header("X-Correlation-ID",correlation).header("X-Internal-Token",internalToken).retrieve().toBodilessEntity(); }
 public static class InventoryUnavailable extends RuntimeException { public InventoryUnavailable(Throwable cause) { super("Inventory unavailable; pending order will retry",cause); } }
}

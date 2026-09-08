package training.order.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {
  private final JdbcTemplate db;

  public OrderController(JdbcTemplate db) {
    this.db = db;
  }

  public record Create(@NotNull UUID productId, @Min(1) int quantity, boolean failPayment) {}

  @Operation(summary = "Create order", description = "Creates an order request using the Idempotency-Key header as the order id. The saga later reserves inventory, publishes events, processes payment, and either confirms or compensates the order.")
  @ApiResponses({
      @ApiResponse(responseCode = "202", description = "Order accepted for saga processing"),
      @ApiResponse(responseCode = "400", description = "Validation failed or Idempotency-Key is missing"),
      @ApiResponse(responseCode = "409", description = "Idempotency key reused for different request")
  })
  @PostMapping
  @ResponseStatus(HttpStatus.ACCEPTED)
  @Transactional
  public Map<String, Object> create(
      @Parameter(description = "Client generated UUID. Reusing the same key returns the same order when the body is identical.", required = true)
      @RequestHeader("Idempotency-Key") UUID key,
      @Valid @RequestBody Create req) {
    db.update("insert into orders(id,product_id,quantity,fail_payment,status,correlation_id) values (?,?,?,?,'PENDING',?) on conflict do nothing", key, req.productId(), req.quantity(), req.failPayment(), MDC.get("correlationId"));
    var order = one(key);
    if (!req.productId().equals(order.get("product_id")) || req.quantity() != ((Number) order.get("quantity")).intValue() || req.failPayment() != (Boolean) order.get("fail_payment")) {
      throw new IllegalStateException("Idempotency key reused for different request");
    }
    return order;
  }

  @Operation(summary = "Get order", description = "Returns the current order state so students can observe PENDING, AWAITING_PAYMENT, CONFIRMED, COMPENSATING, CANCELLED, or REJECTED during saga demos.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Order found"),
      @ApiResponse(responseCode = "404", description = "Order not found")
  })
  @GetMapping("/{id}")
  public Map<String, Object> one(@PathVariable UUID id) {
    return db.queryForList("select * from orders where id=?", id).stream().findFirst().orElseThrow(() -> new NoSuchElementException("Order not found"));
  }

  @Operation(summary = "Cancel order", description = "Starts compensation for an order that has not been confirmed. This is the visible saga compensation endpoint for training.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Order moved to compensation or already terminal"),
      @ApiResponse(responseCode = "409", description = "Confirmed order requires refund flow before cancellation")
  })
  @PostMapping("/{id}/cancel")
  @Transactional
  public Map<String, Object> cancel(@PathVariable UUID id) {
    var order = one(id);
    String status = (String) order.get("status");
    if (status.equals("CONFIRMED")) {
      throw new IllegalStateException("Confirmed orders need a refund flow before cancellation");
    }
    if (status.equals("CANCELLED") || status.equals("REJECTED")) {
      return order;
    }
    db.update("update orders set status='COMPENSATING',updated_at=now() where id=? and status in ('PENDING','AWAITING_PAYMENT','COMPENSATING')", id);
    return one(id);
  }
}

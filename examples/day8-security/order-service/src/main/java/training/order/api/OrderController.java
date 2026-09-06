package training.order.api;
import org.springframework.web.bind.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.slf4j.MDC;
import java.util.*;
@RestController @RequestMapping("/orders")
public class OrderController {
 private final JdbcTemplate db;
 public OrderController(JdbcTemplate db) { this.db=db; }
 public record Create(@NotNull UUID productId,@Min(1) int quantity,boolean failPayment) {}
 @PostMapping @ResponseStatus(org.springframework.http.HttpStatus.ACCEPTED) @Transactional
 public Map<String,Object> create(@RequestHeader("Idempotency-Key") UUID key,@Valid @RequestBody Create req) {
  db.update("insert into orders(id,product_id,quantity,fail_payment,status,correlation_id) values (?,?,?,?,'PENDING',?) on conflict do nothing",key,req.productId(),req.quantity(),req.failPayment(),MDC.get("correlationId"));
  var order=one(key);
  if(!req.productId().equals(order.get("product_id")) || req.quantity()!=((Number)order.get("quantity")).intValue() || req.failPayment()!=(Boolean)order.get("fail_payment")) throw new IllegalStateException("Idempotency key reused for different request");
  return order;
 }
 @GetMapping("/{id}") public Map<String,Object> one(@PathVariable UUID id) { return db.queryForList("select * from orders where id=?",id).stream().findFirst().orElseThrow(()->new NoSuchElementException("Order not found")); }

 @PostMapping("/{id}/cancel") @Transactional
 public Map<String,Object> cancel(@PathVariable UUID id) {
  var order=one(id);
  String status=(String)order.get("status");
  if(status.equals("CONFIRMED")) throw new IllegalStateException("Confirmed orders need a refund flow before cancellation");
  if(status.equals("CANCELLED") || status.equals("REJECTED")) return order;
  db.update("update orders set status='COMPENSATING',updated_at=now() where id=? and status in ('PENDING','AWAITING_PAYMENT','COMPENSATING')",id);
  return one(id);
 }
}

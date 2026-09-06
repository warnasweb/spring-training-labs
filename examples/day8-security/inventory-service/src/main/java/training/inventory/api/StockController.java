package training.inventory.api;
import training.inventory.service.StockService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
@RestController @RequestMapping("/inventory")
public class StockController {
 private final StockService service;
 public StockController(StockService service) { this.service=service; }
 public record Reservation(@NotNull UUID orderId,@NotNull UUID productId,@Min(1) int quantity) {}
 public record Result(boolean reserved) {}
 @GetMapping("/{product}") public Map<String,Object> stock(@PathVariable UUID product) { return service.stock(product); }
 @PostMapping("/reservations") public Result reserve(@Valid @RequestBody Reservation r) { return new Result(service.reserve(r.orderId(),r.productId(),r.quantity())); }
 @DeleteMapping("/reservations/{order}") public void release(@PathVariable UUID order) { service.release(order); }
}

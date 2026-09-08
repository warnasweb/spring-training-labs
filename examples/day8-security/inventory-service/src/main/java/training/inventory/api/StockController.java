package training.inventory.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import java.util.UUID;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import training.inventory.service.StockService;

@RestController
@RequestMapping("/inventory")
@SecurityRequirement(name = "bearerAuth")
public class StockController {
  private final StockService service;

  public StockController(StockService service) {
    this.service = service;
  }

  public record Reservation(@NotNull UUID orderId, @NotNull UUID productId, @Min(1) int quantity) {}
  public record Result(boolean reserved) {}

  @Operation(summary = "Query product stock", description = "Returns the stock row for one product. Use this before and after reservation or compensation to see state changes.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Stock returned"),
      @ApiResponse(responseCode = "404", description = "Stock row not found")
  })
  @GetMapping("/{product}")
  public Map<String, Object> stock(@PathVariable UUID product) {
    return service.stock(product);
  }

  @Operation(summary = "Reserve stock", description = "Attempts to reserve stock for an order. The operation is idempotent for the same order id and request body.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Reservation decision returned"),
      @ApiResponse(responseCode = "400", description = "Validation failed"),
      @ApiResponse(responseCode = "409", description = "Reservation key reused for a different request")
  })
  @PostMapping("/reservations")
  public Result reserve(@Valid @RequestBody Reservation r) {
    return new Result(service.reserve(r.orderId(), r.productId(), r.quantity()));
  }

  @Operation(summary = "Release stock reservation", description = "Compensates a failed order saga by returning previously reserved stock to availability.")
  @ApiResponse(responseCode = "200", description = "Reservation released or no-op if there was nothing to release")
  @DeleteMapping("/reservations/{order}")
  public void release(@PathVariable UUID order) {
    service.release(order);
  }
}

package training.product.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import training.product.domain.Product;

@RestController
@RequestMapping("/products")
@SecurityRequirement(name = "bearerAuth")
public class ProductController {
  private final JdbcTemplate db;

  public ProductController(JdbcTemplate db) {
    this.db = db;
  }

  @Operation(summary = "List products", description = "Returns the product catalog sorted by name. This is the first endpoint to try after authorizing Swagger UI with a JWT.")
  @ApiResponse(responseCode = "200", description = "Products returned")
  @GetMapping
  public List<Product> all() {
    return db.query("select * from products order by name", (rs, n) -> new Product(rs.getObject("id", UUID.class), rs.getString("name"), rs.getBigDecimal("price")));
  }

  @Operation(summary = "Get one product", description = "Returns one product by id. A missing product is translated to the common 404 error response.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Product found"),
      @ApiResponse(responseCode = "404", description = "Product not found")
  })
  @GetMapping("/{id}")
  public Product one(@PathVariable UUID id) {
    return db.query("select * from products where id=?", (rs, n) -> new Product(id, rs.getString("name"), rs.getBigDecimal("price")), id)
        .stream().findFirst().orElseThrow(() -> new NoSuchElementException("Product not found"));
  }

  @Operation(summary = "Create product", description = "Creates a catalog item. Validation rejects blank names and prices below 0.01 before the database insert runs.")
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "Product created"),
      @ApiResponse(responseCode = "400", description = "Validation failed"),
      @ApiResponse(responseCode = "401", description = "JWT is missing or invalid")
  })
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Product create(@Valid @RequestBody Product p) {
    UUID id = UUID.randomUUID();
    db.update("insert into products values (?,?,?)", id, p.name(), p.price());
    return one(id);
  }

  @Operation(summary = "Update product", description = "Replaces the editable product fields. The id is taken from the path so clients cannot change identity through the request body.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Product updated"),
      @ApiResponse(responseCode = "400", description = "Validation failed"),
      @ApiResponse(responseCode = "404", description = "Product not found")
  })
  @PutMapping("/{id}")
  public Product update(@PathVariable UUID id, @Valid @RequestBody Product p) {
    if (db.update("update products set name=?,price=? where id=?", p.name(), p.price(), id) == 0) {
      throw new NoSuchElementException("Product not found");
    }
    return one(id);
  }

  @Operation(summary = "Delete product", description = "Deletes a product from the catalog. In real systems this is often a soft delete when orders already reference the product.")
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "Product deleted"),
      @ApiResponse(responseCode = "404", description = "Product not found")
  })
  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable UUID id) {
    if (db.update("delete from products where id=?", id) == 0) {
      throw new NoSuchElementException("Product not found");
    }
  }
}

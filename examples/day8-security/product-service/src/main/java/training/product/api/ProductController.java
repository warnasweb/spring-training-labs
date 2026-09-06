package training.product.api;
import training.product.domain.Product;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.*;
@RestController @RequestMapping("/products")
public class ProductController {
 private final JdbcTemplate db;
 public ProductController(JdbcTemplate db) { this.db=db; }
 @GetMapping public List<Product> all() { return db.query("select * from products order by name",(rs,n)->new Product(rs.getObject("id",UUID.class),rs.getString("name"),rs.getBigDecimal("price"))); }
 @GetMapping("/{id}") public Product one(@PathVariable UUID id) { return db.query("select * from products where id=?",(rs,n)->new Product(id,rs.getString("name"),rs.getBigDecimal("price")),id).stream().findFirst().orElseThrow(()->new NoSuchElementException("Product not found")); }
 @PostMapping @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
 public Product create(@Valid @RequestBody Product p) { UUID id=UUID.randomUUID();db.update("insert into products values (?,?,?)",id,p.name(),p.price());return one(id); }
 @PutMapping("/{id}") public Product update(@PathVariable UUID id,@Valid @RequestBody Product p) { if(db.update("update products set name=?,price=? where id=?",p.name(),p.price(),id)==0) throw new NoSuchElementException("Product not found");return one(id); }
 @DeleteMapping("/{id}") @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
 public void delete(@PathVariable UUID id) { if(db.update("delete from products where id=?",id)==0) throw new NoSuchElementException("Product not found"); }
}

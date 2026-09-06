package training.notification.api;
import org.springframework.web.bind.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.*;
@RestController @RequestMapping("/notifications")
public class QueryController {
 private final JdbcTemplate db;
 public QueryController(JdbcTemplate db) { this.db=db; }
 @GetMapping("/{order}") public List<Map<String,Object>> get(@PathVariable UUID order) { return db.queryForList("select * from notifications where order_id=?",order); }
}

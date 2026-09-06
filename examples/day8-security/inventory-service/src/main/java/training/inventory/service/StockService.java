package training.inventory.service;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
@Service
public class StockService {
 private final JdbcTemplate db;
 public StockService(JdbcTemplate db) { this.db=db; }
 @Transactional public boolean reserve(UUID order,UUID product,int quantity) {
  lock(order);
  var prior=db.queryForList("select * from reservations where order_id=?",order);
  if(!prior.isEmpty()) {
   var p=prior.getFirst();
   if(!product.equals(p.get("product_id")) || quantity!=((Number)p.get("quantity")).intValue()) throw new IllegalStateException("Reservation key reused");
   return "RESERVED".equals(p.get("status"));
  }
  boolean ok=db.update("update stock set available=available-? where product_id=? and available>=?",quantity,product,quantity)==1;
  db.update("insert into reservations values (?,?,?,?)",order,product,quantity,ok?"RESERVED":"REJECTED");return ok;
 }
 @Transactional public void release(UUID order) {
  lock(order);
  var rows=db.queryForList("select * from reservations where order_id=? and status='RESERVED'",order);
  if(!rows.isEmpty()) { var p=rows.getFirst();db.update("update stock set available=available+? where product_id=?",p.get("quantity"),p.get("product_id"));db.update("update reservations set status='RELEASED' where order_id=?",order); }
 }
 private void lock(UUID order) { db.queryForList("select pg_advisory_xact_lock(hashtextextended(?,0))",order.toString()); }
 public Map<String,Object> stock(UUID product) { return db.queryForList("select * from stock where product_id=?",product).stream().findFirst().orElseThrow(()->new NoSuchElementException("Stock not found")); }
}

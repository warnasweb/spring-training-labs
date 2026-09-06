package training.inventory.service;
import org.junit.jupiter.api.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.flywaydb.core.Flyway;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import java.util.concurrent.*;
@Testcontainers(disabledWithoutDocker=true)
class StockIntegrationTest {
 @Container static PostgreSQLContainer<?> postgres=new PostgreSQLContainer<>("postgres:17.4");
 JdbcTemplate db;StockService service;TransactionTemplate tx;
 UUID product=UUID.fromString("11111111-1111-1111-1111-111111111111");
 @BeforeEach void setup() {
  var ds=new DriverManagerDataSource(postgres.getJdbcUrl(),postgres.getUsername(),postgres.getPassword());
  Flyway.configure().dataSource(ds).load().migrate();db=new JdbcTemplate(ds);service=new StockService(db);tx=new TransactionTemplate(new DataSourceTransactionManager(ds));
  db.update("delete from reservations");db.update("update stock set available=100");
 }
 boolean reserve(UUID order,int q) { return Boolean.TRUE.equals(tx.execute(s->service.reserve(order,product,q))); }
 @Test void repeatedReserveAndReleaseAreIdempotent() {
  UUID order=UUID.randomUUID();assertTrue(reserve(order,3));assertTrue(reserve(order,3));
  assertEquals(97,service.stock(product).get("available"));
  tx.executeWithoutResult(s->service.release(order));tx.executeWithoutResult(s->service.release(order));
  assertEquals(100,service.stock(product).get("available"));assertFalse(reserve(order,3));
 }
 @Test void insufficientStockIsStableDecision() {
  UUID order=UUID.randomUUID();assertFalse(reserve(order,101));assertFalse(reserve(order,101));assertEquals(100,service.stock(product).get("available"));
 }
 @Test void concurrentOrdersCannotOversell() throws Exception {
  try(var pool=Executors.newFixedThreadPool(8)) {
   var calls=new ArrayList<Callable<Boolean>>();for(int i=0;i<20;i++) calls.add(()->reserve(UUID.randomUUID(),10));
   int success=0;for(var f:pool.invokeAll(calls)) if(f.get()) success++;
   assertEquals(10,success);assertEquals(0,service.stock(product).get("available"));
  }
 }
}

package training.payment.messaging;
import training.common.events.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.*;
import org.springframework.transaction.support.TransactionTemplate;
import org.flywaydb.core.Flyway;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.UUID;
@Testcontainers(disabledWithoutDocker=true)
class PaymentTransactionIntegrationTest {
 @Container static PostgreSQLContainer<?> postgres=new PostgreSQLContainer<>("postgres:17.4");
 JdbcTemplate db;TransactionTemplate tx;OrderListener listener;ObjectMapper json=new ObjectMapper();
 @BeforeEach void setup() {
  var ds=new DriverManagerDataSource(postgres.getJdbcUrl(),postgres.getUsername(),postgres.getPassword());
  Flyway.configure().dataSource(ds).load().migrate();db=new JdbcTemplate(ds);
  tx=new TransactionTemplate(new DataSourceTransactionManager(ds));listener=new OrderListener(db,new EventStore(db,json),json);
  db.update("delete from payments");db.update("delete from inbox");db.update("delete from outbox");
 }
 void receive(Event e) {
  tx.executeWithoutResult(s->{ try { listener.receive(json.writeValueAsString(e)); } catch(Exception ex) { throw new RuntimeException(ex); } });
 }
 @Test void duplicatesCommitOnlyOnePaymentAndOneResult() {
  var e=new Event(UUID.randomUUID(),"OrderCreated",UUID.randomUUID(),UUID.randomUUID(),1,false,"integration");
  receive(e);receive(e);receive(e.next("OrderCreated"));
  assertEquals(1,db.queryForObject("select count(*) from payments",Integer.class));
  assertEquals(1,db.queryForObject("select count(*) from outbox",Integer.class));
 }
 @Test void rollbackAlsoRollsBackInboxSoDeliveryCanRetry() {
  var e=new Event(UUID.randomUUID(),"OrderCreated",UUID.randomUUID(),UUID.randomUUID(),1,true,"integration");
  assertThrows(RuntimeException.class,()->tx.executeWithoutResult(s->{ receive(e);throw new RuntimeException("crash before commit"); }));
  assertEquals(0,db.queryForObject("select count(*) from inbox",Integer.class));
  assertEquals(0,db.queryForObject("select count(*) from payments",Integer.class));
  assertEquals(0,db.queryForObject("select count(*) from outbox",Integer.class));
  receive(e);assertEquals("FAILED",db.queryForObject("select status from payments",String.class));
 }
}

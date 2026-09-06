package training.order.service;
import training.order.client.InventoryClient;
import training.common.events.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.slf4j.*;
import java.util.UUID;
@Service
public class SagaWorker {
 private static final Logger log=LoggerFactory.getLogger(SagaWorker.class);
 private final JdbcTemplate db; private final InventoryClient inventory; private final EventStore events;
 public SagaWorker(JdbcTemplate db,InventoryClient inventory,EventStore events) { this.db=db;this.inventory=inventory;this.events=events; }
 @Scheduled(initialDelayString="${saga.initial-delay:1000}",fixedDelayString="${saga.delay:1000}") @Transactional
 public void advance() {
  // Durable PENDING rows close the crash window around a remote reservation.
  // Repeating the same reservation after a timeout is safe because inventory owns its idempotency key.
  var rows=db.queryForList("select * from orders where status in ('PENDING','COMPENSATING') order by updated_at limit 10 for update skip locked");
  for(var o:rows) {
   UUID id=(UUID)o.get("id");String correlation=(String)o.get("correlation_id");
   try(var ignored=MDC.putCloseable("correlationId",correlation)) {
    Event e=new Event(UUID.randomUUID(),"OrderCreated",id,(UUID)o.get("product_id"),((Number)o.get("quantity")).intValue(),(Boolean)o.get("fail_payment"),correlation);
    if("PENDING".equals(o.get("status"))) {
     boolean reserved=inventory.reserve(id,e.productId(),e.quantity(),correlation);
     db.update("update orders set status=?,updated_at=now() where id=?",reserved?"AWAITING_PAYMENT":"REJECTED",id);
     events.append(reserved?e:e.next("OrderRejected"));
    } else {
     inventory.release(id,correlation);
     db.update("update orders set status='CANCELLED',updated_at=now() where id=?",id);
     events.append(e.next("OrderCancelled"));
    }
   } catch(org.springframework.web.client.RestClientException | InventoryClient.InventoryUnavailable | io.github.resilience4j.circuitbreaker.CallNotPermittedException ex) {
    log.warn("Saga {} waiting for inventory: {}",id,ex.toString());
    db.update("update orders set updated_at=now() where id=?",id);
   }
  }
 }
}

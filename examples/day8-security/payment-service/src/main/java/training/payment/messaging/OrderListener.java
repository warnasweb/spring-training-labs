package training.payment.messaging;
import training.common.events.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;
@Component
public class OrderListener {
 private final JdbcTemplate db;private final EventStore events;private final ObjectMapper json;
 public OrderListener(JdbcTemplate db,EventStore events,ObjectMapper json) { this.db=db;this.events=events;this.json=json; }
 @KafkaListener(topics="shop.events") @Transactional
 public void receive(String payload) throws Exception {
  Event e=json.readValue(payload,Event.class);
  if(!e.type().equals("OrderCreated") || !events.first(e)) return;
  // This is a deterministic simulator, not an external charge. A real provider needs its own idempotency key.
  String outcome=e.failPayment()?"FAILED":"SUCCEEDED";
  if(db.update("insert into payments(order_id,status) values (?,?) on conflict do nothing",e.orderId(),outcome)==1)
   events.append(e.next(e.failPayment()?"PaymentFailed":"PaymentSucceeded"));
 }
}

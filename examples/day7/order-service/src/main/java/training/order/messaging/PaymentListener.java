package training.order.messaging;
import training.common.events.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;
@Component
public class PaymentListener {
 private final JdbcTemplate db;private final EventStore events;private final ObjectMapper json;
 public PaymentListener(JdbcTemplate db,EventStore events,ObjectMapper json) { this.db=db;this.events=events;this.json=json; }
 @KafkaListener(topics="shop.events") @Transactional
 public void receive(String payload) throws Exception {
  Event e=json.readValue(payload,Event.class);
  if(!e.type().equals("PaymentSucceeded") && !e.type().equals("PaymentFailed")) return;
  if(!events.first(e)) return;
  String state=e.type().equals("PaymentSucceeded")?"CONFIRMED":"COMPENSATING";
  int changed=db.update("update orders set status=?,updated_at=now() where id=? and status='AWAITING_PAYMENT'",state,e.orderId());
  if(changed==1 && state.equals("CONFIRMED")) events.append(e.next("OrderConfirmed"));
 }
}

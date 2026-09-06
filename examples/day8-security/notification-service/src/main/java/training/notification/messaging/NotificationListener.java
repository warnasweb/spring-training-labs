package training.notification.messaging;
import training.common.events.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.slf4j.*;
@Component
public class NotificationListener {
 private static final Logger log=LoggerFactory.getLogger(NotificationListener.class);
 private final JdbcTemplate db;private final EventStore events;private final ObjectMapper json;
 public NotificationListener(JdbcTemplate db,EventStore events,ObjectMapper json) { this.db=db;this.events=events;this.json=json; }
 @KafkaListener(topics="shop.events") @Transactional
 public void receive(String payload) throws Exception {
  Event e=json.readValue(payload,Event.class);
  if(!java.util.Set.of("OrderCreated","OrderConfirmed","OrderCancelled","OrderRejected").contains(e.type()) || !events.first(e)) return;
  try(var ignored=MDC.putCloseable("correlationId",e.correlationId())) {
   int changed=db.update("insert into notifications(order_id,event_type,message) values (?,?,?) on conflict do nothing",e.orderId(),e.type(),"Order "+e.orderId()+": "+e.type());
   if(changed==1) log.info("Recorded notification {} for {}",e.type(),e.orderId());
  }
 }
}

package training.common.events;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.concurrent.TimeUnit;
@Component
public class OutboxPublisher {
 private final JdbcTemplate db; private final KafkaTemplate<String,String> kafka; private final ObjectMapper json;
 public OutboxPublisher(JdbcTemplate db,KafkaTemplate<String,String> kafka,ObjectMapper json) { this.db=db;this.kafka=kafka;this.json=json; }
 @Scheduled(initialDelayString="${outbox.initial-delay:1000}",fixedDelayString="${outbox.delay:1000}") @Transactional
 public void publish() throws Exception {
  // Lock rows across replicas. Crash after broker ACK but before commit => duplicate, never silent loss.
  var rows=db.queryForList("select id,payload from outbox where published=false order by created_at,id limit 20 for update skip locked");
  for(var row:rows) {
   String payload=(String)row.get("payload"); Event e=json.readValue(payload,Event.class);
   kafka.send("shop.events",e.orderId().toString(),payload).get(10,TimeUnit.SECONDS);
   db.update("update outbox set published=true where id=?",row.get("id"));
  }
 }
}

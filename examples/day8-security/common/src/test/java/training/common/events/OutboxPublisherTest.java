package training.common.events;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
class OutboxPublisherTest {
 @Test void failedBrokerAckDoesNotMarkPublished() throws Exception {
  var db=mock(JdbcTemplate.class);
  @SuppressWarnings("unchecked") KafkaTemplate<String,String> kafka=mock(KafkaTemplate.class);
  var json=new ObjectMapper();var e=new Event(UUID.randomUUID(),"OrderCreated",UUID.randomUUID(),UUID.randomUUID(),1,false,"test");
  String payload=json.writeValueAsString(e);
  when(db.queryForList(anyString())).thenReturn(List.of(Map.of("id",e.eventId(),"payload",payload)));
  when(kafka.send("shop.events",e.orderId().toString(),payload)).thenReturn(CompletableFuture.failedFuture(new RuntimeException("broker down")));
  assertThrows(Exception.class,()->new OutboxPublisher(db,kafka,json).publish());
  verify(db,never()).update(anyString(),any(UUID.class));
 }
}

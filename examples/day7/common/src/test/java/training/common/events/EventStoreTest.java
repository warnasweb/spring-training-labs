package training.common.events;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.UUID;
class EventStoreTest {
 @Test void publicationCarriesCorrelationAndStableEventIdentity() throws Exception {
  var db=mock(JdbcTemplate.class);var json=new ObjectMapper();var e=new Event(UUID.randomUUID(),"OrderCreated",UUID.randomUUID(),UUID.randomUUID(),1,false,"trace-me");
  new EventStore(db,json).append(e);
  verify(db).update(eq("insert into outbox(id,payload) values (?,?)"),eq(e.eventId()),eq(json.writeValueAsString(e)));
  assertEquals(e,json.readValue(json.writeValueAsString(e),Event.class));
 }
}

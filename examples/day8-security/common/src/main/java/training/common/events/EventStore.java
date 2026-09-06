package training.common.events;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
@Component
public class EventStore {
 private final JdbcTemplate db; private final ObjectMapper json;
 public EventStore(JdbcTemplate db,ObjectMapper json) { this.db=db;this.json=json; }
 // Invoke inside the same local transaction as the business write.
 public void append(Event e) {
  try { db.update("insert into outbox(id,payload) values (?,?)",e.eventId(),json.writeValueAsString(e)); }
  catch(com.fasterxml.jackson.core.JsonProcessingException ex) { throw new IllegalStateException(ex); }
 }
 public boolean first(Event e) { return db.update("insert into inbox(id) values (?) on conflict do nothing",e.eventId())==1; }
}

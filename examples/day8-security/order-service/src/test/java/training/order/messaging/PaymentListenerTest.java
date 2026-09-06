package training.order.messaging;
import training.common.events.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import static org.mockito.Mockito.*;
import java.util.UUID;
class PaymentListenerTest {
 @Test void failedPaymentSchedulesDurableCompensation() throws Exception {
  var db=mock(JdbcTemplate.class);var events=mock(EventStore.class);var json=new ObjectMapper();
  var e=new Event(UUID.randomUUID(),"PaymentFailed",UUID.randomUUID(),UUID.randomUUID(),1,true,"test");
  when(events.first(e)).thenReturn(true);new PaymentListener(db,events,json).receive(json.writeValueAsString(e));
  verify(db).update("update orders set status=?,updated_at=now() where id=? and status='AWAITING_PAYMENT'","COMPENSATING",e.orderId());
  verify(events,never()).append(any());
 }
}

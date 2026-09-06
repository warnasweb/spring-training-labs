package training.payment.messaging;
import training.common.events.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.UUID;
class OrderListenerTest {
 @Test void successAndFailureProduceCorrespondingEvent() throws Exception {
  for(boolean fail:new boolean[]{false,true}) {
   var db=mock(JdbcTemplate.class);var events=mock(EventStore.class);var json=new ObjectMapper();
   Event e=new Event(UUID.randomUUID(),"OrderCreated",UUID.randomUUID(),UUID.randomUUID(),2,fail,"test");
   when(events.first(e)).thenReturn(true);
   when(db.update(anyString(),eq(e.orderId()),eq(fail?"FAILED":"SUCCEEDED"))).thenReturn(1);
   new OrderListener(db,events,json).receive(json.writeValueAsString(e));
   verify(events).append(argThat(next->next.type().equals(fail?"PaymentFailed":"PaymentSucceeded") && next.orderId().equals(e.orderId())));
  }
 }
 @Test void duplicateDoesNotChargeOrPublish() throws Exception {
  var db=mock(JdbcTemplate.class);var events=mock(EventStore.class);var json=new ObjectMapper();
  var e=new Event(UUID.randomUUID(),"OrderCreated",UUID.randomUUID(),UUID.randomUUID(),1,false,"test");
  new OrderListener(db,events,json).receive(json.writeValueAsString(e));
  verifyNoInteractions(db);verify(events,never()).append(any());
 }
}

package training.order.service;
import training.order.client.InventoryClient;
import training.common.events.*;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import static org.mockito.Mockito.*;
import java.util.*;
class SagaWorkerTest {
 final UUID id=UUID.randomUUID(),product=UUID.randomUUID();
 final JdbcTemplate db=mock(JdbcTemplate.class);final InventoryClient inventory=mock(InventoryClient.class);final EventStore events=mock(EventStore.class);
 void row(String state) { when(db.queryForList(anyString())).thenReturn(List.of(Map.of("id",id,"product_id",product,"quantity",2,"fail_payment",false,"status",state,"correlation_id","test"))); }
 @Test void unavailableLeavesOrderPendingWithoutEvent() {
  row("PENDING");when(inventory.reserve(id,product,2,"test")).thenThrow(new InventoryClient.InventoryUnavailable(new RuntimeException()));
  new SagaWorker(db,inventory,events).advance();verifyNoInteractions(events);
  verify(db).update("update orders set updated_at=now() where id=?",id);
 }
 @Test void successWritesOrderCreated() {
  row("PENDING");when(inventory.reserve(id,product,2,"test")).thenReturn(true);
  new SagaWorker(db,inventory,events).advance();verify(events).append(argThat(e->e.type().equals("OrderCreated")));
 }
 @Test void stockRejectionDoesNotStartPayment() {
  row("PENDING");new SagaWorker(db,inventory,events).advance();verify(events).append(argThat(e->e.type().equals("OrderRejected")));
 }
 @Test void compensationReleasesBeforeCancellation() {
  row("COMPENSATING");new SagaWorker(db,inventory,events).advance();var sequence=inOrder(inventory,db,events);
  sequence.verify(inventory).release(id,"test");sequence.verify(db).update("update orders set status='CANCELLED',updated_at=now() where id=?",id);
  sequence.verify(events).append(argThat(e->e.type().equals("OrderCancelled")));
 }
}

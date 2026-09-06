package training.common.events;
import java.util.UUID;
public record Event(UUID eventId, String type, UUID orderId, UUID productId, int quantity,
                    boolean failPayment, String correlationId) {
 public Event next(String type) { return new Event(UUID.randomUUID(),type,orderId,productId,quantity,failPayment,correlationId); }
}

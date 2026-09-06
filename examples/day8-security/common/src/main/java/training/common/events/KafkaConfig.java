package training.common.events;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;
@Configuration
public class KafkaConfig {
 @Bean NewTopic events() { return TopicBuilder.name("shop.events").partitions(3).replicas(1).build(); }
 // Keep retrying rather than silently skipping failed business transactions in this lab.
 @Bean DefaultErrorHandler errorHandler() { return new DefaultErrorHandler(new FixedBackOff(2000,FixedBackOff.UNLIMITED_ATTEMPTS)); }
}

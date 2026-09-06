package training.product.api;
import training.product.domain.Product;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
class ProductValidationTest {
 @Test void rejectsBlankNameAndNonPositivePrice() {
  try(var factory=Validation.buildDefaultValidatorFactory()) {
   assertEquals(2,factory.getValidator().validate(new Product(null,"",BigDecimal.ZERO)).size());
  }
 }
}

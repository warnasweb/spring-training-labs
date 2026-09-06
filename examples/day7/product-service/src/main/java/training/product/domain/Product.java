package training.product.domain;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.UUID;
public record Product(UUID id,@NotBlank @Size(max=200) String name,@NotNull @DecimalMin("0.01") BigDecimal price) {}

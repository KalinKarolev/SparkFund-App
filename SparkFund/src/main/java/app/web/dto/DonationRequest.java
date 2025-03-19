package app.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DonationRequest {

    @NotNull
    @DecimalMin(value = "1", message = "Amount must be at least 1")
    private BigDecimal amount;

    @Size(max = 300, message = "Your message cannot exceed 300 symbols")
    private String message;
}

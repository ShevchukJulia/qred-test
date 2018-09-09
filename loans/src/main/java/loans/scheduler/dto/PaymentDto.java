package loans.scheduler.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PaymentDto {

    private Long id;

    private LocalDate termDate;

    private BigDecimal principal;

    private BigDecimal commission;

}

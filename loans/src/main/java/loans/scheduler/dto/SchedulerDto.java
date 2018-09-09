package loans.scheduler.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class SchedulerDto {

    private Long id;

    private BigDecimal totalPrinciple;

    private BigDecimal totalCommission;

    private String currency;

    private List<PaymentDto> payments;

}

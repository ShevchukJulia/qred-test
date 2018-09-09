package scheduler.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class LoanDto {

    @NotNull
    private Long id;

    @NotNull
    private BigDecimal amount;

    @NotNull
    private Integer term;

    @NotNull
    private LocalDate confirmDate;

    @NotNull
    private Double interestRate;

    @NotNull
    private String currency;

}

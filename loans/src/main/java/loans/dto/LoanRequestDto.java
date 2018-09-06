package loans.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class LoanRequestDto {

    private Long loanId;
    private BigDecimal amount;
    private Integer term;
    private LocalDate confirmDate;
    private Double interestRate;
    private String currency;

}

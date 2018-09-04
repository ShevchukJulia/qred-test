package loans.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ScheduleItem {

    private Integer number;

    private LocalDate termDate;

    private BigDecimal principal;

    private BigDecimal commission;

    private String currency;

}

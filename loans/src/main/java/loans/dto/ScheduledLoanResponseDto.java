package loans.dto;

import loans.scheduler.dto.SchedulerDto;
import loans.persistance.model.Loan;
import lombok.Data;

@Data
public class ScheduledLoanResponseDto {

    private Loan loan;

    private SchedulerDto scheduler;

}

package loans.dto;

import loans.persistance.model.Loan;
import lombok.Data;

import java.util.List;

@Data
public class ScheduledLoan {

    private Loan loan;

    private List<ScheduleItem> schedulers;

}

package scheduler.service;

import scheduler.dto.LoanDto;
import scheduler.persistance.model.Scheduler;

public interface SchedulerService {

    Scheduler create(LoanDto loanDto);

    Scheduler findById(Long id);

    Scheduler findByLoanId(Long id);

}

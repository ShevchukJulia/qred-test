package scheduler.service;

import scheduler.dto.LoanDto;
import scheduler.persistance.model.Scheduler;

public interface SchedulerService {

    Scheduler save(LoanDto loanDto);

    Scheduler findByLoanId(Long id);

}

package loans.scheduler.client;


import loans.dto.LoanRequestDto;
import loans.scheduler.dto.SchedulerDto;

public interface SchedulerRestClient {

    SchedulerDto createScheduler(LoanRequestDto loan) ;

    SchedulerDto findSchedulerByLoanId(Long loanId);

}

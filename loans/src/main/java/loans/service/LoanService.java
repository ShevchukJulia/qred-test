package loans.service;

import loans.scheduler.dto.SchedulerDto;
import loans.dto.ScheduledLoanResponseDto;
import loans.persistance.model.Loan;

import java.util.List;

public interface LoanService {

    Loan create(Loan loan);

    Loan getById(Long id);

    List<Loan> findAll();

    Loan rejectLoan(Long id);

    Loan validateLoan(Long id, Double interestRate);

    ScheduledLoanResponseDto confirmLoan(Long id);

    SchedulerDto findSchedulerByLoanId(Long id);

}

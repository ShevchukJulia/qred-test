package loans.service;

import loans.dto.ScheduledLoan;
import loans.persistance.model.Loan;

import java.math.BigDecimal;
import java.util.List;

public interface LoanService {

    Loan create(Loan loan);

    Loan getById(Long id);

    List<Loan> findAll();

    Loan rejectLoan(Long id);

    Loan validateLoan(Long id, BigDecimal interestRate);

    ScheduledLoan confirmLoan(Long id);

}

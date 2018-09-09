package loans.persistance.repository;


import loans.persistance.model.Loan;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LoanRepository extends CrudRepository<Loan, Long> {

    List<Loan> findByCreationTimeBetweenAndCompanyId(LocalDateTime from, LocalDateTime to, Long companyId);

}

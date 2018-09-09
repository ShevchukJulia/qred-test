package loans.persistance.repository;

import loans.persistance.model.Company;
import loans.persistance.model.CompanyStatus;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompanyRepository extends CrudRepository<Company, Long> {

    Optional<Company> findByIdAndStatus(Long id, CompanyStatus status);
}

package loans.persistance.repository;

import loans.persistance.model.BlockedCompany;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BlockedCompanyRepository extends CrudRepository<BlockedCompany, Integer> {

}

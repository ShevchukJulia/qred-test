package scheduler.persistance.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import scheduler.persistance.model.Scheduler;

import java.util.Optional;

@Repository
public interface SchedulerRepository extends CrudRepository<Scheduler, Long> {

    Optional<Scheduler> findByLoanId(Long id);
}

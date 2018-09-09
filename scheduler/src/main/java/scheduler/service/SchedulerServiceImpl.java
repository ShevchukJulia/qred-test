package scheduler.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import scheduler.dto.LoanDto;
import scheduler.exception.ItemNotFoundException;
import scheduler.persistance.model.Payment;
import scheduler.persistance.model.Scheduler;
import scheduler.persistance.repository.SchedulerRepository;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.math.BigDecimal.ROUND_HALF_EVEN;

@Slf4j
@Service
public class SchedulerServiceImpl implements SchedulerService {
    private static final Integer ROUND_SCALE = 2;
    private static final Integer PERCENT = 100;

    private SchedulerRepository schedulerRepository;

    @Autowired
    public SchedulerServiceImpl(SchedulerRepository schedulerRepository) {
        this.schedulerRepository = schedulerRepository;
    }

    @Override
    public Scheduler save(LoanDto loanDto) {
        Optional<Scheduler> scheduler = schedulerRepository.findByLoanId(loanDto.getId());

        if (scheduler.isPresent()) {
            Scheduler updatedScheduler = schedulerRepository.save(updateScheduler(scheduler.get(), loanDto));
            log.info("Scheduler for loan with id {} was updated", loanDto.getId());
            return updatedScheduler;
        }

        return schedulerRepository.save(createScheduler(loanDto));
    }

    @Override
    public Scheduler findByLoanId(Long id) {
        Optional<Scheduler> scheduler = schedulerRepository.findByLoanId(id);
        if (!scheduler.isPresent()) {
            String message = MessageFormat.format("Scheduler with loan id {0} does not exist", id);
            log.error(message);
            throw new ItemNotFoundException(message);
        }
        return scheduler.get();
    }

    private Scheduler createScheduler(LoanDto loanDto) {
        Scheduler scheduler = new Scheduler();
        scheduler.setTotalPrinciple(loanDto.getAmount());
        scheduler.setCurrency(loanDto.getCurrency());
        scheduler.setLoanId(loanDto.getId());
        scheduler.setTotalCommission(calculateTotalCommission(loanDto));

        List<Payment> payments = createPayments(loanDto, scheduler);
        scheduler.setPayments(payments);
        return scheduler;
    }

    private Scheduler updateScheduler(Scheduler scheduler, LoanDto loanDto) {
        scheduler.setTotalCommission(calculateTotalCommission(loanDto));

        updatePayments(loanDto, scheduler);
        return scheduler;
    }

    private List<Payment> createPayments(LoanDto loanDto, Scheduler scheduler) {
        BigDecimal commission = calculateCommission(loanDto);
        BigDecimal principal = calculatePrincipal(loanDto);
        List<Payment> payments = new ArrayList<>();

        for (int i = 0; i < loanDto.getTerm(); i++) {
            Payment payment = new Payment();
            payment.setPrincipal(principal);
            payment.setCommission(commission);

            LocalDate termDate = loanDto.getConfirmDate().plusMonths(i);
            payment.setTermDate(termDate);
            payment.setScheduler(scheduler);

            payments.add(payment);
        }
        return payments;
    }

    private void updatePayments(LoanDto loanDto, Scheduler scheduler) {
        BigDecimal commission = calculateCommission(loanDto);
        scheduler.getPayments().forEach(item -> item.setCommission(commission));
    }

    private BigDecimal calculatePrincipal(LoanDto loanDto) {
        return loanDto.getAmount()
                .divide(BigDecimal.valueOf(loanDto.getTerm()), ROUND_SCALE, ROUND_HALF_EVEN);
    }

    private BigDecimal calculateCommission(LoanDto loanDto)  {
        return loanDto.getAmount()
                .multiply(BigDecimal.valueOf(loanDto.getInterestRate()))
                .divide(BigDecimal.valueOf(PERCENT), ROUND_SCALE, ROUND_HALF_EVEN);
    }

    private BigDecimal calculateTotalCommission(LoanDto loanDto) {
        return calculateCommission(loanDto).multiply(BigDecimal.valueOf(loanDto.getTerm()));
    }

}

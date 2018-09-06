package scheduler.service;

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

import static java.math.BigDecimal.ROUND_HALF_EVEN;

@Service
public class SchedulerServiceImpl implements SchedulerService {
    private static final Integer ROUND_SCALE = 2;
    private static final Integer PERCENT = 100;

    private SchedulerRepository schedulerRepository;

    public SchedulerServiceImpl(SchedulerRepository schedulerRepository) {
        this.schedulerRepository = schedulerRepository;
    }

    @Override
    public Scheduler create(LoanDto loanDto) {
        Scheduler scheduler = new Scheduler();
        scheduler.setTotalPrinciple(loanDto.getAmount());
        scheduler.setCurrency(loanDto.getCurrency());
        scheduler.setLoanId(loanDto.getLoanId());

        List<Payment> payments = createPayments(loanDto, scheduler);
        scheduler.setPayments(payments);

        BigDecimal totalCommission = calculateCommission(loanDto).multiply(BigDecimal.valueOf(loanDto.getTerm()));
        scheduler.setTotalCommission(totalCommission);

        return schedulerRepository.save(scheduler);
    }

    @Override
    public Scheduler findById(Long id) {
        return schedulerRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException(
                        MessageFormat.format("Scheduler with id {0} does not exist", id)));
    }

    @Override
    public Scheduler findByLoanId(Long id) {
        return schedulerRepository.findByLoanId(id)
                .orElseThrow(() -> new ItemNotFoundException(
                        MessageFormat.format("Scheduler with loan id {0} does not exist", id)));
    }

    private List<Payment> createPayments(LoanDto loanDto, Scheduler scheduler) {
        BigDecimal commission = calculateCommission(loanDto);
        BigDecimal principal = loanDto.getAmount()
                .divide(BigDecimal.valueOf(loanDto.getTerm()), ROUND_SCALE, ROUND_HALF_EVEN);
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

    private BigDecimal calculateCommission(LoanDto loanDto)  {
        return loanDto.getAmount()
                .multiply(BigDecimal.valueOf(loanDto.getInterestRate()))
                .divide(BigDecimal.valueOf(PERCENT), ROUND_SCALE, ROUND_HALF_EVEN);
    }

}

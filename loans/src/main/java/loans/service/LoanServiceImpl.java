package loans.service;

import loans.client.SchedulerRestClient;
import loans.dto.ScheduleItem;
import loans.dto.ScheduledLoan;
import loans.persistance.model.BlockedCompany;
import loans.persistance.model.Loan;
import loans.persistance.model.LoanStatus;
import loans.persistance.repository.BlockedCompanyRepository;
import loans.persistance.repository.LoanRepository;
import loans.web.exceptions.ItemNotFoundException;
import loans.web.exceptions.LoanValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class LoanServiceImpl implements LoanService {

    private static final BigDecimal LIMIT_PERCENT = BigDecimal.valueOf(0.03);
    private static final Integer DEFAULT_TERM = 6;
    private static final Integer MIN_TERM = 1;
    private static final Integer MAX_TERM = 12;
    private static final Integer BILLING_PERIOD = 12;
    private static final Integer ROUND_SCALE = 7;

    private LoanRepository loanRepository;

    private BlockedCompanyRepository blockedCompanyRepository;

    private SchedulerRestClient schedulerRestClient;

    @Autowired
    public LoanServiceImpl(LoanRepository loanRepository, BlockedCompanyRepository blockedCompanyRepository,
                           SchedulerRestClient schedulerRestClient) {
        this.loanRepository = loanRepository;
        this.blockedCompanyRepository = blockedCompanyRepository;
        this.schedulerRestClient = schedulerRestClient;
    }

    @Override
    public Loan create(Loan loan) {
        if (!isValidTerm(loan)) {
            throw new LoanValidationException("Application is invalid");
        }

        if (shouldBeBlocked(loan)) {
            saveCompanyAsBlocked(new BlockedCompany(loan.getCompanyId(), loan.getCompanyName()));
            throw new LoanValidationException(
                    MessageFormat.format("Company with Id {0} is blocked", loan.getCompanyId()));
        }

        loan.setStatus(LoanStatus.NEW);

        return loanRepository.save(loan);
    }

    @Override
    public Loan getById(Long id) {
        return loanRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException(
                        MessageFormat.format("Loan with loanId {0} not found", id)));
    }

    @Override
    public List<Loan> findAll() {
        return (List<Loan>) loanRepository.findAll();
    }

    @Override
    public Loan rejectLoan(Long id) {
        Loan loanToUpdate = loanRepository.findById(id).
                orElseThrow(() -> new ItemNotFoundException(
                        MessageFormat.format("Loan with loanId {0} not found", id)));
        loanToUpdate.setStatus(LoanStatus.REJECTED);
        return loanRepository.save(loanToUpdate);
    }

    @Override
    public Loan validateLoan(Long id, BigDecimal interestRate) {
        Loan loanToValidate = loanRepository.findById(id).
                orElseThrow(() -> new ItemNotFoundException(
                        MessageFormat.format("Loan with loanId {0} not found", id)));

        if (isBlackListed(loanToValidate) || !isSolvent(loanToValidate, interestRate)) {
            loanToValidate.setStatus(LoanStatus.INVALID);
        } else {
            loanToValidate.setStatus(LoanStatus.VALIDATED);
        }

        return loanRepository.save(loanToValidate);
    }

    @Override
    public ScheduledLoan confirmLoan(Long id) {
        Loan loanToConfirm = loanRepository.findById(id).
                orElseThrow(() -> new ItemNotFoundException(
                        MessageFormat.format("Loan with id {0} not found", id)));
        if (loanToConfirm.getStatus() == LoanStatus.INVALID) {
            throw new LoanValidationException(
                    MessageFormat.format("Loan with id {0} is invalid", id));
        }

        loanToConfirm.setStatus(LoanStatus.CONFIRMED);
        loanRepository.save(loanToConfirm);

        ScheduledLoan scheduledLoan = new ScheduledLoan();
        scheduledLoan.setLoan(loanToConfirm);

        List<ScheduleItem> scheduler = schedulerRestClient.createScheduler(loanToConfirm);
        scheduledLoan.setSchedulers(scheduler);

        return scheduledLoan;
    }

    private boolean isValidTerm(Loan loan) {
        if (loan.getTerm() == null) {
            loan.setTerm(DEFAULT_TERM);
        }

        return loan.getTerm() >= MIN_TERM && loan.getTerm() <= MAX_TERM;
    }

    private boolean shouldBeBlocked(Loan loan) {
        LocalDateTime to = LocalDateTime.now();
        List<Loan> loansDuringOneMinute = loanRepository
                .findByCreationTimeBetweenAndCompanyId(to.minusMinutes(1), to, loan.getCompanyId());

        return loansDuringOneMinute.size() >= 2;
    }

    private void saveCompanyAsBlocked(BlockedCompany company) {
        blockedCompanyRepository.save(company);
        throw new LoanValidationException(
                MessageFormat.format("Company with loanId {0} is blocked", company.getCompanyId()));
    }

    private boolean isBlackListed(Loan loan) {
        return blockedCompanyRepository.findById(loan.getCompanyId()).isPresent();
    }

    private boolean isSolvent(Loan loan, BigDecimal interestRate) {
        if (loan.getTurnover() == null) {
            return true;
        }

        BigDecimal monthlyLimit = loan.getTurnover()
                .divide(BigDecimal.valueOf(BILLING_PERIOD), ROUND_SCALE, BigDecimal.ROUND_HALF_EVEN)
                .multiply(LIMIT_PERCENT);

        BigDecimal monthlyExpenses = loan.getAmount().multiply(interestRate);

        return monthlyExpenses.compareTo(monthlyLimit) > 0;
    }

}

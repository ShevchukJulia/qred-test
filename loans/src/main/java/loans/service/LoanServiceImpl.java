package loans.service;

import loans.persistance.model.Currency;
import loans.scheduler.dto.SchedulerDto;
import loans.scheduler.client.SchedulerRestClientImpl;
import loans.dto.LoanRequestDto;
import loans.dto.ScheduledLoanResponseDto;
import loans.persistance.model.BlockedCompany;
import loans.persistance.model.Loan;
import loans.persistance.model.LoanStatus;
import loans.persistance.repository.BlockedCompanyRepository;
import loans.persistance.repository.LoanRepository;
import loans.web.exception.InvalidDataException;
import loans.web.exception.ItemNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.time.LocalDate;
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
    private static final Integer ALLOWABLE_APPLICATION_COUNT = 2;
    private static final Integer PERCENT = 100;

    private LoanRepository loanRepository;

    private BlockedCompanyRepository blockedCompanyRepository;

    private SchedulerRestClientImpl schedulerRestClient;

    @Autowired
    public LoanServiceImpl(LoanRepository loanRepository, BlockedCompanyRepository blockedCompanyRepository,
                           SchedulerRestClientImpl schedulerRestClient) {
        this.loanRepository = loanRepository;
        this.blockedCompanyRepository = blockedCompanyRepository;
        this.schedulerRestClient = schedulerRestClient;;
    }

    @Override
    public Loan create(Loan loan) {
        if (!isValidLoan(loan)) {
            throw new InvalidDataException("Application is invalid");
        }

        if (shouldBeBlocked(loan)) {
            saveCompanyAsBlocked(new BlockedCompany(loan.getCompanyId(), loan.getCompanyName()));
            throw new InvalidDataException(
                    MessageFormat.format("Company with id {0} is blocked", loan.getCompanyId()));
        }

        loan.setStatus(LoanStatus.NEW);

        return loanRepository.save(loan);
    }

    @Override
    public Loan getById(Long id) {
        return loanRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException(
                        MessageFormat.format("Loan with id {0} not found", id)));
    }

    @Override
    public List<Loan> findAll() {
        return (List<Loan>) loanRepository.findAll();
    }

    @Override
    public Loan rejectLoan(Long id) {
        Loan loanToUpdate = loanRepository.findById(id).
                orElseThrow(() -> new ItemNotFoundException(
                        MessageFormat.format("Loan with id {0} not found", id)));
        loanToUpdate.setStatus(LoanStatus.REJECTED);
        return loanRepository.save(loanToUpdate);
    }

    @Override
    public Loan validateLoan(Long id, Double interestRate) {
        Loan loanToValidate = loanRepository.findById(id).
                orElseThrow(() -> new ItemNotFoundException(
                        MessageFormat.format("Loan with id {0} not found", id)));

        if (isBlackListed(loanToValidate) || !isSolvent(loanToValidate, interestRate)) {
            loanToValidate.setStatus(LoanStatus.INVALID);
        } else {
            loanToValidate.setStatus(LoanStatus.VALID);
        }

        loanToValidate.setInterestRate(interestRate);

        return loanRepository.save(loanToValidate);
    }

    @Override
    public ScheduledLoanResponseDto confirmLoan(Long id) {
        Loan loanToConfirm = loanRepository.findById(id).
                orElseThrow(() -> new ItemNotFoundException(
                        MessageFormat.format("Loan with id {0} not found", id)));
        if (loanToConfirm.getStatus() != LoanStatus.VALID) {
            throw new InvalidDataException(
                    MessageFormat.format("Loan with id {0} wasn't validated", id));
        }

        ScheduledLoanResponseDto scheduledLoanDto = new ScheduledLoanResponseDto();

        loanToConfirm.setStatus(LoanStatus.CONFIRMED);
        loanToConfirm.setConfirmationDate(LocalDate.now());

        scheduledLoanDto.setLoan(loanToConfirm);

        LoanRequestDto loanRequestDto = mapToLoanRequestDto(loanToConfirm);
        SchedulerDto scheduler = schedulerRestClient.createScheduler(loanRequestDto);

        loanToConfirm.setSchedulerId(scheduler.getSchedulerId());
        loanRepository.save(loanToConfirm);

        scheduledLoanDto.setScheduler(scheduler);

        return scheduledLoanDto;
    }

    @Override
    public SchedulerDto findScheduler(Long id) {
        Loan loanToConfirm = loanRepository.findById(id).
                orElseThrow(() -> new ItemNotFoundException(
                        MessageFormat.format("Loan with id {0} not found", id)));
        if (loanToConfirm.getStatus() != LoanStatus.VALID) {
            throw new InvalidDataException(
                    MessageFormat.format("Loan with id {0} is invalid", id));
        }

        return schedulerRestClient.findSchedulerByLoanId(id);
    }

    private boolean isValidLoan(Loan loan) {
        if (loan.getCurrency() == null) {
            loan.setCurrency(Currency.EUR);
        }

        if (loan.getTerm() == null) {
            loan.setTerm(DEFAULT_TERM);
        }

        return loan.getTerm() >= MIN_TERM && loan.getTerm() <= MAX_TERM;
    }

    private boolean shouldBeBlocked(Loan loan) {
        LocalDateTime to = LocalDateTime.now();
        List<Loan> loansDuringOneMinute = loanRepository
                .findByCreationTimeBetweenAndCompanyId(to.minusMinutes(1), to, loan.getCompanyId());

        return loansDuringOneMinute.size() >= ALLOWABLE_APPLICATION_COUNT;
    }

    private void saveCompanyAsBlocked(BlockedCompany company) {
        blockedCompanyRepository.save(company);
        throw new InvalidDataException(
                MessageFormat.format("Company with id {0} is blocked", company.getCompanyId()));
    }

    private boolean isBlackListed(Loan loan) {
        return blockedCompanyRepository.findById(loan.getCompanyId()).isPresent();
    }

    private boolean isSolvent(Loan loan, Double interestRate) {
        if (loan.getTurnover() == null) {
            return true;
        }

        BigDecimal monthlyLimit = loan.getTurnover()
                .divide(BigDecimal.valueOf(BILLING_PERIOD), ROUND_SCALE, BigDecimal.ROUND_HALF_EVEN)
                .multiply(LIMIT_PERCENT);

        BigDecimal commission = loan.getAmount()
                .multiply(BigDecimal.valueOf(interestRate))
                .divide(BigDecimal.valueOf(PERCENT), ROUND_SCALE, BigDecimal.ROUND_HALF_EVEN);
        BigDecimal principal = loan.getAmount()
                .divide(BigDecimal.valueOf(loan.getTerm()), ROUND_SCALE, BigDecimal.ROUND_HALF_EVEN);
        BigDecimal monthlyExpensesForLon = commission.add(principal);

        return monthlyExpensesForLon.compareTo(monthlyLimit) > 0;
    }

    private LoanRequestDto mapToLoanRequestDto(Loan loan) {
        LoanRequestDto loanRequestDto = new LoanRequestDto();

        loanRequestDto.setLoanId(loan.getLoanId());
        loanRequestDto.setAmount(loan.getAmount());
        loanRequestDto.setInterestRate(loan.getInterestRate());
        loanRequestDto.setTerm(loan.getTerm());
        loanRequestDto.setConfirmDate(loan.getConfirmationDate());
        loanRequestDto.setCurrency(loan.getCurrency().name());

        return loanRequestDto;
    }

}

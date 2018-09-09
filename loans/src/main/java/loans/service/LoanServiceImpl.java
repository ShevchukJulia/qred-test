package loans.service;

import loans.dto.ScheduledLoanResponseDto;
import loans.persistance.model.Company;
import loans.persistance.model.Currency;
import loans.persistance.model.Loan;
import loans.persistance.model.LoanStatus;
import loans.persistance.repository.LoanRepository;
import loans.scheduler.client.SchedulerRestClientImpl;
import loans.scheduler.dto.LoanRequestDto;
import loans.scheduler.dto.SchedulerDto;
import loans.web.exception.InvalidDataException;
import loans.web.exception.ItemNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class LoanServiceImpl implements LoanService {

    private static final BigDecimal LIMIT_PERCENT = BigDecimal.valueOf(0.3);
    private static final Integer DEFAULT_TERM = 6;
    private static final Integer MIN_TERM = 1;
    private static final Integer MAX_TERM = 12;
    private static final Integer BILLING_PERIOD = 12;
    private static final Integer ROUND_SCALE = 2;
    private static final Integer ALLOWABLE_APPLICATION_COUNT = 2;
    private static final Integer PERCENT = 100;

    private LoanRepository loanRepository;

    private CompanyService companyService;

    private SchedulerRestClientImpl schedulerRestClient;

    @Autowired
    public LoanServiceImpl(LoanRepository loanRepository, CompanyService companyService,
                           SchedulerRestClientImpl schedulerRestClient) {
        this.loanRepository = loanRepository;
        this.companyService = companyService;
        this.schedulerRestClient = schedulerRestClient;
    }

    @Override
    public Loan create(Loan loan) {
        checkLoanData(loan);
        loan.setStatus(LoanStatus.NEW);

        Company company = companyService.saveAsActive(loan.getCompany());
        loan.setCompany(company);

        Loan savedLoan = loanRepository.save(loan);
        log.info("Loan with id {} was saved", savedLoan.getId());

        return savedLoan;
    }

    @Override
    public Loan getById(Long id) {
        Optional<Loan> loan = loanRepository.findById(id);
        if (loan.isPresent()) {
            return loan.get();
        }

        String message = MessageFormat.format("Loan with id {0} not found", id);
        log.error(message);
        throw new ItemNotFoundException(message);
    }

    @Override
    public List<Loan> findAll() {
        return (List<Loan>) loanRepository.findAll();
    }

    @Override
    public Loan rejectLoan(Long id) {
        Loan loanToReject = getById(id);
        loanToReject.setStatus(LoanStatus.REJECTED);

        Loan updatedLoan = loanRepository.save(loanToReject);
        log.info("Loan with id {} was rejected", id);

        return updatedLoan;
    }

    @Override
    public Loan validateLoan(Long id, Double interestRate) {
        Loan loanToValidate = getById(id);

        if (interestRate <= 0) {
            log.error("Invalid interest rate", interestRate);
            throw new InvalidDataException("Invalid loan. Interest rate should be > 0");
        }

        if (hasBlockedCompany(loanToValidate) || !isSolvent(loanToValidate, interestRate)) {
            loanToValidate.setStatus(LoanStatus.INVALID);
        } else {
            loanToValidate.setStatus(LoanStatus.VALID);
        }

        loanToValidate.setInterestRate(interestRate);

        Loan savedLoan = loanRepository.save(loanToValidate);
        log.info("Loan with id {} was validated, status {}", id, savedLoan.getStatus());

        return savedLoan;
    }

    @Override
    public ScheduledLoanResponseDto confirmLoan(Long id) {
        Loan loan = getById(id);
        checkStatus(loan, LoanStatus.VALID);

        loan.setStatus(LoanStatus.CONFIRMED);
        loan.setConfirmationDate(LocalDate.now());
        loanRepository.save(loan);
        log.info("Loan with id {} was confirmed", id);

        ScheduledLoanResponseDto scheduledLoanDto = new ScheduledLoanResponseDto();
        scheduledLoanDto.setLoan(loan);
        scheduledLoanDto.setScheduler(retrieveScheduler(loan));

        return scheduledLoanDto;
    }

    @Override
    public SchedulerDto findSchedulerByLoanId(Long id) {
        Loan loan = getById(id);
        checkStatus(loan, LoanStatus.CONFIRMED);

        SchedulerDto scheduler = schedulerRestClient.findSchedulerByLoanId(id);
        log.info("Scheduler for loan with id {} was retrieved", id);
        return scheduler;
    }

    private void checkLoanData(Loan loan) {
        if (loan.getCurrency() == null) {
            loan.setCurrency(Currency.EUR);
        }

        if (loan.getTerm() == null) {
            loan.setTerm(DEFAULT_TERM);
        }

        if (loan.getTerm() < MIN_TERM || loan.getTerm() > MAX_TERM) {
            String invalidTermMessage = "Invalid loan. Term should be in the range from 1 to 12";
            log.error(invalidTermMessage);
            throw new InvalidDataException(invalidTermMessage);
        }

        String blockedCompanyMessage = MessageFormat.format("Invalid loan. Company with id {0} is blocked",
                loan.getCompany().getId());

        if (hasBlockedCompany(loan)) {
            log.error(blockedCompanyMessage);
            throw new InvalidDataException(blockedCompanyMessage);
        }

        if (shouldBeBlocked(loan)) {
            companyService.saveAsBlocked(loan.getCompany());
            log.error(blockedCompanyMessage);
            throw new InvalidDataException(blockedCompanyMessage);
        }

    }

    private void checkStatus(Loan loan, LoanStatus status) {
        if (loan.getStatus() != status) {
            String message = MessageFormat.format("Loan with id {0} has invalid status {1}, should be {2}",
                    loan.getId(), loan.getStatus(), status.name());
            log.error(message);
            throw new InvalidDataException(message);
        }
    }

    private boolean hasBlockedCompany(Loan loanToValidate) {
        return companyService.isBlockedCompany(loanToValidate.getCompany().getId());
    }

    private boolean shouldBeBlocked(Loan loan) {
        LocalDateTime to = LocalDateTime.now();
        List<Loan> loansDuringOneMinute = loanRepository
                .findByCreationTimeBetweenAndCompanyId(to.minusMinutes(1), to, loan.getCompany().getId());

        return loansDuringOneMinute.size() >= ALLOWABLE_APPLICATION_COUNT;
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
        BigDecimal monthlyExpensesForLoan = principal.add(commission);

        return monthlyExpensesForLoan.compareTo(monthlyLimit) < 0;
    }

    private SchedulerDto retrieveScheduler(Loan loanToConfirm) {
        LoanRequestDto loanRequestDto = mapToLoanRequestDto(loanToConfirm);
        return schedulerRestClient.createScheduler(loanRequestDto);
    }

    private LoanRequestDto mapToLoanRequestDto(Loan loan) {
        LoanRequestDto loanRequestDto = new LoanRequestDto();

        loanRequestDto.setId(loan.getId());
        loanRequestDto.setAmount(loan.getAmount());
        loanRequestDto.setInterestRate(loan.getInterestRate());
        loanRequestDto.setTerm(loan.getTerm());
        loanRequestDto.setConfirmDate(loan.getConfirmationDate());
        loanRequestDto.setCurrency(loan.getCurrency().name());

        return loanRequestDto;
    }

}

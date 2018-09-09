package loans.service;

import loans.dto.ScheduledLoanResponseDto;
import loans.persistance.model.Company;
import loans.persistance.model.Loan;
import loans.persistance.repository.LoanRepository;
import loans.scheduler.client.SchedulerRestClientImpl;
import loans.scheduler.dto.LoanRequestDto;
import loans.scheduler.dto.SchedulerDto;
import loans.web.exception.InvalidDataException;
import loans.web.exception.ItemNotFoundException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static loans.persistance.model.CompanyStatus.ACTIVE;
import static loans.persistance.model.Currency.EUR;
import static loans.persistance.model.LoanStatus.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LoanServiceImplTest {

    @InjectMocks
    private LoanServiceImpl loanService;

    @Mock
    private CompanyService companyService;

    @Mock
    private SchedulerRestClientImpl schedulerRestClient;

    @Mock
    private LoanRepository loanRepository;

    private Loan loan;
    private Company company;
    private Long loanId = 1L;

    @Before
    public void setup() {
        loan = new Loan();
        loan.setId(loanId);
        loan.setAmount(BigDecimal.valueOf(1000));
        loan.setEmail("uhf@uih.com");
        loan.setPhone("234-34-76");
        loan.setTurnover(BigDecimal.valueOf(65000));
        loan.setTerm(8);

        company = new Company();
        company.setId(2L);
        company.setName("ASR");
        company.setStatus(ACTIVE);

        loan.setCompany(company);

        when(companyService.saveAsActive(company)).thenReturn(company);
        when(companyService.isBlockedCompany(company.getId())).thenReturn(false);
        when(loanRepository.save(loan)).thenReturn(loan);
        when(loanRepository.findByCreationTimeBetweenAndCompanyId(any(LocalDateTime.class), any(LocalDateTime.class),
                eq(company.getId()))).thenReturn(Collections.emptyList());
        when(loanRepository.findById(loanId)).thenReturn(Optional.of(loan));

        SchedulerDto scheduler = new SchedulerDto();
        scheduler.setId(12L);
        when(schedulerRestClient.findSchedulerByLoanId(eq(loanId))).thenReturn(scheduler);
    }

    @Test
    public void createLoan() {
        Loan actualLoan = loanService.create(loan);
        assertEquals(actualLoan.getStatus(), NEW);
        assertEquals(actualLoan.getCompany(), company);
        assertEquals(actualLoan.getCurrency(), EUR);
        assertEquals(8, actualLoan.getTerm().intValue());
    }

    @Test
    public void createLoanDefaultTerm() {
        loan.setTerm(null);
        Loan actual = loanService.create(loan);
        assertEquals(6, actual.getTerm().intValue());
    }

    @Test(expected = InvalidDataException.class)
    public void createLoanInvalidTerm() {
        loan.setTerm(0);
        loanService.create(loan);
    }

    @Test(expected = InvalidDataException.class)
    public void createLoanTermMoreThanAllowed() {
        loan.setTerm(13);
        loanService.create(loan);
    }

    @Test(expected = InvalidDataException.class)
    public void createLoanBlockedCompany() {
        when(companyService.isBlockedCompany(company.getId())).thenReturn(true);
        loanService.create(loan);
    }

    @Test(expected = InvalidDataException.class)
    public void blockCompanyOnLoanCreation() {
        when(loanRepository.findByCreationTimeBetweenAndCompanyId(any(LocalDateTime.class), any(LocalDateTime.class),
                eq(company.getId()))).thenReturn(Arrays.asList(new Loan(), new Loan()));

        loanService.create(loan);
    }

    @Test
    public void getById() {
        Loan actual = loanService.getById(loanId);
        assertEquals(loanId, actual.getId());
    }

    @Test
    public void rejectLoan() {
        Loan actual = loanService.rejectLoan(loanId);
        assertEquals(REJECTED, actual.getStatus());
    }

    @Test(expected = ItemNotFoundException.class)
    public void rejectLoanNotFound() {
        when(loanRepository.findById(loanId)).thenReturn(Optional.empty());
        loanService.rejectLoan(loanId);
    }

    @Test
    public void validateLoan() {
        Loan actual = loanService.validateLoan(loanId, 12.3);
        assertEquals(VALID, actual.getStatus());
        assertEquals(12.3, actual.getInterestRate());
    }

    @Test(expected = ItemNotFoundException.class)
    public void validateLoanNotFound() {
        when(loanRepository.findById(loanId)).thenReturn(Optional.empty());
        loanService.validateLoan(loanId, 3.5D);
    }

    @Test(expected = InvalidDataException.class)
    public void validateLoanInvalidInterestRate() {
        loanService.validateLoan(loanId, 0.0);
    }

    @Test
    public void validateLoanBlockedCompany() {
        when(companyService.isBlockedCompany(company.getId())).thenReturn(true);
        Loan actual = loanService.validateLoan(loanId, 5.0);
        assertEquals(INVALID, actual.getStatus());
    }

    @Test
    public void validateLoanInsolventCompany() {
        loan.setTurnover(BigDecimal.valueOf(12000));
        loan.setAmount(BigDecimal.valueOf(10000));

        Loan actual = loanService.validateLoan(loanId, 0.1);
        assertEquals(INVALID, actual.getStatus());
    }

    @Test
    public void confirmLoan() {
        loan.setStatus(VALID);
        loan.setCurrency(EUR);

        SchedulerDto scheduler = new SchedulerDto();
        scheduler.setId(12L);
        when(schedulerRestClient.createScheduler(any(LoanRequestDto.class))).thenReturn(scheduler);

        ScheduledLoanResponseDto scheduledLoan = loanService.confirmLoan(loanId);
        assertEquals(loan, scheduledLoan.getLoan());
        assertNotNull(scheduledLoan.getScheduler());
        assertEquals(12, scheduledLoan.getScheduler().getId().intValue());
        assertNotNull(scheduledLoan.getLoan().getConfirmationDate());
        assertEquals(CONFIRMED, scheduledLoan.getLoan().getStatus());
    }

    @Test(expected = ItemNotFoundException.class)
    public void confirmLoanNotFound() {
        when(loanRepository.findById(loanId)).thenReturn(Optional.empty());
        loanService.confirmLoan(loanId);
    }

    @Test(expected = InvalidDataException.class)
    public void confirmLoanNotValidated() {
        loan.setStatus(REJECTED);
        loanService.confirmLoan(loanId);
    }

    @Test
    public void findSchedulerByLoanId() {
        loan.setStatus(CONFIRMED);

        SchedulerDto actual = loanService.findSchedulerByLoanId(loanId);
        assertEquals(12L, actual.getId().longValue());
    }

    @Test(expected = InvalidDataException.class)
    public void findSchedulerByLoanIdNotConfirmedLoan() {
        loanService.findSchedulerByLoanId(loanId);
    }

    @Test(expected = ItemNotFoundException.class)
    public void findSchedulerLoanNotFound() {
        when(loanRepository.findById(loanId)).thenReturn(Optional.empty());
        loanService.findSchedulerByLoanId(loanId);
    }

}
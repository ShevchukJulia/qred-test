package loans.web.controller;

import loans.scheduler.dto.SchedulerDto;
import loans.dto.ScheduledLoanResponseDto;
import loans.persistance.model.Loan;
import loans.service.LoanService;
import loans.service.LoanServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/loans")
public class LoanController {

    private LoanService loanService;

    @Autowired
    public LoanController(LoanServiceImpl loanServiceImpl) {
        this.loanService = loanServiceImpl;
    }

    @RequestMapping(method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    public Loan createLoan(@RequestBody Loan loan) {
        return loanService.create(loan);
    }

    @RequestMapping(method = RequestMethod.GET)
    public List<Loan> findAll() {
        return loanService.findAll();
    }

    @RequestMapping(method = RequestMethod.GET, path = "/{id}")
    public Loan getById(@PathVariable Long id) {
        return loanService.getById(id);
    }

    @RequestMapping(method = RequestMethod.GET, path = "/{id}/reject")
    public Loan rejectLoan(@PathVariable Long id) {
        return loanService.rejectLoan(id);
    }

    @RequestMapping(method = RequestMethod.GET, path = "/{id}/validate")
    public Loan validateLoan(@PathVariable Long id,
                             @RequestParam("interest_rate") Double interestRate) {
        return loanService.validateLoan(id, interestRate);
    }

    @RequestMapping(method = RequestMethod.GET, path = "/{id}/confirm")
    public ScheduledLoanResponseDto confirmLoan(@PathVariable Long id) {
        return loanService.confirmLoan(id);
    }

    @RequestMapping(method = RequestMethod.GET, path = "/{id}/scheduler")
    public SchedulerDto getSchedulerByLoanId(@PathVariable Long id) {
        return loanService.findScheduler(id);
    }

}

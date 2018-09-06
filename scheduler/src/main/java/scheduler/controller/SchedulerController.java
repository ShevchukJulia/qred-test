package scheduler.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import scheduler.dto.LoanDto;
import scheduler.persistance.model.Scheduler;
import scheduler.service.SchedulerService;

import javax.validation.Valid;

@RestController
@RequestMapping("/scheduler")
public class SchedulerController {

    private SchedulerService service;

    @Autowired
    public SchedulerController(SchedulerService service) {
        this.service = service;
    }

    @RequestMapping(method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    public Scheduler createScheduler(@RequestBody @Valid LoanDto loan) {
        return service.create(loan);
    }

    @RequestMapping(method = RequestMethod.GET, path = "/{id}")
    public Scheduler getSchedulerById(@PathVariable Long id) {
        return service.findById(id);
    }

    @RequestMapping(method = RequestMethod.GET)
    public Scheduler getSchedulerByLoanId(@RequestParam("loan_id") Long loanId) {
        return service.findByLoanId(loanId);
    }

}

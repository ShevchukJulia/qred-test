package loans.scheduler.client;

import loans.scheduler.dto.SchedulerDto;
import loans.dto.LoanRequestDto;
import loans.web.exception.InvalidDataException;
import loans.web.exception.ItemNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Component
public class SchedulerRestClientImpl implements SchedulerRestClient {

    private static final String LOAN_ID = "loan_id";

    @Value("${scheduler.url}")
    private String schedulerBaseUrl;

    private RestTemplate restTemplate;

    @Autowired
    public SchedulerRestClientImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public SchedulerDto createScheduler(LoanRequestDto loan) {
        URI uri = UriComponentsBuilder
                .fromHttpUrl(schedulerBaseUrl)
                .build()
                .toUri();

        ResponseEntity<SchedulerDto> response = restTemplate
                .exchange(uri, HttpMethod.POST, new HttpEntity<>(loan), SchedulerDto.class);

        return response.getBody();
    }

    @Override
    public SchedulerDto findSchedulerByLoanId(Long loanId) {
        URI uri = UriComponentsBuilder
                .fromHttpUrl(schedulerBaseUrl)
                .queryParam(LOAN_ID, loanId)
                .build()
                .toUri();

        ResponseEntity<SchedulerDto> response = restTemplate.getForEntity(uri, SchedulerDto.class);

        return response.getBody();
    }

}

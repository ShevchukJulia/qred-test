package loans.client;


import loans.dto.ScheduleItem;
import loans.persistance.model.Loan;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SchedulerRestClient {

    public List<ScheduleItem> createScheduler(Loan loan) {
        return new ArrayList<>();
    }
}

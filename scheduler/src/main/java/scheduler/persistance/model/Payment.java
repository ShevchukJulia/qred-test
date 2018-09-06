package scheduler.persistance.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Data
public class Payment {

    @Id
    @Column(name = "payment_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long paymentId;

    @NotNull
    @Column(name = "term_date")
    private LocalDate termDate;

    @NotNull
    private BigDecimal principal;

    @NotNull
    private BigDecimal commission;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "scheduler_id")
    private Scheduler scheduler;

}

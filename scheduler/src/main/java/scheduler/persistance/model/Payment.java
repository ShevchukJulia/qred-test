package scheduler.persistance.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter
@Setter
public class Payment {

    @Id
    @Column(name = "payment_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

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

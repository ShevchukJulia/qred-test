package scheduler.persistance.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Getter
@Setter
public class Scheduler {

    @Id
    @Column(name = "scheduler_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull
    @Column(name = "total_principle")
    private BigDecimal totalPrinciple;

    @NotNull
    @Column(name = "total_commission")
    private BigDecimal totalCommission;

    @NotNull
    private String currency;

    @NotNull
    @Column(name = "loan_id", unique = true)
    private Long loanId;

    @OneToMany(mappedBy = "scheduler", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Payment> payments;

}

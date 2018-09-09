package loans.persistance.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "loan_id")
    private Long id;

    @NotNull
    private BigDecimal amount;

    @Enumerated(value = EnumType.STRING)
    private Currency currency;

    @NotNull
    @Email
    private String email;

    @NotNull
    private String phone;

    private BigDecimal turnover;

    private Integer term;

    @Enumerated(value = EnumType.STRING)
    private LoanStatus status;

    @Column(name = "confirmation_date")
    private LocalDate confirmationDate;

    @CreationTimestamp
    @Column(name = "creation_time")
    private LocalDateTime creationTime;

    @Column(name = "interest_rate")
    private Double interestRate;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "company_id")
    private Company company;
}

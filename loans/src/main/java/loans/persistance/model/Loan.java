package loans.persistance.model;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "loan_id")
    private Long loanId;

    @NotNull
    private BigDecimal amount;

    private String currency;

    @NotNull
    @Column(name = "company_id")
    private Integer companyId;

    @NotNull
    @Email
    private String email;

    @NotNull
    private String phone;

    private BigDecimal turnover;

    private Integer term;

    @Column(name = "company_name")
    private String companyName;

    @Column(name = "company_type")
    @Enumerated(EnumType.STRING)
    private CompanyType companyType;

    private LoanStatus status;

    @Column(name = "confirmation_date")
    private LocalDate confirmationDate;

    @CreationTimestamp
    @Column(name = "creation_time")
    private LocalDateTime creationTime;

    @Column(name = "interest_rate")
    private Float interestRate;

}

package loans.persistance.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.List;

@Entity
@Getter
@Setter
public class Company {

    @Id
    @NotNull
    @Column(name = "company_id")
    private Long id;

    private String name;

    @Enumerated(EnumType.STRING)
    private CompanyType type;

    @Enumerated(EnumType.STRING)
    private CompanyStatus status;

    @JsonIgnore
    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Loan> loans;

}

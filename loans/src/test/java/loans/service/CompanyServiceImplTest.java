package loans.service;

import loans.persistance.model.Company;
import loans.persistance.repository.CompanyRepository;
import loans.web.exception.ItemNotFoundException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

import static loans.persistance.model.CompanyStatus.ACTIVE;
import static loans.persistance.model.CompanyStatus.BLOCKED;
import static loans.persistance.model.CompanyType.HOLDING_COMPANY;
import static loans.persistance.model.CompanyType.LTD;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CompanyServiceImplTest {

    @InjectMocks
    private CompanyServiceImpl service;

    @Mock
    private CompanyRepository companyRepository;

    private Company company;
    private Long id = 12345L;

    @Before
    public void setup() {
        company = new Company();
        company.setId(id);
        company.setStatus(ACTIVE);
        company.setName("Company-X");
        company.setType(LTD);

        when(companyRepository.save(company)).thenReturn(company);
    }

    @Test
    public void saveAsActive() {
        when(companyRepository.findById(id)).thenReturn(Optional.empty());

        Company actualCompany = service.saveAsActive(company);
        assertTrue(actualCompany.getId().equals(id));
        assertTrue(actualCompany.getStatus().equals(ACTIVE));
    }

    @Test
    public void updateCompany() {
        String name = "SRRRR";
        when(companyRepository.findById(id)).thenReturn(Optional.of(company));

        company.setName(name);
        company.setType(HOLDING_COMPANY);

        Company actualCompany = service.saveAsActive(company);

        assertTrue(actualCompany.getId().equals(id));
        assertTrue(actualCompany.getStatus().equals(ACTIVE));
        assertTrue(actualCompany.getName().equals(name));
        assertTrue(actualCompany.getType().equals(HOLDING_COMPANY));
    }

    @Test
    public void saveAsBlocked() {
        when(companyRepository.findById(id)).thenReturn(Optional.of(company));

        Company actualCompany = service.saveAsBlocked(company);

        assertTrue(actualCompany.getId().equals(id));
        assertTrue(actualCompany.getStatus().equals(BLOCKED));
    }

    @Test(expected = ItemNotFoundException.class)
    public void saveAsBlockedNotFound() {
        when(companyRepository.findById(id)).thenReturn(Optional.empty());
        service.saveAsBlocked(company);
    }

    @Test
    public void isBlockedCompany() {
        company.setStatus(BLOCKED);
        when(companyRepository.findByIdAndStatus(id, BLOCKED)).thenReturn(Optional.of(company));
        assertTrue(service.isBlockedCompany(id));
    }

}
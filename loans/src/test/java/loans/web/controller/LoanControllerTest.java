package loans.web.controller;


import loans.persistance.model.Company;
import loans.persistance.model.Loan;
import loans.persistance.model.LoanStatus;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.List;

import static loans.persistance.model.LoanStatus.NEW;
import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class LoanControllerTest {

    private static final String BASE_URL = "/loans";

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Test
    public void createLoan() {
        Loan loan = new Loan();
        loan.setAmount(BigDecimal.valueOf(1000));
        loan.setEmail("uhf@uih.com");
        loan.setPhone("234-34-76");
        loan.setTurnover(BigDecimal.valueOf(65000));
        loan.setTerm(8);

        Company company = new Company();
        company.setId(2L);
        company.setName("ASR");
        loan.setCompany(company);

        ResponseEntity<Loan> response = testRestTemplate.postForEntity(BASE_URL, loan, Loan.class);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());

        Loan actual = response.getBody();
        assertNotNull(actual.getId());
        assertEquals(NEW, actual.getStatus());
        assertEquals(2L, actual.getCompany().getId().longValue());
    }

    @Test
    @Sql("test_data.sql")
    public void testFindAll() {
        ResponseEntity<List<Loan>> response = testRestTemplate.exchange(
                BASE_URL,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Loan>>() {
                });
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().size() == 2);
    }

    @Test
    @Sql("test_data.sql")
    public void getById() {
        ResponseEntity<Loan>response = testRestTemplate.getForEntity(
                BASE_URL + "/1",
                Loan.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1L, response.getBody().getId().longValue());
    }

    @Test
    @Sql("test_data.sql")
    public void rejectLoan() {
        ResponseEntity<Loan>response = testRestTemplate.getForEntity(
                BASE_URL + "/1/reject",
                Loan.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(LoanStatus.REJECTED, response.getBody().getStatus());
    }

    @Test
    @Sql("test_data.sql")
    public void validateLoan() {
        ResponseEntity<Loan>response = testRestTemplate.getForEntity(
                BASE_URL + "/1/validate?interest_rate=3",
                Loan.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(LoanStatus.VALID, response.getBody().getStatus());
    }

}
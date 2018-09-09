package loans.service;

import loans.persistance.model.Company;
import loans.persistance.model.CompanyStatus;
import loans.persistance.repository.CompanyRepository;
import loans.web.exception.InvalidDataException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.Optional;

@Slf4j
@Service
public class CompanyServiceImpl implements CompanyService {

    private CompanyRepository repository;

    @Autowired
    public CompanyServiceImpl(CompanyRepository repository) {
        this.repository = repository;
    }

    @Override
    public Company saveAsActive(Company company) {
        Optional<Company> companyOptional = repository.findById(company.getId());
        if (companyOptional.isPresent()) {
            Company companyToUpdate = repository.save(updateCompany(company, companyOptional.get()));
            log.info("Company with id {} was updated", companyToUpdate.getId());
            return companyToUpdate;
        }

        company.setStatus(CompanyStatus.ACTIVE);
        Company companyToSave = repository.save(company);
        log.info("Company with id {} was saved", companyToSave.getId());
        return companyToSave;
    }

    @Override
    public Company saveAsBlocked(Company company) {
        Optional<Company> companyOptional = repository.findById(company.getId());
        if (!companyOptional.isPresent()) {
            String message = MessageFormat.format("Company with id {0} does not exist", company.getId());
            log.error(message);
            throw new InvalidDataException(message);
        }
        Company companyToSave = companyOptional.get();
        companyToSave.setStatus(CompanyStatus.BLOCKED);

        Company savedCompany = repository.save(companyToSave);
        log.info("Company with id {} is blocked", savedCompany.getId());

        return savedCompany;
    }

    @Override
    public boolean isBlockedCompany(Long id) {
        return repository.findByIdAndStatus(id, CompanyStatus.BLOCKED).isPresent();
    }

    private Company updateCompany(Company companyFrom, Company companyTo) {
        companyTo.setName(companyFrom.getName());
        companyTo.setType(companyFrom.getType());
        return companyTo;
    }

}

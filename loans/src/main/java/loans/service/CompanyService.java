package loans.service;

import loans.persistance.model.Company;

public interface CompanyService {

    Company saveAsActive(Company company);

    Company saveAsBlocked(Company company);

    boolean isBlockedCompany(Long id);

}

package dev.kalles.security.context;

import dev.kalles.security.exception.CompanyContextRequiredException;

import java.util.UUID;

public class CompanyContextHolder {

    public static UUID getCompanyId() {
        return RequestContext.current().companyId();
    }

    public static UUID requireCompanyId() {
        UUID companyId = getCompanyId();
        if (companyId == null) {
            throw new CompanyContextRequiredException();
        }
        return companyId;
    }
}

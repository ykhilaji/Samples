package core.service;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.logging.Logger;

@Service
public class SampleServiceImpl implements SampleService {
    private Logger logger = Logger.getLogger(SampleServiceImpl.class.getSimpleName());

    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN', 'DBA', 'DEVELOPER')")
    public String all() {
        logger.info("ALL USERS");

        return "ALL USERS";
    }

    @PreAuthorize("hasAuthority('ADMIN') and hasAuthority('ALL')")
    public String adminOnly() {
        logger.info("ADMIN ONLY");

        return "ADMIN ONLY";
    }

    @PreAuthorize("(hasAuthority('DBA') or hasAuthority('ADMIN')) and (hasAnyAuthority('DBA_AUTHORITIES', 'SENIOR_DBA_AUTHORITIES') or hasAuthority('ALL'))")
    public String dbaOnly() {
        logger.info("DBA ONLY OR ADMIN");

        return "DBA ONLY OR ADMIN";
    }

    @PreAuthorize("(hasAuthority('DBA') or hasAuthority('ADMIN')) and (hasAuthority('SENIOR_DBA_AUTHORITIES') or hasAuthority('ALL'))")
    public String seniorDbaOnly() {
        logger.info("SENIOR DBA ONLY OR ADMIN");

        return "SENIOR DBA ONLY OR ADMIN";
    }

    @PreAuthorize("(hasAuthority('DEVELOPER') or hasAuthority('ADMIN')) and (hasAnyAuthority('DEVELOPER_AUTHORITIES', 'SENIOR_DEVELOPER_AUTHORITIES') or hasAuthority('ALL'))")
    public String devloperOnly() {
        logger.info("DEVELOPER ONLY OR ADMIN");

        return "DEVELOPER ONLY OR ADMIN";
    }

    @PreAuthorize("(hasRole('DEVELOPER') or hasRole('ADMIN'))and (hasAuthority('SENIOR_DEVELOPER_AUTHORITIES') or hasAuthority('ALL'))")
    public String seniorDeveloperOnly() {
        logger.info("SENIOR DEVELOPER ONLY OR ADMIN");

        return "SENIOR DEVELOPER ONLY OR ADMIN";
    }
}

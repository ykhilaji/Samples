package core.controller;

import core.service.SampleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.logging.Logger;

@RestController
public class IndexController {
    private Logger logger = Logger.getLogger(IndexController.class.getSimpleName());

    @Autowired
    private SampleService service;

    @GetMapping(value = "/")
    public String all() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        logger.info(String.format("Username: %s, authorities: %s", user.getUsername(), user.getAuthorities().toString()));

        logger.info("All");

        return service.all();
    }

    @GetMapping(value = "/admin")
    public String admin() {
        logger.info("Admin");

        return service.adminOnly();
    }

    @GetMapping(value = "/dba")
    public String dba() {
        logger.info("Dba");

        return service.dbaOnly();
    }

    @GetMapping(value = "/dba/senior")
    public String seniorDba() {
        logger.info("Senior dba");

        return service.seniorDbaOnly();
    }

    @GetMapping(value = "/developer")
    public String developer() {
        logger.info("Developer");

        return service.devloperOnly();
    }

    @GetMapping(value = "/developer/senior")
    public String seniorDeveloper() {
        logger.info("Senior developer");

        return service.seniorDeveloperOnly();
    }
}

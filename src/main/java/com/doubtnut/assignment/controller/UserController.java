package com.doubtnut.assignment.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


public class UserController {

    @Autowired(required = false)
    private SessionRegistry sessionRegistry;

    public List<SessionInformation> listInactiveUsersLoggedInUsers() {
        final List<Object> allPrincipals = sessionRegistry.getAllPrincipals();

        for (final Object principal : allPrincipals) {
            if (principal instanceof SecurityProperties.User) {
                final SecurityProperties.User user = (SecurityProperties.User) principal;

                // fetching Active suers
                List<SessionInformation> activeUserSessions =
                        sessionRegistry.getAllSessions(principal,
                                /* includeExpiredSessions */ false); // Should not return null;

                // fetching InActive suers
                List<SessionInformation> inActiveUserSessions =
                        sessionRegistry.getAllSessions(principal,
                                /* includeExpiredSessions */ true);

                if (!inActiveUserSessions.isEmpty()) {
                    // Do something with user
                    System.out.println(user);
                    return inActiveUserSessions;
                }
            }
        }
        return null;
    }
}
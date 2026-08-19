package com.financialdistrict.strategy.cli;

import org.springframework.stereotype.Component;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Component
@Command(name = "create-user", description = "Create a new user")
public class ExampleUserCommand implements Runnable {

    @Parameters(index = "0", description = "Username")
    private String username;

    @Parameters(index = "1", description = "Email")
    private String email;

    @Override
    public void run() {
        System.out.println("HEREs johny!");
        System.out.println("username: " + username);
        System.out.println("email: " + email);
    }
}
package com.financialdistrict.strategy.cli;

import org.springframework.stereotype.Component;

import picocli.CommandLine.Command;

@Component
@Command(
    name = "my-app",
    description = "My application CLI",
    subcommands = {
        SimulateForPrototypeManCommand.class
    }
)
public class MyAppCommand implements Runnable {

    @Override
    public void run() {
        System.out.println(
            "Use --help to see available commands."
        );
    }
}
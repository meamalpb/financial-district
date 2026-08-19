package com.financialdistrict.strategy.cli;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import picocli.CommandLine;
import picocli.CommandLine.IFactory;

@Component
public class PicocliRunner implements CommandLineRunner {

    private final MyAppCommand myAppCommand;
    private final IFactory factory;

    public PicocliRunner(MyAppCommand myAppCommand, IFactory factory) {
        this.myAppCommand = myAppCommand;
        this.factory = factory;
    }

    @Override
    public void run(String... args) throws Exception {
        if (args.length == 0) {
            return;
        }
        int exitCode = new CommandLine(myAppCommand, factory).execute(args);
        System.exit(exitCode);
    }
}

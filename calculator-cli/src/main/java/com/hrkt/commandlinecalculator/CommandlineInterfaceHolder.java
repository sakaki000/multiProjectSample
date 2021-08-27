package com.hrkt.commandlinecalculator;

import org.springframework.stereotype.Component;

@Component
public class CommandlineInterfaceHolder {
    public static CommandlineInterface commandlineInterface = new CommandlineInterface();
}
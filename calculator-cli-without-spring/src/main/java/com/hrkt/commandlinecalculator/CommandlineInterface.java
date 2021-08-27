package com.hrkt.commandlinecalculator;

import lombok.extern.slf4j.Slf4j;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.io.IOException;

@Slf4j
public class CommandlineInterface {
    public void run() {
        DeskCalculator deskCalculator = new DeskCalculator();

        try (Terminal terminal = TerminalBuilder.terminal()) {
            RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
            long uptimeInMillis = runtimeMXBean.getUptime();
            terminal.writer().println(String.format("Calculator is running. Press ctrl-c to exit.(boot in %d msec.)",
                    uptimeInMillis));
	    terminal.flush();
            int ch;
            while ((ch = terminal.reader().read()) != 0x09) {
		//ToDo: java.util.IllegalFormatCodePointException: Code point = 0xfffffffe
                //log.debug(String.format("%d, %c", (int)ch, ch));
                // TAB(0x09)で抜ける
                boolean needPrint = false;
                try {
                    switch (ch) {
                        case '+': {
                            deskCalculator.pushPlusButton();
                            log.debug("plus");
                            needPrint = true;
                            break;
                        }
                        case '-': {
                            deskCalculator.pushSubtractButton();
                            log.debug("subtract");
                            needPrint = true;
                            break;
                        }
                        case '*': {
                            deskCalculator.pushMultiplyButton();
                            log.debug("multiply");
                            needPrint = true;
                            break;
                        }
                        case '/': {
                            deskCalculator.pushDivideButton();
                            log.debug("divide");
                            needPrint = true;
                            break;
                        }
                        case '=': {
                            // same as 'Enter', so do not break here.
                        }
                        case 13: {
                            // 'Enter'
                            deskCalculator.pushEvalButton();
                            log.debug(deskCalculator.getCurrentValue().toPlainString());
                            terminal.writer().println();
                            terminal.writer().print("=" + deskCalculator.getCurrentValue().stripTrailingZeros().toPlainString());
                            terminal.writer().println();
	                    terminal.flush();
                            break;
                        }
                        case 8: {
                            // 'Backspace'
                            deskCalculator.pushClearButton();
                            terminal.writer().println();
	                    terminal.flush();
                            break;
                        }
                        case 'q': {
                            System.exit(0);
                        }
                        default: {
                            needPrint = deskCalculator.pushChar((char) ch);
                        }
                    }
                    if (needPrint) {
                        terminal.writer().print((char) ch);
	                terminal.flush();
                    }
                } catch (ArithmeticException e) {
                    showErrorMessage(terminal, e);
                }
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    private void showErrorMessage(Terminal terminal, ArithmeticException e) {
        terminal.writer().println();
        terminal.writer().println("ERROR: " + e.getMessage());
	terminal.flush();
    }
}

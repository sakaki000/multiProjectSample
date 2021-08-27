package com.hrkt.commandlinecalculator;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.var;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * DeskCalculator
 *
 * Note:
 * <ul>
 *     <li>When division cause ArithmeticException, this class falls back to use MathContext.DECIMAL128 mode.</li>
 * </ul>
 *
 * see:
 * https://docs.oracle.com/javase/jp/1.3/api/java/math/BigDecimal.html
 *
 */
@Slf4j
public class DeskCalculator {
    private BigDecimal stack;
    private StringBuilder sb;
    private boolean needClearBufferInNextInput;

    enum Operator {
        PLUS {
            @Override
            BigDecimal apply(@NonNull BigDecimal lhs, @NonNull BigDecimal rhs) {
                return lhs.add(rhs);
            }
        },
        SUBTRACT {
            @Override
            BigDecimal apply(@NonNull BigDecimal lhs, @NonNull BigDecimal rhs) {
                return lhs.subtract(rhs);
            }
        },
        MULTIPLY {
            @Override
            BigDecimal apply(@NonNull BigDecimal lhs, @NonNull BigDecimal rhs) {
                return lhs.multiply(rhs);
            }
        },
        DIVIDE {
            @Override
            BigDecimal apply(@NonNull BigDecimal lhs, @NonNull BigDecimal rhs) {
                try {
                    return lhs.divide(rhs);
                } catch (ArithmeticException e) {
                    // fallback to MathContext.DECIMAL128
                    log.trace("ArithmeticException occurred:" + e);
                    return lhs.divide(rhs, MathContext.DECIMAL128);
                }
            }
        },
        NONE {
            // allow null for rhs
            @Override
            BigDecimal apply(@NonNull BigDecimal lhs, BigDecimal rhs) {
                return lhs;
            }
        };

        abstract BigDecimal apply(BigDecimal lhs, BigDecimal rhs);
    }

    private Operator currentOperator = Operator.NONE;

    public DeskCalculator() {
        clearStack();
        clearBuffer();
    }

    public synchronized boolean pushChar(char c) {
        if(needClearBufferInNextInput) {
            clearBuffer();
            needClearBufferInNextInput = false;
        }

        if ('0' <= c && c <= '9') {
            sb.append(c);
            return true;
        } else if (c == '.' && sb.toString().indexOf('.') < 0) {
            sb.append(c);
            return true;
        } else if (c == '-' && sb.length() == 0) {
            sb.append(c);
            return true;
        }

        // do nothing.
        return false;
    }

    public synchronized BigDecimal getCurrentValue() {
        if (0 == sb.length()) {
            return BigDecimal.ZERO;
        } else if(1 == sb.length() && '-' == sb.charAt(0)) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(sb.toString(), MathContext.UNLIMITED);
    }

    public synchronized void pushButtonInternal(Operator pushedOperator) {
        if(Operator.NONE == currentOperator) {
            stack = getCurrentValue();
        } else {
            stack = pushedOperator.apply(stack, getCurrentValue());
        }
        currentOperator = pushedOperator;
        clearBuffer();
    }

    public synchronized void pushPlusButton() {
        pushButtonInternal(Operator.PLUS);
    }

    public synchronized void pushSubtractButton() {
        pushButtonInternal(Operator.SUBTRACT);
    }

    public synchronized void pushMultiplyButton() {
        pushButtonInternal(Operator.MULTIPLY);
    }

    public synchronized void pushDivideButton() {
        pushButtonInternal(Operator.DIVIDE);
    }

    public synchronized BigDecimal pushEvalButton() {
        if (Operator.NONE == currentOperator) {
            return getCurrentValue();
        }
        var v = currentOperator.apply(stack, getCurrentValue());
        currentOperator = Operator.NONE;
        replaceBuffer(v.toPlainString());
        clearStack();
        needClearBufferInNextInput = true;
        return v;
    }

    public synchronized void pushClearButton() {
        currentOperator = Operator.NONE;
        clearBuffer();
        clearStack();
    }

    private synchronized void clearStack() {
        stack = BigDecimal.ZERO;
    }

    private synchronized void clearBuffer() {
        sb = new StringBuilder();
    }

    private synchronized void replaceBuffer(String s) {
        sb = new StringBuilder(s);
    }
}

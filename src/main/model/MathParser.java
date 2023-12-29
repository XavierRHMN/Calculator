package main.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class MathParser {

    enum TokenType {
        NUMBER, OPERATOR, PARENTHESIS
    }
    static class Token {
        String value;
        TokenType type;

        public Token(String value, TokenType type) {
            this.value = value;
            this.type = type;
        }
    }

    // Tokenize the expression
    private List<Token> tokenize(String expression) {
        List<Token> tokens = new ArrayList<>();
        char[] chars = expression.toCharArray();

        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (Character.isWhitespace(c)) continue;

            if (Character.isDigit(c) || c == '.') {
                StringBuilder number = new StringBuilder();
                while (i < chars.length && (Character.isDigit(chars[i]) || (chars[i] == '.' && !number.toString().contains(".")))) {
                    number.append(chars[i++]);
                }
                i--; // Adjust for the next character
                tokens.add(new Token(number.toString(), TokenType.NUMBER));

                // Implicit multiplication after a number
                if (i + 1 < chars.length && chars[i + 1] == '(') {
                    tokens.add(new Token("×", TokenType.OPERATOR));
                }
            } else if ("–".indexOf(c) != -1) {
                tokens.add(new Token("(", TokenType.PARENTHESIS));
                tokens.add(new Token("0", TokenType.NUMBER));
                tokens.add(new Token("-", TokenType.OPERATOR));
                tokens.add(new Token("1", TokenType.NUMBER));
                tokens.add(new Token(")", TokenType.PARENTHESIS));
                tokens.add(new Token("*", TokenType.OPERATOR));
            } else if ("+-×÷^".indexOf(c) != -1) {
                tokens.add(new Token(String.valueOf(c), TokenType.OPERATOR));
            } else if (c == '(') {
                tokens.add(new Token(String.valueOf(c), TokenType.PARENTHESIS));
            } else if (c == ')') {
                tokens.add(new Token(String.valueOf(c), TokenType.PARENTHESIS));
                // Implicit multiplication after a closing parenthesis
                if (i + 1 < chars.length && chars[i + 1] == '.') {
                    throw new IllegalArgumentException("Invalid expression: ')' directly followed by '.'");
                }

                if (i + 1 < chars.length && (Character.isDigit(chars[i + 1]) || chars[i + 1] == '(')) {
                    tokens.add(new Token("×", TokenType.OPERATOR));
                }
            }  else {
                throw new IllegalArgumentException("Invalid character: " + c);
            }
        }

        return tokens;
    }

    // if last item in operator stack has >= precedence than first item in input, then last item in operator
    // stack is pushed to output and first item in input is pushed to operator stack, otherwise the first item
    //  in input is pushed to operator stack

    // if an open bracket is first in the input it is pushed to operator stack, otherwise if a closing
    // bracket is first in the input then it will continually push each item from the operatorStack to the output
    // until it reaches the closing bracket in the operator stack, which is then popped
    public double parseExpression(String expression) {
        List<Token> tokens = tokenize(expression);
        Stack<Token> operatorStack = new Stack<>();
        Stack<Double> operandStack = new Stack<>();

        for (Token token : tokens) {
            switch (token.type) {
                case NUMBER:
                    operandStack.push(Double.parseDouble(token.value));
                    break;
                case OPERATOR:
                    while (!operatorStack.isEmpty() && getPrecedence(operatorStack.peek()) >= getPrecedence(token)) {
                        processOperator(operatorStack.pop(), operandStack);
                    }
                    operatorStack.push(token);
                    break;
                case PARENTHESIS:
                    if (token.value.equals("(")) {
                        operatorStack.push(token);
                    } else {
                        while (!operatorStack.isEmpty() && !operatorStack.peek().value.equals("(")) {
                            processOperator(operatorStack.pop(), operandStack);
                        }
                        operatorStack.pop(); // Pop the '(' from the stack
                    }
                    break;
            }
        }

        while (!operatorStack.isEmpty()) {
            processOperator(operatorStack.pop(), operandStack);
        }

        return operandStack.pop();
    }

    private void processOperator(Token operator, Stack<Double> operandStack) {
        double rightOperand = operandStack.pop();
        double leftOperand = operandStack.pop();
        switch (operator.value) {
            case "+":
                operandStack.push(leftOperand + rightOperand);
                break;
            case "-":
                operandStack.push(leftOperand - rightOperand);
                break;
            case "×":
                operandStack.push(leftOperand * rightOperand);
                break;
            case "*":
                operandStack.push(leftOperand * rightOperand);
                break;
            case "÷":
                operandStack.push(leftOperand / rightOperand);
                break;
            case "^":
                operandStack.push(Math.pow(leftOperand, rightOperand));
                break;
            default:
                throw new IllegalArgumentException("Unknown operator: " + operator.value);
        }
    }

    // gets the precedence of current operator
    private int getPrecedence(Token token) {
        if (token.type == TokenType.PARENTHESIS) return 0;

        switch (token.value) {
            case "+":
            case "-":
                return 1;
            case "×":
            case "÷":
                return 2;
            case "^":
                return 4;
            case "*":
                return 3;
            default:
                throw new IllegalArgumentException("Unknown operator: " + token.value);
        }
    }
}

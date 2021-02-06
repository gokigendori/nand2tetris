package com.gokigendori.nand2tetris.vm;

import java.util.Arrays;
import java.util.List;

public class Parser {
    private Command type;
    private String arg1;
    private Integer arg2;

    public Command getType() {
        return type;
    }

    public String getArg1() {
        return arg1;
    }

    public Integer getArg2() {
        return arg2;
    }

    public enum Segment{
        ARGUMENT,
        LOCAL,
        POINTER,
        STATIC,
        CONSTANT,
        THIS,
        THAT,
        TEMP;
    }

    public enum Command {
        C_ARITHMETIC,
        C_PUSH,
        C_POP,
        C_LABEL,
        C_GOTO,
        C_IF,
        C_FUNCTION,
        C_RETURN,
        C_CALL;
    }

    public Parser(String line) {
        String[] commands = line.split(" ");
        switch (commands.length) {
            case 1:
                if (args1List().contains(commands[0])) {
                    type = Command.C_ARITHMETIC;
                    arg1 = commands[0];
                } else if (commands[0].equals("return")) {
                    type = Command.C_RETURN;
                }
                break;
            case 2:
                switch (commands[0]) {
                    case "label":
                        type = Command.C_LABEL;
                        arg1 = commands[1];
                        break;
                    case "goto":
                        type = Command.C_GOTO;
                        arg1 = commands[1];
                        break;
                    case "if-goto":
                        type = Command.C_IF;
                        arg1 = commands[1];
                        break;
                }
                break;
            case 3:
                parserArgs3(commands);
                break;
        }
    }

    public Segment getSegment() {
        if (type.equals(Command.C_POP) || type.equals(Command.C_PUSH)) {
            switch (arg1) {
                case "argument":
                    return Segment.ARGUMENT;
                case "local":
                    return Segment.LOCAL;
                case "static":
                    return Segment.STATIC;
                case "pointer":
                    return Segment.POINTER;
                case "constant":
                    return Segment.CONSTANT;
                case "this":
                    return Segment.THIS;
                case "that":
                    return Segment.THAT;
                case "temp":
                    return Segment.TEMP;
            }
        }
        return null;
    }

    private void parserArgs3(String[] commands) {
        switch (commands[0]) {
            case "push":
                type = Command.C_PUSH;
                break;
            case "pop":
                type = Command.C_POP;
                break;
            case "function":
                type = Command.C_FUNCTION;
                break;
            case "call":
                type = Command.C_CALL;
                break;
        }
        arg1 = commands[1];
        arg2 = Integer.parseInt(commands[2]);
    }

    private List<String> args1List() {
        return Arrays.asList(
                "add", "sub", "neg",
                "eq", "gt", "lt", "and", "or", "not"
        );
    }
}

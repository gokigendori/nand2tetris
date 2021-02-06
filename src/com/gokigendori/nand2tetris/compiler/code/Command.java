package com.gokigendori.nand2tetris.compiler.code;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public enum Command {
    ADD("add","+"),
    SUB("sub","-"),
    NEG("neg","neg"),
    EQ("eq","="),
    GT("gt",">"),
    LT("lt","<"),
    AND("and","&"),
    OR("or","|"),
    NOT("not","~"),;

    private final String name;

    private final String symbol;

    Command(String name,String symbol) {
        this.name = name;
        this.symbol = symbol;
    }

    private static Map<String, Command> SYMBOL_VALUE_MAP = new HashMap<>();

    static {
        Stream.of(Command.values()).forEach(v -> SYMBOL_VALUE_MAP.put(v.symbol, v));
    }

    public static Command valueBySymbol(String symbol) {
        return SYMBOL_VALUE_MAP.get(symbol);
    }

    public String getName() {
        return name;
    }

}

package com.gokigendori.nand2tetris.compiler.code;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public enum SymbolKind {
    STATIC("static"),
    FIELD("field"),
    ARG("arg"),
    VAR("var"),
    NONE("none"),
    ;

    private final String name;
    private static Map<String, SymbolKind> CODE_VALUE_MAP = new HashMap<>();

    SymbolKind(String name) {
        this.name = name;
    }

    static {
        Stream.of(SymbolKind.values()).forEach(v -> CODE_VALUE_MAP.put(v.name, v));
    }

    public static SymbolKind valueByName(String name) {
        return CODE_VALUE_MAP.get(name);
    }

    public String getName() {
        return name;
    }
}

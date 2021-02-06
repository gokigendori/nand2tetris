package com.gokigendori.nand2tetris.compiler.code;

public class Symbol {
    private String name;
    private String type;
    private SymbolKind kind;
    private int index;

    public Symbol(String name, String type, SymbolKind kind, int index) {
        this.name = name;
        this.type = type;
        this.kind = kind;
        this.index = index;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public SymbolKind getKind() {
        return kind;
    }

    public int getIndex() {
        return index;
    }
}

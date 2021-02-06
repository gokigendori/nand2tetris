package com.gokigendori.nand2tetris.compiler.xml;

public class Terminal {
    TokenType type;
    String name;

    public TokenType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public Terminal(TokenType type, String name) {
        this.type = type;
        this.name = name;
    }
}

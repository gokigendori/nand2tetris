package com.gokigendori.nand2tetris.compiler.code;

public enum TokenType {
    KEYWORD("keyword"),
    SYMBOL("symbol"),
    IDENTIFIER("identifier"),
    INT_CONST("integerConstant"),
    STRING_CONST("stringConstant"),
    EOF("eof"),
    ;

    public String getName() {
        return name;
    }

    private final String name;

    TokenType(String name) {
        this.name = name;
    }
}
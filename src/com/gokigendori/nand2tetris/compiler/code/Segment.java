package com.gokigendori.nand2tetris.compiler.code;

public enum Segment {
    ARG("argument"),
    LOCAL("local"),
    POINTER("pointer"),
    STATIC("static"),
    CONSTANT("constant"),
    THIS("this"),
    THAT("that"),
    TEMP("temp"),
    ;

    private final String name;

    Segment(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}

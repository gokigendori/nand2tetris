package com.gokigendori.nand2tetris.compiler.code;

import java.util.HashMap;
import java.util.Map;

public class SymbolTable {
    private final Map<String, Symbol> map;

    public SymbolTable() {
        this.map = new HashMap<>();
    }

    public void startSubroutine() {
        this.map.clear();
    }

    public void define(String name, String type, SymbolKind kind) {
        // name, 型 , 属性
        this.map.put(name, new Symbol(name, type, kind, varCount(kind)));
    }

    public int varCount(SymbolKind kind) {
        int count = 0;
        for (Symbol symbol : map.values()) {
            if (symbol.getKind().equals(kind)) {
                count++;
            }
        }
        return count;
    }

    public SymbolKind kindOf(String name) {
        if (map.get(name) == null) {
            return SymbolKind.NONE;
        }
        return map.get(name).getKind();
    }

    public String typeOf(String name) {
        return map.get(name).getType();
    }

    public int indexOf(String name) {
        return map.get(name).getIndex();
    }
}

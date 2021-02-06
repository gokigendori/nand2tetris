package com.gokigendori.nand2tetris.compiler.code;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class VMWriter {
    private final String className;
    private List<String> results;
    private final Path out;
    private int ifNum;
    private int whileNum;

    public VMWriter(String className, Path out) {
        this.className = className;
        this.out = out;
        results = new ArrayList<>();
    }

    public void output() throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(out)) {
            writer.append(String.join("\n", results));
        }
    }

    public int getSize() {
        return results.size();
    }

    public int getIfNum() {
        return ifNum;
    }

    public int getWhileNum() {
        return whileNum;
    }

    public void incrementIf() {
        ifNum++;
    }

    public void incrementWhile() {
        whileNum++;
    }

    public String getClassName() {
        return className;
    }

    public void writePush(Segment segment, String index) {
        results.add(String.format("push %s %s", segment.getName(), Integer.parseInt(index)));
    }
    public void writePush(Segment segment, Integer index) {
        results.add(String.format("push %s %d", segment.getName(), index));
    }

    public void writePop(Segment segment, String index) {
        results.add(String.format("pop %s %d", segment.getName(), Integer.parseInt(index)));
    }
    public void writePop(Segment segment, Integer index) {
        results.add(String.format("pop %s %d", segment.getName(), index));
    }

    public void writeArithmetic(Command command) {
        results.add(command.getName());
    }

    public void writeLabel(String label) {
        results.add("label " + label);
    }

    public void writeGoto(String label) {
        results.add("goto " + label);
    }

    public void writeIf(String label) {
        results.add("if-goto " + label);
    }

    public void writeCall(String name, int nArgs) {
        results.add(String.format("call %s %d", name, nArgs));
    }

    public void writeFunction(String functionName, int nLocals) {
        ifNum = 0;
        whileNum = 0;
        results.add(String.format("function %s.%s %d", className, functionName, nLocals));
    }

    public void writeReturn() {
        results.add("return");
    }
}

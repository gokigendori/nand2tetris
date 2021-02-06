package com.gokigendori.nand2tetris.vm;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Vm {
    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            return;
        }
        File dir = new File(args[0]);
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }
        List<String> result = new ArrayList<>();
        result.add("@256");
        result.add("D=A");
        result.add("@SP");
        result.add("M=D");
        CodeWriter boot = new CodeWriter("");
        boot.writeCall(new Parser("call Sys.init 0"));
        result.addAll(boot.getResult());


        for (File file : files) {
            if (!file.isFile()) {
                continue;
            }
            if (!file.getName().matches(".*\\.vm")) {
                continue;
            }
            String text = Files.readString(file.toPath());
            List<String> lines = cleanArray(text);

            String fileName = file.getName().replaceAll("\\.vm", "");
            CodeWriter writer = new CodeWriter(fileName);
            for (String line : lines) {
                Parser p = new Parser(line);
                if (p.getType().equals(Parser.Command.C_FUNCTION)) {
                    writer.setFunctionName(p.getArg1());
                }
                if (p.getType().equals(Parser.Command.C_LABEL)) {
                    writer.addLabelMap(p);
                }
            }

            for (String line : lines) {
                Parser p = new Parser(line);
                switch (p.getType()) {
                    case C_PUSH:
                        writer.writePush(p);
                        break;
                    case C_POP:
                        writer.writePop(p);
                        break;
                    case C_ARITHMETIC:
                        writer.writeArithmetic(p);
                        break;
                    case C_LABEL:
                        writer.writeLabel(p);
                        break;
                    case C_IF:
                        writer.writeIf(p);
                        break;
                    case C_GOTO:
                        writer.writeGoto(p);
                        break;
                    case C_FUNCTION:
                        writer.setFunctionName(p.getArg1());
                        writer.writeFunction(p);
                        break;
                    case C_CALL:
                        writer.writeCall(p);
                        break;
                    case C_RETURN:
                        writer.writeReturn(p);
                        break;
                }
            }
            result.addAll(writer.getResult());
        }
        Path out = Paths.get(dir.getPath(), dir.getName() + ".asm");
        try (BufferedWriter writer = Files.newBufferedWriter(out)) {
            writer.append(String.join("\r\n", result));
        }
    }

    private static List<String> cleanArray(String text) {
        text = text.replaceAll("/\\*\\*[\\s\\S]*?\\*/", "");
        List<String> result = new ArrayList<>();
        String[] lines = text.split("\r\n");
        for (String line : lines) {
            line = line.replaceAll("//.*$", "");
            if (!line.isBlank()) {
                line = line.replaceAll("^\\s+", "");
                line = line.replaceAll("[\\t\\s]+$", "");
                result.add(line);

            }
        }
        return result;
    }
}

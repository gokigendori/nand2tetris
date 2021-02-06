package com.gokigendori.nand2tetris.assembler;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Assembler {
    private static Pattern aPattern = Pattern.compile("@[0-9a-zA-z_.$:]+");
    private static Pattern cPattern = Pattern.compile("(\\w+=)?([\\w-!+&|]+)+(;\\w+)?");
    private static Pattern lPattern = Pattern.compile("\\([a-zA-Z_.$:]+[0-9a-zA-Z_.$:]*\\)");
    private static Pattern symbolPattern = Pattern.compile("@[a-zA-Z_.$:]+[0-9a-zA-Z_.$:]*");

    public enum Command {
        A_COMMAND,
        C_COMMAND,
        L_COMMAND;
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            return;
        }
        Map<String, Integer> symbol = symbolTable();

        Path path = Paths.get(args[0]);
        String text = Files.readString(path);
        List<String> lines = cleanArray(text);
        int jump = 0;
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            Command type = commandType(line);

            if (type.equals(Command.L_COMMAND)) {
                String key = line.replaceAll("[()]", "");
                symbol.put(key, i - jump);
                jump++;
            }
        }
        int var = 16;
        for (String line : lines) {
            Command type = commandType(line);
            if (type.equals(Command.A_COMMAND)) {
                if (!symbol.containsKey(line.replaceAll("@", "")) && symbolPattern.matcher(line).matches()) {
                    String key = line.replaceAll("@", "");
                    symbol.put(key, var);
                    var++;
                }
            }

        }

        List<String> result = new ArrayList<>();
        for (String line : lines) {
            Command type = commandType(line);
            if (type.equals(Command.A_COMMAND)) {
                String v = line.replaceAll("@", "");
                if (line.matches("@[0-9]+")) {
                    result.add(formatInteger(Integer.parseInt(v)));
                } else if (symbol.containsKey(v)) {
                    result.add(formatInteger(symbol.get(v)));
                }
            } else if ((type.equals(Command.C_COMMAND))) {
                result.add(cParse(line).replaceAll(" ", ""));
            }
        }

        Path out = Paths.get(args[0].replaceAll(".asm", ".hack"));
        try (BufferedWriter writer = Files.newBufferedWriter(out)) {
            writer.append(String.join("\r\n", result));
        }
    }

    private static List<String> cleanArray(String text) {
        text = clean(text);
        List<String> result = new ArrayList<>();
        String[] lines = text.split("\r\n");
        for (String line : lines) {
            if (!line.isBlank()) {
                result.add(line.replaceAll(" ", ""));
            }
        }
        return result;
    }

    private static String formatInteger(Integer num) {
        String bin = Integer.toBinaryString(num);
        String formatted = String.format("%16s", bin).replaceAll(" ", "0");

        return formatted;

    }

    private static Map<String, Integer> symbolTable() {
        return new HashMap<>() {
            {
                put("SP", 0);
                put("LCL", 1);
                put("ARG", 2);
                put("THIS", 3);
                put("THAT", 4);
                put("R0", 0);
                put("R1", 1);
                put("R2", 2);
                put("R3", 3);
                put("R4", 4);
                put("R5", 5);
                put("R6", 6);
                put("R7", 7);
                put("R8", 8);
                put("R9", 9);
                put("R10", 10);
                put("R11", 11);
                put("R12", 12);
                put("R13", 13);
                put("R14", 14);
                put("R15", 15);
                put("SCREEN", 16384);
                put("KBD", 24576);
            }
        };
    }

    private static String jump(String j) {
        switch (j) {
            case "null":
                return "000";
            case "JGT":
                return "001";
            case "JEQ":
                return "010";
            case "JGE":
                return "011";
            case "JLT":
                return "100";
            case "JNE":
                return "101";
            case "JLE":
                return "110";
            case "JMP":
                return "111";
        }
        return "";
    }

    private static String dest(String d) {
        switch (d) {
            case "null":
                return "00 0";
            case "M":
                return "00 1";
            case "D":
                return "01 0";
            case "MD":
                return "01 1";
            case "A":
                return "10 0";
            case "AM":
                return "10 1";
            case "AD":
                return "11 0";
            case "AMD":
                return "11 1";
        }
        return "";
    }

    private static String comp(String s) {
        switch (s) {
            case "0":
                return "0 1010 10";
            case "1":
                return "0 1111 11";
            case "-1":
                return "0 1110 10";
            case "D":
                return "0 0011 00";
            case "A":
                return "0 1100 00";
            case "!D":
                return "0 0011 01";
            case "!A":
                return "0 1100 01";
            case "-D":
                return "0 0011 11";
            case "-A":
                return "0 1100 11";
            case "D+1":
                return "0 0111 11";
            case "A+1":
                return "0 1101 11";
            case "D-1":
                return "0 0011 10";
            case "A-1":
                return "0 1100 10";
            case "D+A":
                return "0 0000 10";
            case "D-A":
                return "0 0100 11";
            case "A-D":
                return "0 0001 11";
            case "D&A":
                return "0 0000 00";
            case "D|A":
                return "0 0101 01";
            case "M":
                return "1 1100 00";
            case "!M":
                return "1 1100 01";
            case "-M":
                return "1 1100 11";
            case "M+1":
                return "1 1101 11";
            case "M-1":
                return "1 1100 10";
            case "D+M":
                return "1 0000 10";
            case "D-M":
                return "1 0100 11";
            case "M-D":
                return "1 0001 11";
            case "D&M":
                return "1 0000 00";
            case "D|M":
                return "1 0101 01";
        }
        return "";
    }

    private static String cParse(String line) {
        Matcher matcher = cPattern.matcher(line);
        if (matcher.find()) {
            String d = matcher.group(1) != null ? matcher.group(1).replaceAll("=", "") : "null";
            String c = matcher.group(2);
            String j = matcher.group(3) != null ? matcher.group(3).replaceAll(";", "") : "null";
            return "111" + comp(c) + dest(d) + jump(j);
        }
        return "";
    }

    private static Command commandType(String line) {

        if (aPattern.matcher(line).matches()) {
            return Command.A_COMMAND;
        }
        if (cPattern.matcher(line).matches()) {
            return Command.C_COMMAND;
        }
        if (lPattern.matcher(line).matches()) {
            return Command.L_COMMAND;
        }
        return null;
    }

    private static String clean(String text) {
        text = text.replaceAll("//[^\r]*", "");
        text = text.replaceAll("/\\*\\*[\\s\\S]*?\\*/", "");
        text = text.replaceAll("(\r\n)+", "\r\n");
        return text;
    }
}
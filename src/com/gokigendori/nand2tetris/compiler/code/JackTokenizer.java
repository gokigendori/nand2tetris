package com.gokigendori.nand2tetris.compiler.code;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JackTokenizer {
    private final List<String> lines;

    public List<Terminal> getParsed() {
        return parsed;
    }

    private final List<Terminal> parsed;
    private final Path output;
    private final Pattern keywordPattern;
    private final Pattern symbolPattern;
    private final Pattern integerPattern;
    private final Pattern stringPattern;
    private final Pattern identifierPattern;

    public JackTokenizer(List<String> lines, Path output) {
        this.lines = lines;
        this.output = output;
        this.parsed = new ArrayList<>();

        List<String> keywords = new ArrayList<>();
        for (Keyword keyword : Keyword.values()) {
            keywords.add(keyword.getName());
        }
        keywordPattern = Pattern.compile(
                String.format("^(%s)\\W+", String.join("|", keywords))
        );
        symbolPattern = Pattern.compile("^([{}()\\[\\].,;+\\-*/&|<>=~])");
        integerPattern = Pattern.compile("^(\\d+)");
        stringPattern = Pattern.compile("^\"([^\\r\\n\"]+)\"");
        identifierPattern = Pattern.compile("^([a-zA-Z_][a-zA-Z0-9_]*)");

    }

    public void writeXml() throws IOException {
        List<String> results = new ArrayList<>();
        results.add("<tokens>");
        for (Terminal terminal : parsed) {
            String e = terminal.getType().getName();
            String line = String.format("<%s>%s</%s>", e, escape(terminal.getName()), e);
            results.add(line);
        }
        results.add("</tokens>");
        try (BufferedWriter writer = Files.newBufferedWriter(output)) {
            writer.append(String.join("\r\n", results));
        }
    }

    private String escape(String s) {
        s = s.replace("&", "&amp;");
        s = s.replace("<", "&lt;");
        s = s.replace(">", "&gt;");
        return s;
    }

    public void parse() {
        for (String line : lines) {
            while (!line.isBlank()) {
                line = line.replaceAll("^[\\t\\s]+", "");
                Terminal terminal = getTokenType(line);
                if (terminal != null) {
                    parsed.add(terminal);
                    String deleteString = terminal.getName();
                    if (terminal.getType().equals(TokenType.STRING_CONST)) {
                        deleteString = String.format("\"%s\"", terminal.getName());
                    }
                    int index = line.indexOf(deleteString);
                    line = line.substring(index + deleteString.length());
                }
            }
        }
        // 範囲外参照防止用
        parsed.add(new Terminal(TokenType.EOF, ""));

    }

    public Terminal getTokenType(String s) {

        Matcher matcher;
        matcher = keywordPattern.matcher(s);
        if (matcher.find()) {
            return new Terminal(TokenType.KEYWORD, matcher.group(1));
        }
        matcher = symbolPattern.matcher(s);
        if (matcher.find()) {
            return new Terminal(TokenType.SYMBOL, matcher.group(1));
        }
        matcher = integerPattern.matcher(s);
        if (matcher.find()) {
            return new Terminal(TokenType.INT_CONST, matcher.group(1));
        }
        matcher = identifierPattern.matcher(s);
        if (matcher.find()) {
            return new Terminal(TokenType.IDENTIFIER, matcher.group(1));
        }
        matcher = stringPattern.matcher(s);
        if (matcher.find()) {
            return new Terminal(TokenType.STRING_CONST, matcher.group(1));
        }
        return null;
    }

}

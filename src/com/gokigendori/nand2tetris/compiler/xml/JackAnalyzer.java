package com.gokigendori.nand2tetris.compiler.xml;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class JackAnalyzer {
    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            return;
        }
        File dir = new File(args[0]);
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (!file.isFile()) {
                continue;
            }
            if (!file.getName().matches(".*\\.jack")) {
//            if (!file.getName().matches("SquareGame\\.jack")) {
                continue;
            }
            String text = Files.readString(file.toPath());
            List<String> lines = cleanArray(text);
            String outputFile = String.format("%sTcmp.xml", file.getName().replaceFirst("\\.jack", ""));

            JackTokenizer tokenizer = new JackTokenizer(lines, Paths.get(dir.getPath(), outputFile));
            tokenizer.parse();
//            tokenizer.writeXml();

            String outputXmlFile = String.format("%sCmp.xml", file.getName().replaceFirst("\\.jack", ""));
            CompilationEngine compiler = new CompilationEngine(
                    tokenizer.getParsed(),
                    Paths.get(dir.getPath(), outputXmlFile)
            );
            compiler.process();
            compiler.writeXml();

        }
    }

    private static List<String> cleanArray(String text) {
        text = text.replaceAll("/\\*\\*[\\s\\S]*?\\*/", "");
        List<String> result = new ArrayList<>();
        String[] lines = text.split("\r?\n");
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

package com.gokigendori.nand2tetris.compiler.code;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CompilationEngine {
    private final List<Terminal> terminals;
    private final Path out;
    private final List<String> results;
    private final Set<String> op;
    private final Set<String> keywordConstant;
    private final SymbolTable classTable;
    private final SymbolTable subroutineTable;
    private final VMWriter vmWriter;
    private int currentPosition = 0;

    public CompilationEngine(List<Terminal> terminals, Path out, VMWriter vmWriter) {
        this.terminals = terminals;
        this.out = out;
        this.results = new ArrayList<>();
        this.op = new HashSet<>(Arrays.asList("+", "-", "*", "/", "&", "|", "<", ">", "="));
        this.keywordConstant = new HashSet<>(Arrays.asList("true", "false", "null", "this"));
        this.classTable = new SymbolTable();
        this.subroutineTable = new SymbolTable();
        this.vmWriter = vmWriter;
    }

    public void writeXml() throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(out)) {
            writer.append(String.join("\r\n", results));
        }
    }

    public void outputVm() throws IOException {
        this.vmWriter.output();
    }

    public void process() {
        compileClass();
    }

    private void compileClass() {
        results.add("<class>");
        writeTerminal(TokenType.KEYWORD);
        writeClassName();

        writeTerminal(TokenType.SYMBOL);
        int pre = currentPosition;
        compileClassVarDec();
        while (pre < currentPosition) {
            pre = currentPosition;
            compileClassVarDec();
        }

        pre = currentPosition;
        compileSubroutine();
        while (pre < currentPosition) {
            pre = currentPosition;
            compileSubroutine();
        }

        writeTerminal(TokenType.SYMBOL);
        results.add("</class>");
    }

    private void compileClassVarDec() {
        Terminal terminal = terminals.get(currentPosition);
        if (terminal.getType().equals(TokenType.KEYWORD)) {
            if (terminal.getName().equals("static") || terminal.getName().equals("field")) {
                results.add("<classVarDec>");
                writeTerminal(TokenType.KEYWORD);
                writeClassType();
                writeClassVarName();
                terminal = terminals.get(currentPosition);
                while (terminal.getType().equals(TokenType.SYMBOL) && terminal.getName().equals(",")) {
                    writeTerminal(TokenType.SYMBOL);
                    writeClassVarName();
                    terminal = terminals.get(currentPosition);
                }
                writeTerminal(TokenType.SYMBOL);
                results.add("</classVarDec>");
            }
        }
    }

    private void compileSubroutine() {
        Terminal terminal = terminals.get(currentPosition);
        if (terminal.getType().equals(TokenType.KEYWORD)) {
            if (
                    terminal.getName().equals("constructor") ||
                            terminal.getName().equals("function") ||
                            terminal.getName().equals("method")
            ) {
                results.add("<subroutineDec>");
                writeTerminal(TokenType.KEYWORD);
                writeReturnType();
                String subroutineName = terminals.get(currentPosition).getName();
                writeSubroutineName();
                writeTerminal(TokenType.SYMBOL);
                if (terminal.getName().equals("method")) {
                    // methodの第一引数はthisが渡される
                    subroutineTable.define("this", vmWriter.getClassName(), SymbolKind.valueByName("arg"));
                }
                compileParameterList();
                writeTerminal(TokenType.SYMBOL);
                writeSubroutineBody(terminal.getName(), subroutineName);
                results.add("</subroutineDec>");
            }
        }
    }

    private void compileParameterList() {
        results.add("<parameterList>");
        writeParameterType();
        writeParameter();
        Terminal next = terminals.get(currentPosition);
        while (next.getType().equals(TokenType.SYMBOL) && next.getName().equals(",")) {
            writeTerminal(TokenType.SYMBOL);
            writeParameterType();
            writeParameter();
            next = terminals.get(currentPosition);
        }
        results.add("</parameterList>");
    }

    private void writeSubroutineBody(String subroutineType, String subroutineName) {
        int localArgs = 0;
        results.add("<subroutineBody>");
        writeTerminal(TokenType.SYMBOL);
        int pre = currentPosition;
        localArgs += compileVarDec();
        while (pre < currentPosition) {
            pre = currentPosition;
            localArgs += compileVarDec();
        }

        vmWriter.writeFunction(subroutineName, localArgs);
        if (subroutineType.equals("constructor")) {
            int allocSize = classTable.varCount(SymbolKind.FIELD);
            vmWriter.writePush(Segment.CONSTANT, allocSize);
            vmWriter.writeCall("Memory.alloc", 1);
            vmWriter.writePop(Segment.POINTER, 0);
        }
        if (subroutineType.equals("method")) {
            // this
            vmWriter.writePush(Segment.ARG, 0);
            vmWriter.writePop(Segment.POINTER, 0);
        }
        pre = currentPosition;
        compileStatements();
        while (pre < currentPosition) {
            pre = currentPosition;
            compileStatements();
        }
        writeTerminal(TokenType.SYMBOL);
        results.add("</subroutineBody>");

    }

    private int compileVarDec() {
        Terminal terminal = terminals.get(currentPosition);
        int args = 0;
        if (terminal.getType().equals(TokenType.KEYWORD) &&
                terminal.getName().equals("var")) {
            results.add("<varDec>");
            writeTerminal(TokenType.KEYWORD);
            writeClassType();
            writeBodyVar();
            args++;
            terminal = terminals.get(currentPosition);
            while (terminal.getType().equals(TokenType.SYMBOL) && terminal.getName().equals(",")) {
                writeTerminal(TokenType.SYMBOL);
                writeBodyVar();
                args++;
                terminal = terminals.get(currentPosition);
            }
            writeTerminal(TokenType.SYMBOL);
            results.add("</varDec>");
        }
        return args;
    }

    private void compileStatements() {
        int preSize = results.size();

        int pre = currentPosition;
        writeStatement();

        while (pre < currentPosition) {
            pre = currentPosition;
            writeStatement();
        }
        if (preSize < results.size()) {
            // statementが追加されたときのみ囲む
            results.add(preSize, "<statements>");
            results.add("</statements>");
        }
    }

    private void writeStatement() {
        compileLet();
        compileIf();
        compileWhile();
        compileDo();
        compileReturn();
    }

    private void compileLet() {
        Terminal terminal = terminals.get(currentPosition);
        if (terminal.getType().equals(TokenType.KEYWORD) &&
                terminal.getName().equals("let")) {
            results.add("<letStatement>");
            writeTerminal(TokenType.KEYWORD);

            Map<String, String> map = writeVarName();
            if (terminals.get(currentPosition).getName().equals("[")) {
                // array
                Segment segment = mapKindToSegment(map.get("category"));
                writeTerminal(TokenType.SYMBOL);
                compileExpression();
                if (segment != null) {
                    vmWriter.writePush(segment, map.get("index"));
                }
                vmWriter.writeArithmetic(Command.ADD);
                writeTerminal(TokenType.SYMBOL);

                writeTerminal(TokenType.SYMBOL);
                compileExpression();
                // 右辺の値をtempに保存
                vmWriter.writePop(Segment.TEMP, 0);
                // 左辺のアドレスを設定後にtempをPUSH
                vmWriter.writePop(Segment.POINTER, 1);
                vmWriter.writePush(Segment.TEMP, 0);

                writeTerminal(TokenType.SYMBOL);
                vmWriter.writePop(Segment.THAT, 0);

            } else {
                // arg
                writeTerminal(TokenType.SYMBOL);
                compileExpression();
                writeTerminal(TokenType.SYMBOL);

                Segment segment = mapKindToSegment(map.get("category"));
                if (segment != null) {
                    vmWriter.writePop(segment, map.get("index"));
                }

            }
            results.add("</letStatement>");
        }
    }

    private void compileDo() {
        Terminal terminal = terminals.get(currentPosition);
        if (terminal.getType().equals(TokenType.KEYWORD) &&
                terminal.getName().equals("do")) {
            results.add("<doStatement>");
            writeTerminal(TokenType.KEYWORD);
            writeSubroutineCall();
            writeTerminal(TokenType.SYMBOL);
            // return
            vmWriter.writePop(Segment.TEMP, 0);
            results.add("</doStatement>");
        }
    }

    private void compileWhile() {
        int num = vmWriter.getWhileNum();
        String topLabel = String.format("WHILE_EXP%d", num);
        String endLabel = String.format("WHILE_END%d", num);
        Terminal terminal = terminals.get(currentPosition);
        if (terminal.getType().equals(TokenType.KEYWORD) &&
                terminal.getName().equals("while")) {
            vmWriter.incrementWhile();

            results.add("<whileStatement>");
            writeTerminal(TokenType.KEYWORD);
            writeTerminal(TokenType.SYMBOL);
            vmWriter.writeLabel(topLabel);

            compileExpression();
            vmWriter.writeArithmetic(Command.NOT);
            vmWriter.writeIf(endLabel);

            writeTerminal(TokenType.SYMBOL);
            writeTerminal(TokenType.SYMBOL);
            compileStatements();
            vmWriter.writeGoto(topLabel);
            vmWriter.writeLabel(endLabel);

            writeTerminal(TokenType.SYMBOL);
            results.add("</whileStatement>");
        }
    }

    private void compileReturn() {
        Terminal terminal = terminals.get(currentPosition);

        if (terminal.getType().equals(TokenType.KEYWORD) &&
                terminal.getName().equals("return")) {
            results.add("<returnStatement>");
            writeTerminal(TokenType.KEYWORD);
            int pre = currentPosition;
            compileExpression();
            if (pre == currentPosition) {
                vmWriter.writePush(Segment.CONSTANT, 0);
            }
            vmWriter.writeReturn();
            writeTerminal(TokenType.SYMBOL);
            results.add("</returnStatement>");
        }
    }

    private void compileIf() {
        int num = vmWriter.getIfNum();
        Terminal terminal = terminals.get(currentPosition);
        String trueLabel = String.format("IF_TRUE%d", num);
        String falseLabel = String.format("IF_FALSE%d", num);
        String endLabel = String.format("IF_END%d", num);

        if (terminal.getType().equals(TokenType.KEYWORD) &&
                terminal.getName().equals("if")) {
            vmWriter.incrementIf();

            results.add("<ifStatement>");
            writeTerminal(TokenType.KEYWORD);
            writeTerminal(TokenType.SYMBOL);
            compileExpression();
            writeTerminal(TokenType.SYMBOL);
            writeTerminal(TokenType.SYMBOL);

            vmWriter.writeIf(trueLabel);
            vmWriter.writeGoto(falseLabel);

            vmWriter.writeLabel(trueLabel);
            compileStatements();

            writeTerminal(TokenType.SYMBOL);

            if (terminals.get(currentPosition).getName().equals("else")) {
                vmWriter.writeGoto(endLabel);
                vmWriter.writeLabel(falseLabel);
                writeTerminal(TokenType.KEYWORD);
                writeTerminal(TokenType.SYMBOL);
                compileStatements();
                writeTerminal(TokenType.SYMBOL);
                vmWriter.writeLabel(endLabel);
            } else {
                vmWriter.writeLabel(falseLabel);
            }
            results.add("</ifStatement>");
        }
    }

    private void compileExpression() {
        int preSize = results.size();
        compileTerm();
        Terminal terminal = terminals.get(currentPosition);
        while (isOp(terminal)) {
            writeTerminal(TokenType.SYMBOL);
            compileTerm();
            if (isOsCommand(terminal)) {
                if (terminal.getName().equals("*")) {
                    vmWriter.writeCall("Math.multiply", 2);
                } else {
                    vmWriter.writeCall("Math.divide", 2);
                }
            } else {
                vmWriter.writeArithmetic(Command.valueBySymbol(terminal.getName()));
            }

            terminal = terminals.get(currentPosition);
        }
        if (preSize < results.size()) {
            // statementが追加されたときのみ囲む
            results.add(preSize, "<expression>");
            results.add("</expression>");
        }
    }

    private void compileTerm() {
        Terminal terminal = terminals.get(currentPosition);
        int preSize = results.size();

        if (terminal.getType().equals(TokenType.INT_CONST)) {
            vmWriter.writePush(Segment.CONSTANT, terminal.getName());
            writeTerminal(TokenType.INT_CONST);
        } else if (terminal.getType().equals(TokenType.STRING_CONST)) {
            char[] chars = terminal.getName().toCharArray();
            vmWriter.writePush(Segment.CONSTANT, chars.length);
            vmWriter.writeCall("String.new", 1);
            for (char c : chars) {
                vmWriter.writePush(Segment.CONSTANT, (int) c);
                vmWriter.writeCall("String.appendChar", 2);
            }
            writeTerminal(TokenType.STRING_CONST);
        } else if (isKeywordConstant(terminal)) {
            writeTerminal(TokenType.KEYWORD);
            switch (terminal.getName()) {
                case "true":
//                    vmWriter.writePush(Segment.CONSTANT, 1);
//                    vmWriter.writeArithmetic(Command.NEG);
                    vmWriter.writePush(Segment.CONSTANT, 0);
                    vmWriter.writeArithmetic(Command.NOT);
                    break;
                case "false":
                case "null":
                    vmWriter.writePush(Segment.CONSTANT, 0);
                    break;
                case "this":
                    vmWriter.writePush(Segment.POINTER, 0);
                    break;
            }
        } else if (terminal.getType().equals(TokenType.IDENTIFIER)) {
            Terminal next = terminals.get(currentPosition + 1);
            if (next.getName().equals("(") || next.getName().equals(".")) {
                // subroutine
                writeSubroutineCall();
            } else if (next.getName().equals("[")) {
                // varName []
                Map<String, String> map = writeVarName();
                Segment segment = mapKindToSegment(map.get("category"));

                writeTerminal(TokenType.SYMBOL);
                compileExpression();
                if (segment != null) {
                    vmWriter.writePush(segment, map.get("index"));
                }
                vmWriter.writeArithmetic(Command.ADD);
                vmWriter.writePop(Segment.POINTER, "1");
                // 配列読み込み
                vmWriter.writePush(Segment.THAT, 0);
                writeTerminal(TokenType.SYMBOL);
            } else {
                // varName
                Map<String, String> varInfo = writeVarName();
                vmWriter.writePush(mapKindToSegment(varInfo.get("category")), varInfo.get("index"));
            }
        } else if (terminal.getType().equals(TokenType.SYMBOL)) {
            if (terminal.getName().equals("(")) {
                writeTerminal(TokenType.SYMBOL);
                compileExpression();
                writeTerminal(TokenType.SYMBOL);
            } else if (isUnaryOp(terminal)) {
                writeTerminal(TokenType.SYMBOL);
                compileTerm();
                if (terminal.getName().equals("-")) {
                    vmWriter.writeArithmetic(Command.NEG);
                } else {
                    vmWriter.writeArithmetic(Command.valueBySymbol(terminal.getName()));
                }
            }
        }
        if (preSize < results.size()) {
            // statementが追加されたときのみ囲む
            results.add(preSize, "<term>");
            results.add("</term>");
        }

    }

    private boolean isUnaryOp(Terminal terminal) {
        return Arrays.asList("-", "~").contains(terminal.getName());
    }

    private boolean isOsCommand(Terminal terminal) {
        return Arrays.asList("*", "/").contains(terminal.getName());
    }

    private void writeSubroutineCall() {
        // constructor は alloc , k　個の　args
        // function は k 個の args
        // method は k+1 個の args

        Terminal next = terminals.get(currentPosition + 1);
        if (next.getName().equals("(")) {
            // method
            String subroutineName = terminals.get(currentPosition).getName();
            writeCalledSubroutineName();
            vmWriter.writePush(Segment.POINTER, 0);
            writeTerminal(TokenType.SYMBOL);
            int args = compileExpressionList();
            writeTerminal(TokenType.SYMBOL);
            vmWriter.writeCall(String.format("%s.%s", vmWriter.getClassName(), subroutineName), args + 1);
        } else if (next.getName().equals(".")) {
            String className = terminals.get(currentPosition).getName();
            Map<String, String> map = writeCalledClassName();

            writeTerminal(TokenType.SYMBOL);

            String subroutineName = terminals.get(currentPosition).getName();
            writeCalledSubroutineName();
            if (map.get("category").equals("class")) {
                // function , constructor
                writeTerminal(TokenType.SYMBOL);
                int args = compileExpressionList();
                writeTerminal(TokenType.SYMBOL);
                vmWriter.writeCall(String.format("%s.%s", className, subroutineName), args);
            } else {
                // method
                vmWriter.writePush(mapKindToSegment(map.get("category")), map.get("index"));
                writeTerminal(TokenType.SYMBOL);
                int args = compileExpressionList();
                writeTerminal(TokenType.SYMBOL);
                vmWriter.writeCall(String.format("%s.%s", map.get("type"), subroutineName), args + 1);
            }
        }
    }

    private int compileExpressionList() {
        results.add("<expressionList>");
        int pre = currentPosition;
        int nArgs = 0;
        compileExpression();
        if (pre < currentPosition) {
            nArgs++;
        }
        Terminal terminal = terminals.get(currentPosition);
        while (terminal.getName().equals(",")) {
            writeTerminal(TokenType.SYMBOL);
            compileExpression();
            nArgs++;
            terminal = terminals.get(currentPosition);
        }
        results.add("</expressionList>");
        return nArgs;
    }

    private boolean isOp(Terminal terminal) {
        return op.contains(terminal.getName());
    }

    private boolean isKeywordConstant(Terminal terminal) {
        return keywordConstant.contains(terminal.getName());
    }

    private void writeTerminal(TokenType type) {
        Terminal terminal = terminals.get(currentPosition);
        if (terminal.getType().equals(type)) {
            results.add(
                    String.format(
                            "<%s>%s</%s>",
                            type.getName(),
                            escape(terminal.getName()),
                            type.getName()
                    )
            );
            currentPosition++;
        }
    }

    private void writeIdentifier(Map<String, String> attr) {
        StringBuilder sb = new StringBuilder();
        for (String key : attr.keySet()) {
            sb.append(String.format(" \"%s\"=\"%s\" ", key, attr.get(key)));
        }
        Terminal terminal = terminals.get(currentPosition);
        if (!terminal.getType().equals(TokenType.IDENTIFIER)) {
            return;
        }
        results.add(
                String.format(
                        "<%s%s>%s</%s>",
                        TokenType.IDENTIFIER.getName(),
                        sb.toString(),
                        escape(terminal.getName()),
                        TokenType.IDENTIFIER.getName()
                )
        );
        currentPosition++;
    }

    private String escape(String s) {
        s = s.replace("&", "&amp;");
        s = s.replace("<", "&lt;");
        s = s.replace(">", "&gt;");
        return s;
    }

    private void writeClassType() {
        writeParameterType();
    }

    private void writeReturnType() {
        if (terminals.get(currentPosition).getName().equals("void")) {
            writeTerminal(TokenType.KEYWORD);
            return;
        }
        writeParameterType();
    }

    private Segment mapKindToSegment(String symbolKind) {
        switch (symbolKind) {
            case "static":
                return Segment.STATIC;
            case "field":
                return Segment.THIS;
            case "arg":
                return Segment.ARG;
            case "var":
                return Segment.LOCAL;
        }
        return null;
    }

    private void writeParameterType() {
        Terminal terminal = terminals.get(currentPosition);
        if (terminal.getType().equals(TokenType.KEYWORD)) {
            if (terminal.getName().equals("int") ||
                    terminal.getName().equals("char") ||
                    terminal.getName().equals("boolean")) {
                writeTerminal(TokenType.KEYWORD);
            }
        } else if (terminal.getType().equals(TokenType.IDENTIFIER)) {
            Map<String, String> map = new HashMap<>();
            map.put("category", "class");
            map.put("used", "true");
            writeIdentifier(map);
        }
    }

    private void writeClassVarName() {
        Terminal terminal = terminals.get(currentPosition);
        String type;
        SymbolKind attr;
        if (terminals.get(currentPosition - 1).getType().equals(TokenType.SYMBOL)) {
            // case: field int x,y;
            String pre = terminals.get(currentPosition - 2).getName();
            type = classTable.typeOf(pre);
            attr = classTable.kindOf(pre);
        } else {
            type = terminals.get(currentPosition - 1).getName();
            attr = SymbolKind.valueByName(terminals.get(currentPosition - 2).getName());
        }
        classTable.define(terminal.getName(), type, attr);

        Map<String, String> map = new HashMap<>();
        map.put("category", classTable.kindOf(terminal.getName()).getName());
        map.put("defined", "true");
        map.put("type", classTable.typeOf(terminal.getName()));
        map.put("index", String.valueOf(classTable.indexOf(terminal.getName())));

        writeIdentifier(map);
    }

    private void writeParameter() {
        Terminal terminal = terminals.get(currentPosition);
        String name = terminal.getName();
        if (!terminal.getType().equals(TokenType.IDENTIFIER)) {
            return;
        }
        subroutineTable.define(name, terminals.get(currentPosition - 1).getName(), SymbolKind.valueByName("arg"));

        Map<String, String> map = new HashMap<>();
        map.put("category", getTableKind(name));
        map.put("defined", "true");
        map.put("type", getTableType(name));
        map.put("index", getTableIndex(name));

        writeIdentifier(map);
    }

    private void writeBodyVar() {
        Terminal terminal = terminals.get(currentPosition);
        String name = terminal.getName();
        if (!terminal.getType().equals(TokenType.IDENTIFIER)) {
            return;
        }
        String type;
        SymbolKind attr;
        if (terminals.get(currentPosition - 1).getType().equals(TokenType.SYMBOL)) {
            // case: field int x,y;
            String pre = terminals.get(currentPosition - 2).getName();
            type = subroutineTable.typeOf(pre);
            attr = subroutineTable.kindOf(pre);
        } else {
            type = terminals.get(currentPosition - 1).getName();
            attr = SymbolKind.valueByName(terminals.get(currentPosition - 2).getName());
        }
        subroutineTable.define(name, type, attr);

        Map<String, String> map = new HashMap<>();
        map.put("category", getTableKind(name));
        map.put("defined", "true");
        map.put("type", getTableType(name));
        map.put("index", getTableIndex(name));

        writeIdentifier(map);
    }

    private Map<String, String> writeVarName() {
        String name = terminals.get(currentPosition).getName();
        Map<String, String> map = new HashMap<>();
        map.put("category", getTableKind(name));
        map.put("used", "true");
        map.put("type", getTableType(name));
        map.put("index", getTableIndex(name));

        writeIdentifier(map);
        return map;
    }

    public String getTableKind(String name) {
        if (subroutineTable.kindOf(name).equals(SymbolKind.NONE)) {
            return classTable.kindOf(name).getName();
        }
        return subroutineTable.kindOf(name).getName();
    }

    public String getTableType(String name) {
        if (subroutineTable.kindOf(name).equals(SymbolKind.NONE)) {
            return classTable.typeOf(name);
        }
        return subroutineTable.typeOf(name);
    }

    public String getTableIndex(String name) {
        if (subroutineTable.kindOf(name).equals(SymbolKind.NONE)) {
            return String.valueOf(classTable.indexOf(name));
        }
        return String.valueOf(subroutineTable.indexOf(name));
    }

    private void writeSubroutineName() {
        subroutineTable.startSubroutine();

        Map<String, String> map = new HashMap<>();
        map.put("category", "subroutine");
        map.put("defined", "true");
        writeIdentifier(map);
    }

    private void writeCalledSubroutineName() {
        Map<String, String> map = new HashMap<>();
        map.put("category", "subroutine");
        map.put("used", "true");
        writeIdentifier(map);
    }

    private Map<String, String> writeCalledClassName() {
        String name = terminals.get(currentPosition).getName();
        Map<String, String> map = new HashMap<>();
        if (getTableKind(name).equals(SymbolKind.NONE.getName())) {
            map.put("category", "class");
            map.put("used", "true");
        } else {
            map.put("category", getTableKind(name));
            map.put("used", "true");
            map.put("type", getTableType(name));
            map.put("index", getTableIndex(name));
        }
        writeIdentifier(map);
        return map;
    }

    private void writeClassName() {
        Map<String, String> map = new HashMap<>();
        map.put("category", "class");
        map.put("defined", "true");
        writeIdentifier(map);
    }
}

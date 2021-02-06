package com.gokigendori.nand2tetris.compiler.xml;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CompilationEngine {
    private final List<Terminal> terminals;
    private final Path out;
    private final List<String> results;
    private final Set<String> op;
    private final Set<String> keywordConstant;

    public CompilationEngine(List<Terminal> terminals, Path out) {
        this.terminals = terminals;
        this.out = out;
        this.results = new ArrayList<>();
        this.op = new HashSet<>(Arrays.asList("+", "-", "*", "/", "&", "|", "<", ">", "="));
        this.keywordConstant = new HashSet<>(Arrays.asList("true", "false", "null", "this"));
    }

    public void writeXml() throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(out)) {
            writer.append(String.join("\r\n", results));
        }
    }

    public void process() {
        compileClass();
    }

    private void compileClass() {
        results.add("<class>");
        int pos = 0;
        pos = writeTerminal(pos, TokenType.KEYWORD);
        pos = writeClassName(pos);
        pos = writeTerminal(pos, TokenType.SYMBOL);

        int next = compileClassVarDec(pos);
        while (pos < next) {
            pos = next;
            next = compileClassVarDec(pos);
        }

        next = compileSubroutine(pos);
        while (pos < next) {
            pos = next;
            next = compileSubroutine(pos);
        }

        writeTerminal(pos, TokenType.SYMBOL);
        results.add("</class>");
    }

    private int compileClassVarDec(int pos) {
        Terminal terminal = terminals.get(pos);
        if (terminal.getType().equals(TokenType.KEYWORD)) {
            if (terminal.getName().equals("static") || terminal.getName().equals("field")) {
                results.add("<classVarDec>");
                pos = writeTerminal(pos, TokenType.KEYWORD);
                pos = writeType(pos);
                pos = writeVarName(pos);
                terminal = terminals.get(pos);
                while (terminal.getType().equals(TokenType.SYMBOL) && terminal.getName().equals(",")) {
                    pos = writeTerminal(pos, TokenType.SYMBOL);
                    pos = writeVarName(pos);
                    terminal = terminals.get(pos);
                }
                pos = writeTerminal(pos, TokenType.SYMBOL);
                results.add("</classVarDec>");
            }
        }
        return pos;
    }

    private int compileSubroutine(int pos) {
        Terminal terminal = terminals.get(pos);
        if (terminal.getType().equals(TokenType.KEYWORD)) {
            if (
                    terminal.getName().equals("constructor") ||
                            terminal.getName().equals("function") ||
                            terminal.getName().equals("method")
            ) {
                results.add("<subroutineDec>");
                pos = writeTerminal(pos, TokenType.KEYWORD);
                terminal = terminals.get(pos);
                if (terminal.getType().equals(TokenType.KEYWORD)) {
                    pos = writeTerminal(pos, TokenType.KEYWORD);
                } else {
                    pos = writeType(pos);
                }
                pos = writeSubroutineName(pos);
                pos = writeTerminal(pos, TokenType.SYMBOL);
                pos = compileParameterList(pos);
                pos = writeTerminal(pos, TokenType.SYMBOL);
                pos = writeSubroutineBody(pos);
                results.add("</subroutineDec>");
            }
        }
        return pos;
    }

    private int compileParameterList(int pos) {
        results.add("<parameterList>");
        pos = writeType(pos);
        pos = writeVarName(pos);
        Terminal next = terminals.get(pos);
        while (next.getType().equals(TokenType.SYMBOL) && next.getName().equals(",")) {
            pos = writeTerminal(pos, TokenType.SYMBOL);
            pos = writeType(pos);
            pos = writeVarName(pos);
            next = terminals.get(pos);
        }
        results.add("</parameterList>");
        return pos;
    }

    private int writeSubroutineBody(int pos) {
        results.add("<subroutineBody>");
        pos = writeTerminal(pos, TokenType.SYMBOL);
        int next = compileVarDec(pos);
        while (pos < next) {
            pos = next;
            next = compileVarDec(pos);
        }
        next = compileStatements(pos);
        while (pos < next) {
            pos = next;
            next = compileStatements(pos);
        }
        pos = writeTerminal(pos, TokenType.SYMBOL);
        results.add("</subroutineBody>");
        return pos;
    }


    private int compileVarDec(int pos) {
        Terminal terminal = terminals.get(pos);

        if (terminal.getType().equals(TokenType.KEYWORD) &&
                terminal.getName().equals("var")) {
            results.add("<varDec>");
            pos = writeTerminal(pos, TokenType.KEYWORD);
            pos = writeType(pos);
            pos = writeVarName(pos);
            terminal = terminals.get(pos);
            while (terminal.getType().equals(TokenType.SYMBOL) && terminal.getName().equals(",")) {
                pos = writeTerminal(pos, TokenType.SYMBOL);
                pos = writeVarName(pos);
                terminal = terminals.get(pos);
            }
            pos = writeTerminal(pos, TokenType.SYMBOL);
            results.add("</varDec>");
        }
        return pos;
    }

    private int compileStatements(int pos) {
        int pre = results.size();
        boolean exists = false;
        int next = writeStatement(pos);
        while (pos < next) {
            pos = next;
            exists = true;
            next = writeStatement(pos);
        }
        if (exists) {
            // statementが追加されたときのみ囲む
            results.add(pre, "<statements>");
            results.add("</statements>");
        }
        return pos;
    }

    private int writeStatement(int pos) {
        pos = compileLet(pos);
        pos = compileIf(pos);
        pos = compileWhile(pos);
        pos = compileDo(pos);
        pos = compileReturn(pos);
        return pos;
    }

    private int compileLet(int pos) {
        Terminal terminal = terminals.get(pos);
        if (terminal.getType().equals(TokenType.KEYWORD) &&
                terminal.getName().equals("let")) {
            results.add("<letStatement>");
            pos = writeTerminal(pos, TokenType.KEYWORD);
            pos = writeVarName(pos);
            if (terminals.get(pos).getName().equals("[")) {
                pos = writeTerminal(pos, TokenType.SYMBOL);
                pos = compileExpression(pos);
                pos = writeTerminal(pos, TokenType.SYMBOL);
            }
            pos = writeTerminal(pos, TokenType.SYMBOL);
            pos = compileExpression(pos);
            pos = writeTerminal(pos, TokenType.SYMBOL);
            results.add("</letStatement>");
        }
        return pos;
    }

    private int compileDo(int pos) {
        Terminal terminal = terminals.get(pos);
        if (terminal.getType().equals(TokenType.KEYWORD) &&
                terminal.getName().equals("do")) {
            results.add("<doStatement>");
            pos = writeTerminal(pos, TokenType.KEYWORD);
            pos = writeSubroutineCall(pos);
            pos = writeTerminal(pos, TokenType.SYMBOL);
            results.add("</doStatement>");
        }
        return pos;
    }


    private int compileWhile(int pos) {
        Terminal terminal = terminals.get(pos);
        if (terminal.getType().equals(TokenType.KEYWORD) &&
                terminal.getName().equals("while")) {
            results.add("<whileStatement>");
            pos = writeTerminal(pos, TokenType.KEYWORD);
            pos = writeTerminal(pos, TokenType.SYMBOL);
            pos = compileExpression(pos);
            pos = writeTerminal(pos, TokenType.SYMBOL);
            pos = writeTerminal(pos, TokenType.SYMBOL);
            pos = compileStatements(pos);
            pos = writeTerminal(pos, TokenType.SYMBOL);
            results.add("</whileStatement>");
        }
        return pos;
    }

    private int compileReturn(int pos) {
        Terminal terminal = terminals.get(pos);
        if (terminal.getType().equals(TokenType.KEYWORD) &&
                terminal.getName().equals("return")) {
            results.add("<returnStatement>");
            pos = writeTerminal(pos, TokenType.KEYWORD);
            pos = compileExpression(pos);
            pos = writeTerminal(pos, TokenType.SYMBOL);
            results.add("</returnStatement>");
        }
        return pos;
    }

    private int compileIf(int pos) {
        Terminal terminal = terminals.get(pos);
        if (terminal.getType().equals(TokenType.KEYWORD) &&
                terminal.getName().equals("if")) {
            results.add("<ifStatement>");
            pos = writeTerminal(pos, TokenType.KEYWORD);
            pos = writeTerminal(pos, TokenType.SYMBOL);
            pos = compileExpression(pos);
            pos = writeTerminal(pos, TokenType.SYMBOL);
            pos = writeTerminal(pos, TokenType.SYMBOL);
            pos = compileStatements(pos);
            pos = writeTerminal(pos, TokenType.SYMBOL);
            if (terminals.get(pos).getName().equals("else")) {
                pos = writeTerminal(pos, TokenType.KEYWORD);
                pos = writeTerminal(pos, TokenType.SYMBOL);
                pos = compileStatements(pos);
                pos = writeTerminal(pos, TokenType.SYMBOL);
            }
            results.add("</ifStatement>");
        }
        return pos;
    }

    private int compileExpression(int pos) {
        int tmp = results.size();
        int pre = pos;

        pos = compileTerm(pos);
        Terminal terminal = terminals.get(pos);
        while (isOp(terminal)) {
            pos = writeTerminal(pos, TokenType.SYMBOL);
            pos = compileTerm(pos);
            terminal = terminals.get(pos);
        }
        if (pre < pos) {
            // statementが追加されたときのみ囲む
            results.add(tmp, "<expression>");
            results.add("</expression>");
        }
        return pos;
    }

    private int compileTerm(int pos) {
        Terminal terminal = terminals.get(pos);
        int tmp = results.size();
        int pre = pos;

        if (terminal.getType().equals(TokenType.INT_CONST)) {
            pos = writeTerminal(pos, TokenType.INT_CONST);
        } else if (terminal.getType().equals(TokenType.STRING_CONST)) {
            pos = writeTerminal(pos, TokenType.STRING_CONST);
        } else if (isKeywordConstant(terminal)) {
            pos = writeTerminal(pos, TokenType.KEYWORD);
        } else if (terminal.getType().equals(TokenType.IDENTIFIER)) {
            Terminal next = terminals.get(pos + 1);
            if (next.getName().equals("(") || next.getName().equals(".")) {
                // subroutine
                pos = writeSubroutineCall(pos);
            } else if (next.getName().equals("[")) {
                // varName []
                pos = writeVarName(pos);
                pos = writeTerminal(pos, TokenType.SYMBOL);
                pos = compileExpression(pos);
                pos = writeTerminal(pos, TokenType.SYMBOL);
            } else {
                // varName
                pos = writeVarName(pos);
            }
        } else if (terminal.getType().equals(TokenType.SYMBOL)) {
            if (terminal.getName().equals("(")) {
                pos = writeTerminal(pos, TokenType.SYMBOL);
                pos = compileExpression(pos);
                pos = writeTerminal(pos, TokenType.SYMBOL);
            } else if (isUnaryOp(terminal)) {
                pos = writeTerminal(pos, TokenType.SYMBOL);
                pos = compileTerm(pos);
            }
        }
        if (pre < pos) {
            // statementが追加されたときのみ囲む
            results.add(tmp, "<term>");
            results.add("</term>");
        }
        return pos;
    }

    private boolean isUnaryOp(Terminal terminal) {
        return Arrays.asList("-", "~").contains(terminal.getName());
    }

    private int writeSubroutineCall(int pos) {
        Terminal next = terminals.get(pos + 1);
        if (next.getName().equals("(")) {
            pos = writeSubroutineName(pos);
            pos = writeTerminal(pos, TokenType.SYMBOL);
            pos = compileExpressionList(pos);
            pos = writeTerminal(pos, TokenType.SYMBOL);
        } else if (next.getName().equals(".")) {
            pos = writeClassName(pos);
            pos = writeTerminal(pos, TokenType.SYMBOL);
            pos = writeSubroutineName(pos);
            pos = writeTerminal(pos, TokenType.SYMBOL);
            pos = compileExpressionList(pos);
            pos = writeTerminal(pos, TokenType.SYMBOL);
        }
        return pos;
    }

    private int compileExpressionList(int pos) {
        results.add("<expressionList>");
        pos = compileExpression(pos);
        Terminal terminal = terminals.get(pos);
        while (terminal.getName().equals(",")) {
            pos = writeTerminal(pos, TokenType.SYMBOL);
            pos = compileExpression(pos);
            terminal = terminals.get(pos);
        }
        results.add("</expressionList>");
        return pos;
    }

    private boolean isOp(Terminal terminal) {
        return op.contains(terminal.getName());
    }

    private boolean isKeywordConstant(Terminal terminal) {
        return keywordConstant.contains(terminal.getName());
    }

    private int writeTerminal(int pos, TokenType type) {
        Terminal terminal = terminals.get(pos);
        if (terminal.getType().equals(type)) {
            results.add(
                    String.format(
                            "<%s>%s</%s>",
                            type.getName(),
                            escape(terminal.getName()),
                            type.getName()
                    )
            );
            pos++;
        }
        return pos;
    }

    private String escape(String s) {
        s = s.replace("&", "&amp;");
        s = s.replace("<", "&lt;");
        s = s.replace(">", "&gt;");
        return s;
    }

    private int writeType(int pos) {
        Terminal terminal = terminals.get(pos);
        if (terminal.getType().equals(TokenType.KEYWORD)) {
            if (terminal.getName().equals("int") ||
                    terminal.getName().equals("char") ||
                    terminal.getName().equals("boolean")) {
                pos = writeTerminal(pos, TokenType.KEYWORD);
            }
        } else if (terminal.getType().equals(TokenType.IDENTIFIER)) {
            pos = writeTerminal(pos, TokenType.IDENTIFIER);
        }
        return pos;
    }


    private int writeVarName(int pos) {
        Terminal terminal = terminals.get(pos);
        if (terminal.getType().equals(TokenType.IDENTIFIER)) {
            pos = writeTerminal(pos, TokenType.IDENTIFIER);
        }
        return pos;
    }

    private int writeSubroutineName(int pos) {
        return writeVarName(pos);
    }

    private int writeClassName(int pos) {
        return writeVarName(pos);
    }

}

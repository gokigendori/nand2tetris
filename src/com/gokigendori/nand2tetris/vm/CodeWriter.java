package com.gokigendori.nand2tetris.vm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CodeWriter {

    private final String fileName;

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    private String functionName;

    private Map<String, String> labelMap;

    private List<String> result;

    public CodeWriter(String fileName) {
        this.fileName = fileName;
        result = new ArrayList<>();
        labelMap = new HashMap<>();
    }

    public List<String> getResult() {
        return result;
    }

    public void addLabelMap(Parser parser) {
        labelMap.put(parser.getArg1(), String.format("%s$%s", functionName, parser.getArg1()));
    }

    public void writeLabel(Parser parser) {
        result.add(String.format("(%s)", labelMap.get(parser.getArg1())));
    }

    public void writeFunction(Parser parser) {
        int pos = getPos();
        String functionLabel = String.format("%s", functionName);
        result.add(String.format("(%s)", functionLabel));

        result.add("@" + parser.getArg2());
        result.add("D=A-1");
        result.add("@INIT_END_" + getLoopSymbol(pos));
        result.add("D;JLT");
        // local 初期化
        result.add(String.format("(LOOP_INIT_%s)", getLoopSymbol(pos)));
        result.add("@LCL");
        result.add("A=M+D");
        result.add("M=0");
        result.add("D=D-1");
        result.add("@SP");
        result.add("M=M+1");
        result.add("@LOOP_INIT_" + getLoopSymbol(pos));
        result.add("D;JGE");
        result.add(String.format("(INIT_END_%s)", getLoopSymbol(pos)));
    }

    public void writeReturn(Parser parser) {
        int pos = getPos();
        // 戻り値
        result.add("@SP");
        result.add("M=M-1");
        result.add("A=M");
        result.add("D=M");
        result.add("@return.value." + getSymbol(pos));
        result.add("M=D");

        result.add("@ARG");
        result.add("D=M");
        result.add("@return.sp." + getSymbol(pos));
        result.add("M=D+1");

        // return
        result.add("@LCL");
        result.add("D=M");
        result.add("@5");
        result.add("D=D-A");
        result.add("A=D");
        result.add("D=M");
        result.add("@return.callback." + getSymbol(pos));
        result.add("M=D");

        // THAT
        result.add("@LCL");
        result.add("D=M");
        result.add("A=D-1");
        result.add("D=M");
        result.add("@THAT");
        result.add("M=D");

        // THIS
        result.add("@LCL");
        result.add("D=M");
        result.add("@2");
        result.add("D=D-A");
        result.add("A=D");
        result.add("D=M");
        result.add("@THIS");
        result.add("M=D");

        // ARG
        result.add("@LCL");
        result.add("D=M");
        result.add("@3");
        result.add("D=D-A");
        result.add("A=D");
        result.add("D=M");
        result.add("@ARG");
        result.add("M=D");

        // LCL
        result.add("@LCL");
        result.add("D=M");
        result.add("@4");
        result.add("D=D-A");
        result.add("A=D");
        result.add("D=M");
        result.add("@LCL");
        result.add("M=D");

        // 戻り値設定
        result.add("@return.sp." + getSymbol(pos));
        result.add("D=M");
        result.add("@SP");
        result.add("M=D");

        result.add("@return.value." + getSymbol(pos));
        result.add("D=M");
        result.add("@SP");
        result.add("A=M-1");
        result.add("M=D");

        // return
        result.add("@return.callback." + getSymbol(pos));
        result.add("A=M");
        result.add("0;JMP");
    }

    public void writeCall(Parser parser) {
        String functionName = parser.getArg1();
        Integer argsNum = parser.getArg2();

        String gotoSymbol = String.format("%s", functionName);
        String returnSymbol = String.format("return-%s", getPos());
        // return addressを格納
        result.add("@" + returnSymbol);
        result.add("D=A");
        result.add("@SP");
        result.add("A=M");
        result.add("M=D");
        result.add("@SP");
        result.add("M=M+1");

        result.add("@LCL");
        result.add("D=M");
        result.add("@SP");
        result.add("A=M");
        result.add("M=D");
        result.add("@SP");
        result.add("M=M+1");

        result.add("@ARG");
        result.add("D=M");
        result.add("@SP");
        result.add("A=M");
        result.add("M=D");
        result.add("@SP");
        result.add("M=M+1");

        result.add("@THIS");
        result.add("D=M");
        result.add("@SP");
        result.add("A=M");
        result.add("M=D");
        result.add("@SP");
        result.add("M=M+1");

        result.add("@THAT");
        result.add("D=M");
        result.add("@SP");
        result.add("A=M");
        result.add("M=D");
        result.add("@SP");
        result.add("M=M+1");

        // ARG
        result.add("@SP");
        result.add("D=M");
        result.add("@5");
        result.add("D=D-A");
        result.add("@" + argsNum);
        result.add("D=D-A");
        result.add("@ARG");
        result.add("M=D");

        // LCL
        result.add("@SP");
        result.add("D=M");
        result.add("@LCL");
        result.add("M=D");

        // jump
        result.add("@" + gotoSymbol);
        result.add("0;JMP");

        // return address label
        result.add(String.format("(%s)", returnSymbol));
    }

    public void writeGoto(Parser parser) {
        result.add("@" + labelMap.get(parser.getArg1()));
        result.add("0;JMP");
    }

    public void writeIf(Parser parser) {
        result.add("@SP");
        result.add("M=M-1");
        result.add("A=M");
        result.add("D=M");
        result.add("@" + labelMap.get(parser.getArg1()));
        result.add("D;JNE");
    }

    public void writePush(Parser p) {
        switch (p.getSegment()) {
            case CONSTANT:
                result.add("@" + p.getArg2());
                result.add("D=A");
                result.add("@SP");
                result.add("A=M");
                result.add("M=D");
                result.add("@SP");
                result.add("M=M+1");
                break;
            case LOCAL:
                result.add("@" + p.getArg2());
                result.add("D=A");
                result.add("@LCL");
                result.add("A=M+D");
                result.add("D=M");
                result.add("@SP");
                result.add("A=M");
                result.add("M=D");
                result.add("@SP");
                result.add("M=M+1");
                break;
            case ARGUMENT:
                result.add("@" + p.getArg2());
                result.add("D=A");
                result.add("@ARG");
                result.add("A=M+D");
                result.add("D=M");
                result.add("@SP");
                result.add("A=M");
                result.add("M=D");
                result.add("@SP");
                result.add("M=M+1");
                break;
            case POINTER:
                if (p.getArg2() == 0) {
                    result.add("@THIS");
                    result.add("D=M");
                    result.add("@SP");
                    result.add("A=M");
                    result.add("M=D");
                    result.add("@SP");
                    result.add("M=M+1");
                } else if (p.getArg2() == 1) {
                    result.add("@THAT");
                    result.add("D=M");
                    result.add("@SP");
                    result.add("A=M");
                    result.add("M=D");
                    result.add("@SP");
                    result.add("M=M+1");
                }
                break;
            case THIS:
                result.add("@" + p.getArg2());
                result.add("D=A");
                result.add("@THIS");
                result.add("A=M+D");
                result.add("D=M");
                result.add("@SP");
                result.add("A=M");
                result.add("M=D");
                result.add("@SP");
                result.add("M=M+1");
                break;
            case THAT:
                result.add("@" + p.getArg2());
                result.add("D=A");
                result.add("@THAT");
                result.add("A=M+D");
                result.add("D=M");
                result.add("@SP");
                result.add("A=M");
                result.add("M=D");
                result.add("@SP");
                result.add("M=M+1");
                break;
            case TEMP:
                result.add("@5");
                result.add("D=A");
                result.add("@" + p.getArg2());
                result.add("D=D+A");
                result.add("A=D");
                result.add("D=M");
                result.add("@SP");
                result.add("A=M");
                result.add("M=D");
                result.add("@SP");
                result.add("M=M+1");
                break;
            case STATIC:
                result.add(String.format("@%s.%d", fileName, p.getArg2()));
                result.add("D=M");
                result.add("@SP");
                result.add("A=M");
                result.add("M=D");
                result.add("@SP");
                result.add("M=M+1");
                break;
        }
    }

    public void writePop(Parser p) {
        int pos = getPos();

        switch (p.getSegment()) {
            case CONSTANT:
                break;
            case POINTER:
                // THIS,THATのベースアドレス設定
                if (p.getArg2() == 0) {
                    result.add("@SP");
                    result.add("M=M-1");
                    result.add("A=M");
                    result.add("D=M");
                    result.add("@THIS");
                    result.add("M=D");
                } else if (p.getArg2() == 1) {
                    result.add("@SP");
                    result.add("M=M-1");
                    result.add("A=M");
                    result.add("D=M");
                    result.add("@THAT");
                    result.add("M=D");
                }
                break;
            case THIS:
                result.add("@" + p.getArg2());
                result.add("D=A");
                result.add("@THIS");
                result.add("D=M+D");
                result.add("@tmp.address." + getSymbol(pos));
                result.add("M=D");
                result.add("@SP");
                result.add("M=M-1");
                result.add("A=M");
                result.add("D=M");
                result.add("@tmp.address." + getSymbol(pos));
                result.add("A=M");
                result.add("M=D");
                break;
            case LOCAL:
                result.add("@" + p.getArg2());
                result.add("D=A");
                result.add("@LCL");
                result.add("D=M+D");
                result.add("@tmp.address." + getSymbol(pos));
                result.add("M=D");
                result.add("@SP");
                result.add("M=M-1");
                result.add("A=M");
                result.add("D=M");
                result.add("@tmp.address." + getSymbol(pos));
                result.add("A=M");
                result.add("M=D");
                break;
            case ARGUMENT:
                result.add("@" + p.getArg2());
                result.add("D=A");
                result.add("@ARG");
                result.add("D=M+D");
                result.add("@tmp.address." + getSymbol(pos));
                result.add("M=D");
                result.add("@SP");
                result.add("M=M-1");
                result.add("A=M");
                result.add("D=M");
                result.add("@tmp.address." + getSymbol(pos));
                result.add("A=M");
                result.add("M=D");
                break;
            case THAT:
                result.add("@" + p.getArg2());
                result.add("D=A");
                result.add("@THAT");
                result.add("D=M+D");
                result.add("@tmp.address." + getSymbol(pos));
                result.add("M=D");
                result.add("@SP");
                result.add("M=M-1");
                result.add("A=M");
                result.add("D=M");
                result.add("@tmp.address." + getSymbol(pos));
                result.add("A=M");
                result.add("M=D");
                break;
            case TEMP:
                result.add("@5");
                result.add("D=A");
                result.add("@" + p.getArg2());
                result.add("D=D+A");
                result.add("@tmp.address." + getSymbol(pos));
                result.add("M=D");
                result.add("@SP");
                result.add("M=M-1");
                result.add("A=M");
                result.add("D=M");
                result.add("@tmp.address." + getSymbol(pos));
                result.add("A=M");
                result.add("M=D");
                break;
            case STATIC:
                result.add("@SP");
                result.add("M=M-1");
                result.add("A=M");
                result.add("D=M");
                // 変数シンボル
                result.add(String.format("@%s.%d", fileName, p.getArg2()));
                result.add("M=D");
                break;
        }

    }

    public void writeArithmetic(Parser parser) {
        Integer pos = getPos();
        switch (parser.getArg1()) {
            case "not":
                result.add("@SP");
                result.add("A=M-1");
                result.add("M=!M");
                break;
            case "neg":
                result.add("@SP");
                result.add("A=M-1");
                result.add("M=-M");
                break;
            case "add":
                result.add("@SP");
                result.add("M=M-1");
                result.add("A=M");
                result.add("D=M");
                result.add("@SP");
                result.add("A=M-1");
                result.add("M=M+D");
                break;
            case "sub":
                result.add("@SP");
                result.add("M=M-1");
                result.add("A=M");
                result.add("D=M");
                result.add("@SP");
                result.add("A=M-1");
                result.add("M=M-D");
                break;
            case "and":
                result.add("@SP");
                result.add("M=M-1");
                result.add("A=M");
                result.add("D=M");
                result.add("@SP");
                result.add("A=M-1");
                result.add("M=D&M");
                break;
            case "or":
                result.add("@SP");
                result.add("M=M-1");
                result.add("A=M");
                result.add("D=M");
                result.add("@SP");
                result.add("A=M-1");
                result.add("M=D|M");
                break;
            case "eq":
                result.add("@SP");
                result.add("M=M-1");
                result.add("A=M");
                result.add("D=M");
                result.add("@SP");
                result.add("A=M-1");
                result.add("D=M-D");
                result.add("@LOOP_EQ_TRUE_" + getLoopSymbol(pos));
                result.add("D;JEQ");
                result.add("@SP");
                result.add("A=M-1");
                result.add("M=0");
                result.add("@RESTART_EQ_" + getLoopSymbol(pos));
                result.add("0;JMP");
                result.add("(LOOP_EQ_TRUE_" + getLoopSymbol(pos) + ")");
                result.add("@SP");
                result.add("A=M-1");
                result.add("M=-1");
                result.add("(RESTART_EQ_" + getLoopSymbol(pos) + ")");
                break;
            case "lt":
                result.add("@SP");
                result.add("M=M-1");
                result.add("A=M");
                result.add("D=M");
                result.add("@SP");
                result.add("A=M-1");
                // x-y
                result.add("D=M-D");
                result.add("@LOOP_LT_TRUE_" + getLoopSymbol(pos));
                result.add("D;JLT");
                result.add("@SP");
                result.add("A=M-1");
                result.add("M=0");
                result.add("@RESTART_LT_" + getLoopSymbol(pos));
                result.add("0;JMP");
                result.add("(LOOP_LT_TRUE_" + getLoopSymbol(pos) + ")");
                result.add("@SP");
                result.add("A=M-1");
                result.add("M=-1");
                result.add("(RESTART_LT_" + getLoopSymbol(pos) + ")");
                break;
            case "gt":
                result.add("@SP");
                result.add("M=M-1");
                result.add("A=M");
                result.add("D=M");
                result.add("@SP");
                result.add("A=M-1");
                // x-y
                result.add("D=M-D");
                result.add("@LOOP_GT_TRUE_" + getLoopSymbol(pos));
                result.add("D;JGT");
                result.add("@SP");
                result.add("A=M-1");
                result.add("M=0");
                result.add("@RESTART_GT_" + getLoopSymbol(pos));
                result.add("0;JMP");
                result.add("(LOOP_GT_TRUE_" + getLoopSymbol(pos) + ")");
                result.add("@SP");
                result.add("A=M-1");
                result.add("M=-1");
                result.add("(RESTART_GT_" + getLoopSymbol(pos) + ")");
                break;

        }
    }

    private String getSymbol(Integer pos) {
        return String.format("%s.%d", fileName.toLowerCase(), pos);
    }

    private String getLoopSymbol(Integer pos) {
        return String.format("%s_%d", fileName.toUpperCase(), pos);
    }

    private Integer getPos() {
        return result.size();
    }

}

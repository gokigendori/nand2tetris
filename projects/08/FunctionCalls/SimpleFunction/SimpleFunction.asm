(SimpleFunction.SimpleFunction.test)
@2
D=A-1
(SIMPLEFUNCTION_0)
@LCL
A=M+D
M=0
D=D-1
@SP
M=M+1
@SIMPLEFUNCTION_0
D;JGE
@0
D=A
@LCL
A=M+D
D=M
@SP
A=M
M=D
@SP
M=M+1
@1
D=A
@LCL
A=M+D
D=M
@SP
A=M
M=D
@SP
M=M+1
@SP
M=M-1
A=M
D=M
@SP
A=M-1
M=M+D
@SP
A=M-1
M=!M
@0
D=A
@ARG
A=M+D
D=M
@SP
A=M
M=D
@SP
M=M+1
@SP
M=M-1
A=M
D=M
@SP
A=M-1
M=M+D
@1
D=A
@ARG
A=M+D
D=M
@SP
A=M
M=D
@SP
M=M+1
@SP
M=M-1
A=M
D=M
@SP
A=M-1
M=M-D
@ARG
D=M
@return.simplefunction.76
M=D
@LCL
D=M
@5
D=D-A
@return.simplefunction.76
M=D
@LCL
D=M
A=D-1
D=M
@THAT
M=D
@LCL
D=M
@2
D=D-A
A=D
D=M
@THIS
M=D
@LCL
D=M
@3
D=D-A
A=D
D=M
@ARG
M=D
@LCL
D=M
@4
D=D-A
A=D
D=M
@LCL
M=D
@SP
M=M-1
A=M
D=M
@return.simplefunction.76
A=M
M=D
@return.simplefunction.76
D=M
@SP
M=D+1
@return.simplefunction.76
A=M
(END)
@END
0;JMP
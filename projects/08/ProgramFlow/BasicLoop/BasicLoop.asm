@0
D=A
@SP
A=M
M=D
@SP
M=M+1
@0
D=A
@LCL
D=M+D
@tmp.address.basicloop.7
M=D
@SP
M=M-1
A=M
D=M
@tmp.address.basicloop.7
A=M
M=D
(LOOP_START_BASICLOOP_0)
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
@SP
M=M-1
A=M
D=M
@SP
A=M-1
M=M+D
@0
D=A
@LCL
D=M+D
@tmp.address.basicloop.48
M=D
@SP
M=M-1
A=M
D=M
@tmp.address.basicloop.48
A=M
M=D
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
@1
D=A
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
@0
D=A
@ARG
D=M+D
@tmp.address.basicloop.85
M=D
@SP
M=M-1
A=M
D=M
@tmp.address.basicloop.85
A=M
M=D
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
@LOOP_START_BASICLOOP_0
D;JNE
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
(END)
@END
0;JMP
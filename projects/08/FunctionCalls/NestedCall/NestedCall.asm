@256
D=A
@SP
M=D
@return-0
D=A
@SP
A=M
M=D
@SP
M=M+1
@LCL
D=M
@SP
A=M
M=D
@SP
M=M+1
@ARG
D=M
@SP
A=M
M=D
@SP
M=M+1
@THIS
D=M
@SP
A=M
M=D
@SP
M=M+1
@THAT
D=M
@SP
A=M
M=D
@SP
M=M+1
@SP
D=M
@5
D=D-A
@0
D=D-A
@ARG
M=D
@SP
D=M
@LCL
M=D
@Sys.init
0;JMP
(return-0)
(Sys.init)
@0
D=A-1
@INIT_END_SYS_0
D;JLT
(LOOP_INIT_SYS_0)
@LCL
A=M+D
M=0
D=D-1
@SP
M=M+1
@LOOP_INIT_SYS_0
D;JGE
(INIT_END_SYS_0)
@4000
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
@THIS
M=D
@5000
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
@THAT
M=D
@return-41
D=A
@SP
A=M
M=D
@SP
M=M+1
@LCL
D=M
@SP
A=M
M=D
@SP
M=M+1
@ARG
D=M
@SP
A=M
M=D
@SP
M=M+1
@THIS
D=M
@SP
A=M
M=D
@SP
M=M+1
@THAT
D=M
@SP
A=M
M=D
@SP
M=M+1
@SP
D=M
@5
D=D-A
@0
D=D-A
@ARG
M=D
@SP
D=M
@LCL
M=D
@Sys.main
0;JMP
(return-41)
@5
D=A
@1
D=D+A
@tmp.address.sys.91
M=D
@SP
M=M-1
A=M
D=M
@tmp.address.sys.91
A=M
M=D
(Sys.init$LOOP)
@Sys.init$LOOP
0;JMP
(Sys.main)
@5
D=A-1
@INIT_END_SYS_107
D;JLT
(LOOP_INIT_SYS_107)
@LCL
A=M+D
M=0
D=D-1
@SP
M=M+1
@LOOP_INIT_SYS_107
D;JGE
(INIT_END_SYS_107)
@4001
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
@THIS
M=D
@5001
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
@THAT
M=D
@200
D=A
@SP
A=M
M=D
@SP
M=M+1
@1
D=A
@LCL
D=M+D
@tmp.address.sys.155
M=D
@SP
M=M-1
A=M
D=M
@tmp.address.sys.155
A=M
M=D
@40
D=A
@SP
A=M
M=D
@SP
M=M+1
@2
D=A
@LCL
D=M+D
@tmp.address.sys.175
M=D
@SP
M=M-1
A=M
D=M
@tmp.address.sys.175
A=M
M=D
@6
D=A
@SP
A=M
M=D
@SP
M=M+1
@3
D=A
@LCL
D=M+D
@tmp.address.sys.195
M=D
@SP
M=M-1
A=M
D=M
@tmp.address.sys.195
A=M
M=D
@123
D=A
@SP
A=M
M=D
@SP
M=M+1
@return-215
D=A
@SP
A=M
M=D
@SP
M=M+1
@LCL
D=M
@SP
A=M
M=D
@SP
M=M+1
@ARG
D=M
@SP
A=M
M=D
@SP
M=M+1
@THIS
D=M
@SP
A=M
M=D
@SP
M=M+1
@THAT
D=M
@SP
A=M
M=D
@SP
M=M+1
@SP
D=M
@5
D=D-A
@1
D=D-A
@ARG
M=D
@SP
D=M
@LCL
M=D
@Sys.add12
0;JMP
(return-215)
@5
D=A
@0
D=D+A
@tmp.address.sys.265
M=D
@SP
M=M-1
A=M
D=M
@tmp.address.sys.265
A=M
M=D
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
@2
D=A
@LCL
A=M+D
D=M
@SP
A=M
M=D
@SP
M=M+1
@3
D=A
@LCL
A=M+D
D=M
@SP
A=M
M=D
@SP
M=M+1
@4
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
M=M-1
A=M
D=M
@SP
A=M-1
M=M+D
@SP
M=M-1
A=M
D=M
@SP
A=M-1
M=M+D
@SP
M=M-1
A=M
D=M
@SP
A=M-1
M=M+D
@SP
M=M-1
A=M
D=M
@return.value.sys.356
M=D
@ARG
D=M
@return.sp.sys.356
M=D+1
@LCL
D=M
@5
D=D-A
A=D
D=M
@return.callback.sys.356
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
@return.sp.sys.356
D=M
@SP
M=D
@return.value.sys.356
D=M
@SP
A=M-1
M=D
@return.callback.sys.356
A=M
0;JMP
(Sys.add12)
@0
D=A-1
@INIT_END_SYS_416
D;JLT
(LOOP_INIT_SYS_416)
@LCL
A=M+D
M=0
D=D-1
@SP
M=M+1
@LOOP_INIT_SYS_416
D;JGE
(INIT_END_SYS_416)
@4002
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
@THIS
M=D
@5002
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
@THAT
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
@12
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
M=M+D
@SP
M=M-1
A=M
D=M
@return.value.sys.481
M=D
@ARG
D=M
@return.sp.sys.481
M=D+1
@LCL
D=M
@5
D=D-A
A=D
D=M
@return.callback.sys.481
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
@return.sp.sys.481
D=M
@SP
M=D
@return.value.sys.481
D=M
@SP
A=M-1
M=D
@return.callback.sys.481
A=M
0;JMP
// This file is part of www.nand2tetris.org
// and the book "The Elements of Computing Systems"
// by Nisan and Schocken, MIT Press.
// File name: projects/04/Mult.asm

// Multiplies R0 and R1 and stores the result in R2.
// (R0, R1, R2 refer to RAM[0], RAM[1], and RAM[2], respectively.)
//
// This program only needs to handle arguments that satisfy
// R0 >= 0, R1 >= 0, and R0*R1 < 32768.

// Put your code here.
@sum
M=0 // sum = 0
@i
M=1 // i = 1
(LOOP)
@i
D=M
@R0
D=D-M // i - R0
@END
D;JGT // if i > R0 goto END
@R1
D=M
@sum
M=M+D // sum += R1
@i
M=M+1 // i++
@LOOP 
0;JMP // goto loop
(END)
@END
@sum
D=M
@R2
M=D
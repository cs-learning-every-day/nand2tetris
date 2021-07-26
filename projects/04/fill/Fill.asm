// This file is part of www.nand2tetris.org
// and the book "The Elements of Computing Systems"
// by Nisan and Schocken, MIT Press.
// File name: projects/04/Fill.asm

// Runs an infinite loop that listens to the keyboard input.
// When a key is pressed (any key), the program blackens the screen,
// i.e. writes "black" in every pixel;
// the screen should remain fully black as long as the key is pressed. 
// When no key is pressed, the program clears the screen, i.e. writes
// "white" in every pixel;
// the screen should remain fully clear as long as no key is pressed.

// Put your code here.
// D register只能存储数据
// A register还可以用来M[A]

    // R0 = 屏幕最大填充地址
    @24575
    D=A
    @0
    M=D

    // R1 = 屏幕当前填充地址
    @SCREEN
    D=A
    @1
    M=D
(LOOP)
    @KBD
    D=M
    @FILL
    D; JGT
    @RESET
    0; JMP
(FILL)
    // 判断屏幕是否已满
    @0
    D=M
    @1
    D=D-M
    @LOOP
    D;JLT // 最大地址 < 当前地址
    
    // 填充当前地址 屏幕为黑色
    @1
    D=M
    A=M
    M=-1

    @1
    M=D+1

    @LOOP
    0;JMP
(RESET)
    // 判断屏幕是否为空
    @SCREEN
    D=A
    @1
    D=D-M
    @LOOP
    D;JGT // 屏幕初始地址 > 屏幕当前填充地址

    // 填充当前地址 屏幕为白色
    @1
    D=M
    A=M
    M=0

    @1
    M=D-1

    @LOOP
    0;JMP
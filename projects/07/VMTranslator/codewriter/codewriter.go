package codewriter

import (
	"log"
	"strconv"
)

var index = 0

func WriteArithmetic(cmdName, arg1, arg2 string) string {
	if cmdName == "add" {
		return arithmeticTemplateOne() + "M=M+D\r\n"
	} else if cmdName == "sub" {
		return arithmeticTemplateOne() + "M=M-D\r\n"
	} else if cmdName == "and" {
		return arithmeticTemplateOne() + "M=M&D\r\n"
	} else if cmdName == "or" {
		return arithmeticTemplateOne() + "M=M|D\r\n"
	} else if cmdName == "not" {
		return "@SP\r\nA=M-1\r\nM=!M\r\n"
	} else if cmdName == "neg" {
		return "D=0\r\n@SP\r\nA=M-1\r\nM=D-M\r\n"
	} else if cmdName == "gt" { // not <=
		index += 1
		return arithmeticTemplateTwo("JLE", index)
	} else if cmdName == "lt" { // not >=
		index += 1
		return arithmeticTemplateTwo("JGT", index)
	} else if cmdName == "eq" { // not <>
		index += 1
		return arithmeticTemplateTwo("JNE", index)
	} else {
		log.Fatalln("illegal cmd type")
	}
	return ""
}

func WritePushPop(arg1, arg2 string) string {
	output := ""
	if arg1 == "constant" {
		output = "@" + arg2 + "\r\n" +
			"D=A\r\n" +
			"@SP\r\n" +
			"A=M\r\n" +
			"M=D\r\n" +
			"@SP\r\n" +
			"M=M+1\r\n"
	}

	return output
}

// add、sub、or
func arithmeticTemplateOne() string {
	return "@SP\r\n" +
		"AM=M-1\r\n" +
		"D=M\r\n" +
		"A=A-1\r\n"
}

// gt、lt、eq
func arithmeticTemplateTwo(cmdType string, flag int) string {
	f := strconv.Itoa(flag)
	return "@SP\r\n" +
		"AM=M-1\r\n" +
		"D=M\r\n" +
		"A=A-1\r\n" +
		"D=M-D\r\n" + // 两数相减
		"@FALSE" + f + "\r\n" + // false 跳转symbol
		"D;" + cmdType + "\r\n" +
		"@SP\r\n" +
		"A=M-1\r\n" +
		"M=-1\r\n" + // true
		"@CONTINUE" + f + "\r\n" +
		"0;JMP\r\n" +
		"(FALSE" + f + ")\r\n" +
		"@SP\r\n" +
		"A=M-1\r\n" +
		"M=0\r\n" + // false
		"(@CONTINUE" + f + ")\r\n"
}

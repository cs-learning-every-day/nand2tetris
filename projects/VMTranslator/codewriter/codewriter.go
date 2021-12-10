package codewriter

import (
	"VMTranslator/common"
	"log"
	"regexp"
	"strconv"
	"strings"
)

var index = 0
var labelCount = 0

func WriteArithmetic(cmdName string) string {
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
		return arithmeticTemplateTwo("JGE", index)
	} else if cmdName == "eq" { // not <>
		index += 1
		return arithmeticTemplateTwo("JNE", index)
	} else {
		log.Fatalln("illegal cmd type")
	}
	return ""
}

func WritePushPop(arg1, arg2 string, cmdType common.CommandType, filename string) string {
	output := ""
	argArea := strings.ToLower(arg1)

	if cmdType == common.Push {
		if argArea == "constant" {
			output = "@" + arg2 + "\r\n" +
				"D=A\r\n" +
				"@SP\r\n" +
				"A=M\r\n" +
				"M=D\r\n" +
				"@SP\r\n" +
				"M=M+1\r\n"
		} else if argArea == "static" {
			tmp, _ := strconv.Atoi(arg2)
			tmp += 16
			segment := filename + "." + strconv.Itoa(tmp)
			output = pushTemplate(segment, arg2, true)
		} else if argArea == "pointer" && arg2 == "0" {
			output = pushTemplate("THIS", arg2, true)
		} else if argArea == "pointer" && arg2 == "1" {
			output = pushTemplate("THAT", arg2, true)
		} else if argArea == "that" {
			output = pushTemplate("THAT", arg2, false)
		} else if argArea == "this" {
			output = pushTemplate("THIS", arg2, false)
		} else if argArea == "temp" {
			tmp, _ := strconv.Atoi(arg2)
			tmp += 5
			output = pushTemplate("R5", strconv.Itoa(tmp), false)
		} else if argArea == "local" {
			output = pushTemplate("LCL", arg2, false)
		} else if argArea == "argument" {
			output = pushTemplate("ARG", arg2, false)
		}
	} else if cmdType == common.Pop {
		if argArea == "static" {
			tmp, _ := strconv.Atoi(arg2)
			tmp += 16
			segment := filename + "." + strconv.Itoa(tmp)
			//output = popTemplate(segment, arg2, true)
			output = "@SP\r\n" +
				"AM=M-1\r\n" +
				"D=M\r\n" +
				"@" + segment + "\r\n" +
				"M=D\r\n"
		} else if argArea == "pointer" && arg2 == "0" {
			output = popTemplate("THIS", arg2, true)
		} else if argArea == "pointer" && arg2 == "1" {
			output = popTemplate("THAT", arg2, true)
		} else if argArea == "that" {
			output = popTemplate("THAT", arg2, false)
		} else if argArea == "this" {
			output = popTemplate("THIS", arg2, false)
		} else if argArea == "temp" {
			tmp, _ := strconv.Atoi(arg2)
			tmp += 5
			output = popTemplate("R5", strconv.Itoa(tmp), false)
		} else if argArea == "local" {
			output = popTemplate("LCL", arg2, false)
		} else if argArea == "argument" {
			output = popTemplate("ARG", arg2, false)
		}
	}

	return output
}

func WriteLabel(arg1 string) string {
	match, _ := regexp.MatchString(common.LabelReg, arg1)

	if !match {
		log.Fatal("label 后面不能跟数字开头")
	}

	return "(" + arg1 + ")\r\n"
}

func WriteInit() string {
	output := "@256\r\n" +
		"D=A\r\n" +
		"@SP\r\n" +
		"M=D\r\n"
	output += WriteCall("Sys.init", 0)
	return output
}

func WriteGoTo(label string) string {
	match, _ := regexp.MatchString(common.LabelReg, label)

	if !match {
		log.Fatal("label 格式错误")
	}

	return "@" + label + "\r\n" +
		"0;JMP\r\n"
}

func WriteIf(label string) string {
	match, _ := regexp.MatchString(common.LabelReg, label)

	if !match {
		log.Fatal("label 格式错误")
	}

	return "@SP\r\n" +
		"AM=M-1\r\n" +
		"D=M\r\n" +
		"@" + label + "\r\n" +
		"D;JNE\r\n"
}

func WriteCall(functionName string, numArgs int) string {
	label := "RETURN_ADDRESS_CALL_" + functionName + strconv.Itoa(labelCount)
	labelCount++

	output := "@" + label + "\r\n" +
		"D=A\r\n" +
		"@SP\r\n" +
		"A=M\r\n" +
		"M=D\r\n" +
		"@SP\r\n" +
		"M=M+1\r\n" // push return address

	output += pushTemplate("LCL", "0", true)  // push LCL
	output += pushTemplate("ARG", "0", true)  // push ARG
	output += pushTemplate("THIS", "0", true) // push THIS
	output += pushTemplate("THAT", "0", true) // push THAT

	output += "@SP\r\n" +
		"D=M\r\n" +
		"@5\r\n" +
		"D=D-A\r\n" +
		"@" + strconv.Itoa(numArgs) + "\r\n" +
		"D=D-A\r\n" +
		"@ARG\r\n" +
		"M=D\r\n" + // ARG = SP - n - 5
		"@SP\r\n" +
		"D=M\r\n" +
		"@LCL\r\n" +
		"M=D\r\n" + // LCL = SP
		"@" + functionName + "\r\n" +
		"0;JMP\r\n" + // goto f
		"(" + label + ")\r\n" // (return-address)
	return output
}

func WriteReturn() string {
	return "@LCL\r\n" +
		"D=M\r\n" +
		"@R13\r\n" +
		"M=D\r\n" +
		"@5\r\n" +
		"A=D-A\r\n" +
		"D=M\r\n" +
		"@R14\r\n" + // 保存返回地址
		"M=D\r\n" +
		"@SP\r\n" +
		"AM=M-1\r\n" +
		"D=M\r\n" +
		"@ARG\r\n" +
		"A=M\r\n" +
		"M=D\r\n" + // *ARG = pop()
		"@ARG\r\n" +
		"D=M\r\n" +
		"@SP\r\n" +
		"M=D+1\r\n" + // SP = ARG + 1
		preFrameTemplate("THAT") +
		preFrameTemplate("THIS") +
		preFrameTemplate("ARG") +
		preFrameTemplate("LCL") +
		"@R14\r\n" +
		"A=M\r\n" +
		"0;JMP\r\n"
}

func WriteFunction(functionName string, numLocals int) string {
	output := "(" + functionName + ")\r\n"
	for i := 0; i < numLocals; i++ {
		output += WritePushPop("constant", "0", common.Push, "")
	}
	return output
}

func preFrameTemplate(position string) string {
	return "@R13\r\n" +
		"D=M-1\r\n" +
		"AM=D\r\n" +
		"D=M\r\n" +
		"@" + position + "\r\n" +
		"M=D\r\n"
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
		"(CONTINUE" + f + ")\r\n"
}

func pushTemplate(segment, index string, isDirect bool) string {
	noPointerCode := ""
	if !isDirect {
		noPointerCode = "@" + index + "\r\nA=D+A\r\nD=M\r\n"
	}

	return "@" + segment + "\r\n" +
		"D=M\r\n" +
		noPointerCode +
		"@SP\r\n" +
		"A=M\r\n" +
		"M=D\r\n" +
		"@SP\r\n" +
		"M=M+1\r\n"
}

func popTemplate(segment, index string, isDirect bool) string {
	noPointerCode := "D=A\r\n"
	if !isDirect {
		noPointerCode = "D=M\r\n@" + index + "\r\nD=D+A\r\n"
	}

	return "@" + segment + "\r\n" +
		noPointerCode +
		"@R13\r\n" +
		"M=D\r\n" +
		"@SP\r\n" +
		"AM=M-1\r\n" +
		"D=M\r\n" +
		"@R13\r\n" +
		"A=M\r\n" +
		"M=D\r\n"
}

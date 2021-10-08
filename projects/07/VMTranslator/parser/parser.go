package parser

import (
	"VMTranslator/codewriter"
	"bufio"
	"fmt"
	"io/fs"
	"log"
	"strings"
)

type CommandType int8

const (
	Arithmetic CommandType = iota
	Push
	Pop
	Label
	GoTo
	If
	Function
	Return
	Call
	Nil
)

func Parser(sourceFile fs.File) []string {
	scanner := bufio.NewScanner(sourceFile)

	scanner.Split(bufio.ScanLines)

	var outputs []string

	for hasMoreCommands(scanner) {
		cmd := scanner.Text()
		if isValidCommand(cmd) {
			outputs = append(outputs, advance(cmd))
		}
	}

	return outputs
}

func hasMoreCommands(scanner *bufio.Scanner) bool {
	return scanner.Scan()
}

func advance(command string) string {

	cmdType := commandType(command)
	argOne := arg1(command, cmdType)
	argTwo := arg2(command, cmdType)
	cmdName := commandName(command)

	switch cmdType {
	case Arithmetic:
		return codewriter.WriteArithmetic(cmdName, argOne, argTwo)
	case Pop:
		fmt.Println("Pop: " + argOne + " " + argTwo)
	case Push:
		return codewriter.WritePushPop(argOne, argTwo)
	}

	return ""
}

func commandName(command string) string {
	return strings.Split(command, " ")[0]
}

func commandType(cmd string) CommandType {
	if strings.Contains(cmd, "push") {
		return Push
	} else if strings.Contains(cmd, "pop") {
		return Pop
	} else if strings.Contains(cmd, "add") ||
		strings.Contains(cmd, "sub") {
		return Arithmetic
	} else {
		log.Panicln("command type error")
		return -1
	}
}

func arg1(cmd string, cmdType CommandType) string {
	cmds := strings.Split(cmd, " ")
	if cmdType == Arithmetic {
		return cmds[0]
	}

	if cmdType == Return {
		return ""
	}

	return cmds[1]
}

func arg2(cmd string, cmdType CommandType) string {
	cmds := strings.Split(cmd, " ")

	if cmdType != Push &&
		cmdType != Pop &&
		cmdType != Function &&
		cmdType != Call {
		return ""
	}

	return cmds[2]
}

func isValidCommand(command string) bool {
	if command == "" ||
		(command[0] == '/' && command[1] == '/') {
		return false
	}
	return true
}

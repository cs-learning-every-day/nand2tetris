package parser

import (
	"VMTranslator/codewriter"
	"VMTranslator/common"
	"bufio"
	"os"
	"path/filepath"
	"strings"
)

func Parse(sourceFile *os.File) []string {
	scanner := bufio.NewScanner(sourceFile)

	scanner.Split(bufio.ScanLines)

	var outputs []string

	path := strings.TrimSuffix(sourceFile.Name(), ".vm")
	filename := filepath.Base(path)

	for hasMoreCommands(scanner) {
		cmd := scanner.Text()
		if isValidCommand(cmd) {
			outputs = append(outputs, advance(cmd, filename))
		}
	}

	return outputs
}

func hasMoreCommands(scanner *bufio.Scanner) bool {
	return scanner.Scan()
}

func advance(command, filename string) string {

	cmdType := commandType(command)
	argOne := arg1(command, cmdType)
	argTwo := arg2(command, cmdType)
	cmdName := commandName(command)

	switch cmdType {
	case common.Arithmetic:
		return codewriter.WriteArithmetic(cmdName, argOne, argTwo)
	case common.Pop:
		return codewriter.WritePushPop(argOne, argTwo, common.Pop, filename)
	case common.Push:
		return codewriter.WritePushPop(argOne, argTwo, common.Push, filename)
	}

	return ""
}

func commandName(command string) string {
	return strings.Split(command, " ")[0]
}

func commandType(cmd string) common.CommandType {
	if strings.Contains(cmd, "push") {
		return common.Push
	} else if strings.Contains(cmd, "pop") {
		return common.Pop
	} else {
		return common.Arithmetic
	}
}

func arg1(cmd string, cmdType common.CommandType) string {
	cmds := strings.Split(cmd, " ")
	if cmdType == common.Arithmetic {
		return cmds[0]
	}

	if cmdType == common.Return {
		return ""
	}

	return cmds[1]
}

func arg2(cmd string, cmdType common.CommandType) string {
	cmds := strings.Split(cmd, " ")

	if cmdType != common.Push &&
		cmdType != common.Pop &&
		cmdType != common.Function &&
		cmdType != common.Call {
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

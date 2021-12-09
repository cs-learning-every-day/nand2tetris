package parser

import (
	"VMTranslator/codewriter"
	"VMTranslator/common"
	"VMTranslator/utils"
	"bufio"
	"log"
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
		return codewriter.WriteArithmetic(cmdName)
	case common.Pop:
		return codewriter.WritePushPop(argOne, argTwo, common.Pop, filename)
	case common.Push:
		return codewriter.WritePushPop(argOne, argTwo, common.Push, filename)
	case common.Label:
		return codewriter.WriteLabel(argOne)
	case common.If:
		return codewriter.WriteIf(argOne)
	}

	return ""
}

func commandName(command string) string {
	return strings.Split(command, " ")[0]
}

var aTypes = []string{"add", "sub", "neg", "eq", "gt", "lt", "and", "or", "not"}

func commandType(cmd string) common.CommandType {
	if strings.Contains(cmd, "push") {
		return common.Push
	} else if strings.Contains(cmd, "pop") {
		return common.Pop
	} else if strings.Contains(cmd, "label") {
		return common.Label
	} else if strings.Contains(cmd, "goto") {
		return common.GoTo
	} else if strings.Contains(cmd, "function") {
		return common.Function
	} else if strings.Contains(cmd, "call") {
		return common.Call
	} else if strings.Contains(cmd, "if") {
		return common.If
	} else if utils.Contains(aTypes, cmd[:3]) {
		return common.Arithmetic
	}
	log.Fatal("未知命令")
	return common.Nil
}

func arg1(cmd string, cmdType common.CommandType) string {
	if cmdType == common.Return ||
		cmdType == common.Arithmetic {
		return ""
	}

	cmds := strings.Split(cmd, " ")
	return cmds[1]
}

func arg2(cmd string, cmdType common.CommandType) string {
	if cmdType != common.Push &&
		cmdType != common.Pop &&
		cmdType != common.Function &&
		cmdType != common.Call {
		return ""
	}

	cmds := strings.Split(cmd, " ")
	return cmds[2]
}

func isValidCommand(command string) bool {
	if command == "" ||
		(command[0] == '/' && command[1] == '/') {
		return false
	}
	return true
}

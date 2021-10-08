package parser

import (
	"bufio"
	"fmt"
	"io/fs"
)

type Command string

func Parser(file fs.File) {
	scanner := bufio.NewScanner(file)

	scanner.Split(bufio.ScanLines)

	for scanner.Scan() {
		cmd := Command(scanner.Text())
		if isValidCommand(cmd) {
			fmt.Println(cmd)
		}
	}

}

func advance(command Command) {

}

func commandType(command Command) Command {
	return ""
}

func arg1() {

}

func arg2() {

}

func isValidCommand(command Command) bool {
	if command == "" ||
		(command[0] == '/' && command[1] == '/') {
		return false
	}
	return true
}

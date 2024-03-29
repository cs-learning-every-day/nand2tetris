package parser

import (
	"assembler/code"
	"assembler/symtable"
	"strconv"
	"strings"
)

type CmdType rune

var pc = -1

func Parse(instructions []string, isFirst bool) []string {
	return advance(instructions, isFirst)
}

func hasMoreCommands(instructions []string) bool {
	return len(instructions) > 0
}

func advance(instructions []string, isFirst bool) []string {
	var text []string
	if isFirst {
		symtable.InitSymbolTable()
	}

	for hasMoreCommands(instructions) {
		current := strings.ReplaceAll(instructions[0], " ", "")
		instructions = instructions[1:]

		if isInstructionInvalid(current) {
			continue
		}

		current = removeLastComment(current)
		cmdType := commandType(current)
		// 首次解析构建符号表 并不产生代码
		switch cmdType {
		case 'A':
			if !isFirst {
				sym := symbol(current, cmdType)
				if v, err := strconv.Atoi(sym); err == nil {
					bv := int2Binary(v)
					bv = bv[1:16]
					text = append(text, "0"+bv, "\r\n")
				} else { // symbol
					var bv string
					if symtable.Contains(sym) {
						bv = int2Binary(symtable.GetAddress(sym))
					} else { // variable
						bv = int2Binary(symtable.GetRamAddress())
						symtable.AddEntry(sym, symtable.IncrRamAddress())
					}
					text = append(text, bv, "\r\n")
				}
			} else {
				pc += 1
			}
		case 'C':
			if !isFirst {
				c := code.Comp(current)
				d := code.Dest(current)
				j := code.Jump(current)
				text = append(text, "111"+c+d+j, "\r\n")
			} else {
				pc += 1
			}
		case 'L':
			if isFirst {
				sym := symbol(current, cmdType)
				// 加入到符号表
				symtable.AddEntry(sym, pc+1)
			}
		}
	}
	return text
}

// 返回16位二进制
func int2Binary(v int) string {
	bv := strconv.FormatInt(int64(v&0x0000FFFF), 2)
	for i := len(bv); i < 16; i++ {
		bv = "0" + bv
	}
	return bv
}

// 参数是合法的指令
func removeLastComment(instruction string) string {
	var text string
	for i := 0; i < len(instruction); i++ {
		if instruction[i] == '/' {
			break
		}
		text += string(instruction[i])
	}
	return text
}

func commandType(instruction string) CmdType {
	if instruction[0] == '@' {
		return 'A'
	} else if instruction[0] == '(' {
		return 'L'
	} else {
		return 'C'
	}
}

func isInstructionInvalid(instruction string) bool {
	if instruction == "" ||
		(instruction[0] == '/' && instruction[1] == '/') {
		return true
	}
	return false
}

func symbol(instruction string, cmdType CmdType) string {
	if cmdType == 'A' {
		return instruction[1:]
	} else if cmdType == 'L' {
		// (Xxx)
		instruction = instruction[1:]
		instruction = instruction[:len(instruction)-1]
		return instruction
	}
	return ""
}

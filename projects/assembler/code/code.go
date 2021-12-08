package code

import (
	"strings"
)

var jSet = map[string]string{
	"JGT": "001",
	"JEQ": "010",
	"JGE": "011",
	"JLT": "100",
	"JNE": "101",
	"JLE": "110",
	"JMP": "111",
}

var cSet = map[string]string{
	"0":   "0101010",
	"1":   "0111111",
	"-1":  "0111010",
	"D":   "0001100",
	"A":   "0110000",
	"M":   "1110000",
	"!D":  "0001101",
	"!A":  "0110001",
	"!M":  "1110001",
	"-D":  "0001111",
	"-A":  "0110011",
	"-M":  "1110011",
	"D+1": "0011111",
	"A+1": "0110111",
	"M+1": "1110111",
	"D-1": "0001110",
	"A-1": "0110010",
	"M-1": "1110010",
	"D+A": "0000010",
	"D+M": "1000010",
	"D-A": "0010011",
	"D-M": "1010011",
	"A-D": "0000111",
	"M-D": "1000111",
	"D&A": "0000000",
	"D&M": "1000000",
	"D|A": "0010101",
	"D|M": "1010101",
}

// dest=comp;jump

func Dest(instruction string) string {
	ret := []rune{'0', '0', '0'}

	if strings.Contains(instruction, "=") {
		s := strings.Split(instruction, "=")[0]

		for i := 0; i < len(s); i++ {
			switch s[i] {
			case 'M':
				ret[2] = '1'
			case 'D':
				ret[1] = '1'
			case 'A':
				ret[0] = '1'
			}
		}
	}

	return string(ret)
}

func Comp(instruction string) string {
	comp := instruction
	if strings.Contains(comp, "=") {
		comp = strings.Split(comp, "=")[1]
	}

	if strings.Contains(comp, ";") {
		comp = strings.Split(comp, ";")[0]
	}
	if v, ok := cSet[comp]; ok {
		return v
	} else if strings.ContainsAny(comp, "+|&") {
		return ""
	}

	panic("comp解析错误")
}

func Jump(instruction string) string {
	if strings.Contains(instruction, ";") {
		key := strings.Split(instruction, ";")[1]
		return jSet[key]
	}
	return "000"
}

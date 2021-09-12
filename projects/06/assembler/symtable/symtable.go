package symtable

import "strconv"

var symbolTable = map[string]int{}
var ramAddress = 16

func InitSymbolTable() {
	symbolTable["SP"] = 0
	symbolTable["LCL"] = 1
	symbolTable["ARG"] = 2
	symbolTable["THIS"] = 3
	symbolTable["THAT"] = 4
	symbolTable["SCREEN"] = 16384
	symbolTable["KBD"] = 24576
	for i := 0; i < 16; i++ {
		symbolTable["R"+strconv.Itoa(i)] = i
	}
}

func AddEntry(symbol string, address int) {
	symbolTable[symbol] = address
}

func Contains(symbol string) bool {
	_, ok := symbolTable[symbol]
	return ok
}

func GetAddress(symbol string) int {
	return symbolTable[symbol]
}

func GetRamAddress() int {
	return ramAddress
}

// IncrRamAddress return old ram address
func IncrRamAddress() int {
	old := ramAddress
	ramAddress++
	return old
}

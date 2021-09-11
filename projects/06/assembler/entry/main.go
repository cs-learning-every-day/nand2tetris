package main

import (
	"assembler/parser"
	"bufio"
	"log"
	"os"
	"strings"
)

func createHackFile(from, to string) {
	sourceFile, err := os.Open(from)
	if err != nil {
		log.Fatalf("failed to open")
	}
	defer sourceFile.Close()

	scanner := bufio.NewScanner(sourceFile)

	scanner.Split(bufio.ScanLines)
	var instructions []string

	for scanner.Scan() {
		instructions = append(instructions, scanner.Text())
	}

	// 首次解析收集符号
	parser.Parse(instructions, true)

	// 真正的解析指令
	output := parser.Parse(instructions, false)

	// write file to target
	targetFile, err := os.Create(to)
	if err != nil {
		log.Fatalf("failed to create")
	}
	defer targetFile.Close()

	for _, line := range output {
		targetFile.WriteString(line)
	}
}

func main() {
	if len(os.Args) < 2 {
		log.Fatalf("usage: %s <target file>\n", os.Args[0])
	}

	sourceFilePath := os.Args[1]
	if strings.Index(sourceFilePath, ".asm") < 0 {
		log.Fatalln("目标文件格式错误")
	}

	targetFilePath := strings.Split(sourceFilePath, ".asm")[0] + ".hack"
	createHackFile(sourceFilePath, targetFilePath)
}

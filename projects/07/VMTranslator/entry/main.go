package main

import (
	"VMTranslator/utils"
	"fmt"
	"io/fs"
	"log"
	"os"
	"path/filepath"
	"strings"
)

func checkPathValid(path string) {
	file, err := os.Open(path)
	if err != nil {
		log.Fatalf("open path error\n")
	}

	defer file.Close()
}

// sourcePath肯定是.vm文件
func createAsmFile(sourcePath string) {
	suffixIdx := strings.LastIndex(sourcePath, ".vm")
	targetPath := sourcePath[:suffixIdx] + ".asm"
	fmt.Println(targetPath)
}

func visitDirectory(dirPath string) {
	err := filepath.Walk(dirPath, func(path string, info fs.FileInfo, err error) error {
		// 跳过不是vm文件类型的
		suffixIdx := strings.LastIndex(path, ".vm")
		if suffixIdx < 0 {
			return nil
		}

		if !info.IsDir() {
			fmt.Println("==", path)
			createAsmFile(path)
		}
		return nil
	})
	if err != nil {
		log.Fatal(err)
	}
}

func run(path string) {
	checkPathValid(path)

	if utils.IsDirectory(path) {
		visitDirectory(path)
	} else {
		createAsmFile(path)
	}
}

func main() {
	if len(os.Args) < 2 {
		log.Fatalf("usage: %s <target file> or <target directory>\n", os.Args[0])
	}
	run(os.Args[1])
}

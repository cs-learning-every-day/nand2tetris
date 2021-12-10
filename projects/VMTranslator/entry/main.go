package main

import (
	"VMTranslator/codewriter"
	"VMTranslator/parser"
	"VMTranslator/utils"
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

func getAllVmFilePath(dirPath string) ([]string, error) {
	var output []string

	err := filepath.Walk(dirPath, func(path string, info fs.FileInfo, err error) error {
		// 跳过不是vm文件类型的
		suffixIdx := strings.LastIndex(path, ".vm")
		if suffixIdx < 0 {
			return nil
		}

		if !info.IsDir() {
			output = append(output, path)
		}
		return nil
	})

	return output, err
}

// sourcePath肯定是.vm文件
func createAsmWithSingleFile(sourcePath string) {

	suffixIdx := strings.LastIndex(sourcePath, ".vm")
	targetPath := sourcePath[:suffixIdx] + ".asm"

	// open source file
	sourceFile, err := os.Open(sourcePath)
	if err != nil {
		log.Fatalln("open failed")
	}
	defer sourceFile.Close()

	contents := parser.Parse(sourceFile)

	//fmt.Println(targetPath, ": ", contents)
	createFileWithContents(targetPath, contents)
}

func createAsmWithMultipileFile(dirPath, filename string) {

	var contents []string

	paths, err2 := getAllVmFilePath(dirPath)
	if err2 != nil {
		log.Fatalln("getAllVmFilePath error")
	}

	if len(paths) == 0 {
		log.Fatal("该目录下，没有vm文件！")
	}

	contents = append(contents, codewriter.WriteInit())
	for _, path := range paths {
		sourceFile, err := os.Open(path)
		if err != nil {
			log.Fatalf("open failed (%s)\n", path)
		}
		contents = append(contents, parser.Parse(sourceFile)...)
		sourceFile.Close()
	}

	targetPath := dirPath + "/" + filename + ".asm"
	createFileWithContents(targetPath, contents)
}

func createFileWithContents(path string, contents []string) {
	file, err := os.Create(path)
	if err != nil {
		log.Fatalln("create failed")
	}

	defer file.Close()

	for _, line := range contents {
		file.WriteString(line)
	}
}

func visitDirectory(dirPath, filename string) {
	if filename == "" {
		err := filepath.Walk(dirPath, func(path string, info fs.FileInfo, err error) error {
			// 跳过不是vm文件类型的
			suffixIdx := strings.LastIndex(path, ".vm")
			if suffixIdx < 0 {
				return nil
			}

			if !info.IsDir() {
				createAsmWithSingleFile(path)
			}
			return nil
		})

		if err != nil {
			log.Fatal(err)
		}
	} else {
		// 整合所有vm文件
		createAsmWithMultipileFile(dirPath, filename)
	}

}

func run(path, filename string) {
	checkPathValid(path)

	if utils.IsDirectory(path) {
		visitDirectory(path, filename)
	} else {
		createAsmWithSingleFile(path)
	}
}

func main() {
	if len(os.Args) < 2 {
		log.Fatal("用法详见README")
	}

	var targetFileName = ""

	if len(os.Args) == 3 {
		targetFileName = os.Args[2]
	}

	run(os.Args[1], targetFileName)
}

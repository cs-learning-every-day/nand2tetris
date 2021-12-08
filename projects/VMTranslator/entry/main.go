package main

import (
	"VMTranslator/parser"
	"VMTranslator/utils"
	"io/fs"
	"io/ioutil"
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

func getAllVmFiles(dirPath string) []fs.FileInfo {
	files, err := ioutil.ReadDir(dirPath)

	if err != nil {
		log.Fatal(err)
	}

	res := make([]fs.FileInfo, 0)
	for _, file := range files {
		if strings.HasSuffix(file.Name(), ".vm") {
			res = append(res, file)
		}
	}
	return res
}

// sourcePath肯定是.vm文件
func createAsmFile(sourcePath string) {

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
	targetFile, err := os.Create(targetPath)
	if err != nil {
		log.Fatalln("create failed")
	}

	defer targetFile.Close()

	for _, line := range contents {
		targetFile.WriteString(line)
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
				createAsmFile(path)
			}
			return nil
		})

		if err != nil {
			log.Fatal(err)
		}
	} else {
		files := getAllVmFiles(dirPath)
		if len(files) == 0 {
			log.Fatal("该目录下，没有vm文件！")
		}
		// TODO: 整合所有vm文件
	}

}

func run(path, filename string) {
	checkPathValid(path)

	if utils.IsDirectory(path) {
		visitDirectory(path, filename)
	} else {
		createAsmFile(path)
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

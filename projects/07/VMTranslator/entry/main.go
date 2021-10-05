package main

import (
	"log"
	"os"
)

func generateAsm(path string) {
	file, err := os.Open(path)
	if err != nil {
		log.Fatalf("open path error\n")
	}

	defer file.Close()

	if isDirectory(path) {

	} else {

	}
}

func isDirectory(path string) bool {
	fileInfo, err := os.Stat(path)
	if err != nil {
		log.Fatalf("stat error\n")
	}

	return fileInfo.IsDir()
}

func main() {
	if len(os.Args) < 2 {
		log.Fatalf("usage: %s <target file> or <target directory>\n", os.Args[0])
	}
	generateAsm(os.Args[1])
}

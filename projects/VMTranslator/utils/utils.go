package utils

import (
	"fmt"
	"io/ioutil"
	"log"
	"os"
)

// 遍历目标目录，不遍历子目录
func PrintDirectoryFilename(path string) {
	files, err := ioutil.ReadDir(path)
	if err != nil {
		log.Fatal(err)
	}

	for _, file := range files {
		fmt.Println(file.Name())
	}
}



func IsDirectory(path string) bool {
	fileInfo, err := os.Stat(path)
	if err != nil {
		log.Fatalln("stat error")
	}

	return fileInfo.IsDir()
}

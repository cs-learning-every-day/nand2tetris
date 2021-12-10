package utils

import (
	"fmt"
	"io/ioutil"
	"log"
	"os"
	"strings"
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

func Contains(s []string, t string) bool {
	for _, a := range s {
		if strings.Contains(t, a) {
			return true
		}
	}
	return false
}

func IsDirectory(path string) bool {
	fileInfo, err := os.Stat(path)
	if err != nil {
		log.Fatalln("stat error")
	}

	return fileInfo.IsDir()
}

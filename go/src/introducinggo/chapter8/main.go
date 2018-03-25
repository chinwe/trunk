package main

import (
	"container/list"
	"crypto/sha256"
	"fmt"
	"hash/crc32"
	"introducinggo/chapter8/pkg"
	"io/ioutil"
	"os"
	"strings"
)

func main() {
	// Contains(s, substr string) bool
	fmt.Println(strings.Contains("test", "es")) // => true

	// func Count(s, sep string) int
	fmt.Println(strings.Count("test", "t")) // => 2

	// func HasPrefix(s, prefix string) bool
	fmt.Println(strings.HasPrefix("test", "te")) // => true

	file, err := os.Create("test.txt")
	if err != nil {
		// handle the error here
		return
	}
	defer file.Close()
	file.WriteString("test")

	bs, err := ioutil.ReadFile("test.txt")
	if err != nil {
		return
	}

	str := string(bs)
	fmt.Println(str)

	var x list.List
	x.PushBack(1)
	x.PushBack(2)
	x.PushBack(3)
	for e := x.Front(); e != nil; e = e.Next() {
		fmt.Println(e.Value.(int))
	}

	// create a hasher
	h := crc32.NewIEEE()
	// write our data to it
	h.Write([]byte("test"))
	// calculate the crc32 checksum
	v := h.Sum32()
	fmt.Println(v)

	h1 := sha256.New()
	h1.Write([]byte("test"))
	bs1 := h.Sum([]byte{})
	fmt.Println(bs1)

	pkg.SayHello()
}

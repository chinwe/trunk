package main

import (
	"fmt"
	"reflect"
)

type User struct {
	ID   uint
	Name string
	Age  int
}

func (u User) Hello() {
	fmt.Println("hello go.")
}

func main() {

	u := User{1, "OK", 18}

	Info(u)
}

func Info(o interface{}) {
	t := reflect.TypeOf(o)
	fmt.Println("Type", t.Name())

	v := reflect.ValueOf(o)
	fmt.Println("Filelds:")

	for index := 0; index < t.NumField(); index++ {
		f := t.Field(index)
		val := v.Field(index).Interface()

		fmt.Printf("%s: %v = %v\n", f.Name, f.Type, val)
	}
}

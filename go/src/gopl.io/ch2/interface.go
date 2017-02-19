package main

import (
	"fmt"
)

type USB interface {
	Name() string
	Connect()
}

type PhoneUSB struct {
	name string
}

func (pu PhoneUSB) Name() string {
	return pu.name
}

func (pu PhoneUSB) Connect() {
	fmt.Println("Connect ", pu.name)
}

func main() {
	var pu PhoneUSB
	pu.name = "Nokia 6"

	pu.Connect()
}

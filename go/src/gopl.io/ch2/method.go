package main

import (
	"fmt"
)

// struct value type
type person struct {
	Name    string
	Age     int
	sex     int
	Contact struct {
		Phone, City string
	}
}

type ID uint

func main() {
	p := &person{
		Name: "joe",
		Age:  19,
	}
	p.Contact.Phone = "10086"
	p.Contact.City = "HangZhou"
	p.sex = 1

	p.Print()

	var id ID
	id = 0

	id.Inc(100)

	fmt.Println(id)
}

func (p *person) Print() {
	p.sex = 0
	fmt.Println(*p)
}

func (id *ID) Inc(num uint) {
	*id += ID(num)
}

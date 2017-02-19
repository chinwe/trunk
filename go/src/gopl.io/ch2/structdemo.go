package main

import (
	"fmt"
)

// struct value type
type person struct {
	Name    string
	Age     int
	Contact struct {
		Phone, City string
	}
}

type human struct {
	Sex int
}

type teacher struct {
	human
	Title string
}

type stuent struct {
	human
	Class string
}

func main() {
	p := &person{
		Name: "joe",
		Age:  19,
	}
	p.Contact.Phone = "10086"
	p.Contact.City = "HangZhou"

	foreverEighteen(p)

	fmt.Println(p)

	t := teacher{
		Title: "Master",
		human: human{Sex: 1},
	}

	fmt.Println(t)
}

func foreverEighteen(p *person) {
	p.Age = 18
	fmt.Println(p.Age)
}

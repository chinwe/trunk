package oop_test

import (
	"fmt"
	"testing"
)

type Employee struct {
	Id   string
	Name string
	Age  int
}

func (e *Employee) ToString() string {
	return fmt.Sprintf("{ID: %s, Name: %s, Age: %d}", e.Id, e.Name, e.Age)
}

func TestEmployee(t *testing.T) {
	e1 := Employee{"1", "Tom", 18}
	t.Log(e1)

	e2 := new(Employee)
	e2.Id, e2.Name, e2.Age = "2", "Jerry", 17
	t.Log(e2.ToString())
}

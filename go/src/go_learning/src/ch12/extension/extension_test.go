package extension_test

import (
	"fmt"
	"testing"
)

type Code string

type Programmer interface {
	WriteHelloWorld() Code
}

type GoProgrammer struct {
}

func (gp *GoProgrammer) WriteHelloWorld() Code {
	return "fmt.Println(\"Hello World\")"
}

type JavaProgrammer struct {
}

func (jp *JavaProgrammer) WriteHelloWorld() Code {
	return "System.out.println(\"Hello World\")"
}

func WriteFirstProgram(p Programmer) {
	fmt.Printf("%T %v\n", p, p.WriteHelloWorld())
}

func TestPolymorphism(t *testing.T) {
	gp := new(GoProgrammer)
	jp := new(JavaProgrammer)
	WriteFirstProgram(gp)
	WriteFirstProgram(jp)
}

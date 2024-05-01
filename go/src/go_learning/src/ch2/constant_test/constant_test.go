package constant_test

import "testing"

const (
	Monday = iota + 1
	Tuesday
	Wednesday
)

const (
	Readable = 1 << iota
	Writable
	Executable
)

func TestConstant(t *testing.T) {
	t.Log(Tuesday, Wednesday)
}

func TestConstant1(t *testing.T) {
	a := 7
	t.Log(a&Readable == Readable, a&Writable == Writable, a&Executable == Executable)
}

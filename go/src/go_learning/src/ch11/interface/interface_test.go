package interface_test

import "testing"

type Programmer interface {
	WriteHelloWorld() string
}

type GoProgrammer struct {
}

func (gp *GoProgrammer) WriteHelloWorld() string {
	return "fmt.Println(\"Hello World\")"
}

func TestClient(t *testing.T) {
	var p = &GoProgrammer{}
	t.Log(p.WriteHelloWorld())
}

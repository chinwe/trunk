package empty_interface

import (
	"fmt"
	"testing"
)

func DoSth(p interface{}) {
	/*
		if i, ok := p.(int); ok {
			fmt.Println("int ", i)
			return
		}
		if i, ok := p.(string); ok {
			fmt.Println("string ", i)
			return
		}*/

	switch v := p.(type) {
	case int:
		fmt.Println("int ", v)
	case string:
		fmt.Println("string ", v)
	default:
		fmt.Println("Unknow Type")
	}
}

func TestEmptyInterfaceAssertion(t *testing.T) {
	DoSth(1)
	DoSth("1")
	DoSth([]int{})
}

package type_test

import "testing"

func TestImplicit(t *testing.T) {
	var a uint8 = 128
	var b int32 = 999
	b = int32(a)
	t.Log(b)
}

func TestPointer(t *testing.T) {
	a := 1
	aPtr := &a
	*aPtr = 2
	t.Log(a, aPtr)
	t.Logf("%T %T", a, aPtr)
}

func TestString(t *testing.T) {
	var s string
	t.Log(len(s))
}

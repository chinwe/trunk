package string_test

import "testing"

func TestString(t *testing.T) {
	var s string
	t.Log(s)

	s = "hello"
	t.Log(s, len(s))

	s = "\xE4\xBA\xBB\xFF"
	t.Log(s, len(s))

	s = "中"
	c := []rune(s)
	t.Logf("unicode: %x utf-8: %x", c[0], s)

}

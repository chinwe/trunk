package chapter9_test

import "math"
import "testing"

func TestMax(t *testing.T) {
	v := math.Max(1, 2)
	if v != 1.5 {
		t.Error("Expected 1.5, got ", v)
	}
}

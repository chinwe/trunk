package array_test

import "testing"

func TestArrayInit(t *testing.T) {
	var arr [3]int
	t.Log(arr[1], arr[2])

	arr1 := [4]int{1, 2, 3, 4}
	arr2 := [...]int{1, 2, 3, 4, 5}
	t.Log(arr1[2], arr2[3])
}

func TestArrayTravel(t *testing.T) {
	arr := [...]int{1, 2, 3, 4, 5}
	for i, e := range arr {
		t.Log(i, e)
	}

	for _, e := range arr {
		t.Log(e)
	}
}

func TestArraySlice(t *testing.T) {
	arr := [...]int{1, 2, 3, 4, 5}
	t.Log(arr[0:3])
	t.Log(arr[3:])
}

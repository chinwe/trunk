package map_test

import (
	"testing"
)

func TestMapInit(t *testing.T) {
	m1 := map[int]int{1: 1, 2: 4, 3: 9}
	t.Log(m1, len(m1))

	m2 := map[int]int{}
	t.Log(m2, len(m2))

	m3 := make(map[int]int, 10)
	t.Log(m3, len(m3))
}

func TestAccessNotExistingKey(t *testing.T) {
	m1 := map[int]int{}
	t.Log(m1[1])
	m1[2] = 0
	t.Log(m1[2])

	if v, ok := m1[3]; ok {
		t.Log(v)
	} else {
		t.Log("Key not exist.")
	}
}

func TestTravelMap(t *testing.T) {
	m1 := map[int]int{1: 1, 2: 4, 3: 9}

	for k, v := range m1 {
		t.Log(k, v)
	}
}

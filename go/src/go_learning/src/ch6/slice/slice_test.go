package slice_test

import "testing"

func TestSliceInit(t *testing.T) {
	var s0 []int
	t.Log(len((s0)), cap(s0))
	s0 = append(s0, 1)
	t.Log(len((s0)), cap(s0))

	s1 := []int{1, 2, 3, 4}
	t.Log(len((s1)), cap(s1))

	s2 := make([]int, 3, 5)
	t.Log(len((s2)), cap(s2))
}

func TestSliceGrowing(t *testing.T) {
	s := []int{}
	for i := 0; i < 10; i++ {
		s = append(s, i)
		t.Log(len((s)), cap(s))
	}
}

func TestSliceShareMemory(t *testing.T) {
	year := []string{
		"Jan", "Feb", "Mar", "Apr", "May", "Jun",
		"Jul", "Aug", "Sep", "Oct", "Nov", "Dec",
	}

	q2 := year[3:6]
	t.Log(q2, len(q2), cap(q2))

	summer := year[5:8]
	t.Log(summer, len(summer), cap(summer))

	summer[0] = "Unknow"
	t.Log(q2, year)
}

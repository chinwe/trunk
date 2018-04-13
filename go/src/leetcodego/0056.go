/*
56. 合并区间


给出一个区间的集合, 请合并所有重叠的区间。

示例：
给出 [1,3],[2,6],[8,10],[15,18],
返回 [1,6],[8,10],[15,18].

*/

package main

import (
	"fmt"
	"sort"
)

type Interval struct {
	Start int
	End   int
}

type IntervalSlice []Interval

func (s IntervalSlice) Len() int           { return len(s) }
func (s IntervalSlice) Swap(i, j int)      { s[i], s[j] = s[j], s[i] }
func (s IntervalSlice) Less(i, j int) bool { return s[i].Start < s[j].Start }

/**
 * Definition for an interval.
 * type Interval struct {
 *	   Start int
 *	   End   int
 * }
 */
func merge(intervals []Interval) []Interval {
	ret := []Interval{}
	sort.Sort(IntervalSlice(intervals))
	if len(intervals) == 0 {
		return ret
	}
	ret = append(ret, intervals[0])
	for i := 1; i < len(intervals); i++ {
		idx := len(ret) - 1
		if intervals[i].Start <= ret[idx].End {
			if intervals[i].End > ret[idx].End {
				ret[idx].End = intervals[i].End
			}
		} else {
			ret = append(ret, intervals[i])
		}
	}

	return ret
}

func main() {
	intervals := []Interval{{1, 3}, {2, 6}, {8, 10}, {15, 18}}
	fmt.Println(merge(intervals))

	intervals1 := []Interval{{2, 3}, {4, 5}, {6, 7}, {8, 9}, {1, 10}}
	fmt.Println(merge(intervals1))
}

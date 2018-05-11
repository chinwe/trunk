/*
830. Positions of Large Groups

In a string S of lowercase letters, these letters form consecutive groups of the same character.
For example, a string like S = "abbxxxxzyy" has the groups "a", "bb", "xxxx", "z" and "yy".
Call a group large if it has 3 or more characters.  We would like the starting and ending positions of every large group.
The final answer should be in lexicographic order.

Example 1:
Input: "abbxxxxzzy"
Output: [[3,6]]
Explanation: "xxxx" is the single large group with starting  3 and ending positions 6.

Example 2:
Input: "abc"
Output: []
Explanation: We have "a","b" and "c" but no large group.

Example 3:
Input: "abcdddeeeeaabbbcd"
Output: [[3,5],[6,9],[12,14]]

Note:  1 <= S.length <= 1000
*/

package main

import (
	"fmt"
)

func largeGroupPositions(S string) [][]int {
	group := [][]int{}
	cntG := 0
	for i := 1; i < len(S); i++ {
		if S[i] == S[i-1] {
			cntG++
		} else {
			if cntG >= 2 {
				group = append(group, []int{i - 1 - cntG, i - 1})
			}
			cntG = 0
		}
	}

	if cntG >= 2 {
		group = append(group, []int{len(S) - 1 - cntG, len(S) - 1})
	}

	return group
}

func main() {
	fmt.Println(largeGroupPositions("abbxxxxzzy"))
	fmt.Println(largeGroupPositions("abbxxxx"))
	fmt.Println(largeGroupPositions("abc"))
	fmt.Println(largeGroupPositions("abcdddeeeeaabbbcd"))
	fmt.Println(largeGroupPositions(""))
}

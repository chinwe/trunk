/*
206. 反转链表

反转一个单链表。

进阶:
链表可以迭代或递归地反转。你能否两个都实现一遍？
*/

package main

import (
	"fmt"
)

type ListNode struct {
	Val  int
	Next *ListNode
}

/**
 * Definition for singly-linked list.
 * type ListNode struct {
 *     Val int
 *     Next *ListNode
 * }
 */
func reverseList(head *ListNode) *ListNode {
	if head == nil || head.Next == nil {
		return head
	}

	cur := head.Next
	last := head
	head.Next = nil
	for cur != nil {
		nextNode := cur.Next
		cur.Next = last
		last = cur
		cur = nextNode
	}

	return last
}

func printList(l *ListNode) {
	for ; l != nil; l = l.Next {
		fmt.Print(l.Val, "->")
	}
	fmt.Println("nil")
}

func main() {
	head := new(ListNode)
	head.Val = 1
	head.Next = new(ListNode)
	head.Next.Val = 2
	printList(head)
	printList(reverseList(head))
}

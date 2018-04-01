/*
83. 删除排序链表中的重复元素

给定一个排序链表，删除所有重复的元素使得每个元素只留下一个。

案例：
给定 1->1->2，返回 1->2
给定 1->1->2->3->3，返回 1->2->3

*/
package main

import (
	"fmt"
)

type ListNode struct {
	Val  int
	Next *ListNode
}

func printList(l *ListNode) {
	for ; l != nil; l = l.Next {
		fmt.Print(l.Val, "->")
	}
	fmt.Println("nil")
}

/**
 * Definition for singly-linked list.
 * type ListNode struct {
 *     Val int
 *     Next *ListNode
 * }
 */
func deleteDuplicates(head *ListNode) *ListNode {
	newHead := head
	for head != nil && head.Next != nil {
		if head.Val == head.Next.Val {
			head.Next = head.Next.Next
		} else {
			head = head.Next
		}
	}
	return newHead
}

func main() {
	l1 := new(ListNode)
	l1.Val = 1
	l1.Next = new(ListNode)
	l1.Next.Val = 1
	l1.Next.Next = new(ListNode)
	l1.Next.Next.Val = 1
	l1.Next.Next.Next = new(ListNode)
	l1.Next.Next.Next.Val = 2
	l1.Next.Next.Next.Next = new(ListNode)
	l1.Next.Next.Next.Next.Val = 2

	printList(deleteDuplicates(l1))
}

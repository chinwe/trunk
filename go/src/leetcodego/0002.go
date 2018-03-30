/*
2. 两数相加

给定两个非空链表来代表两个非负数，位数按照逆序方式存储，它们的每个节点只存储单个数字。将这两数相加会返回一个新的链表。
你可以假设除了数字 0 之外，这两个数字都不会以零开头。

示例：
输入：(2 -> 4 -> 3) + (5 -> 6 -> 4)
输出：7 -> 0 -> 8
原因：342 + 465 = 807

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
 */
func addTwoNumbers(l1 *ListNode, l2 *ListNode) *ListNode {
	var l *ListNode
	var header *ListNode
	for {
		if l1 != nil || l2 != nil {
			if header == nil {
				// 第一个节点
				header = new(ListNode)
				l = header
			} else if l.Next == nil {
				// 未进位，新节点
				l.Next = new(ListNode)
				l = l.Next
			} else if l.Next != nil {
				// 进位，新节点
				l = l.Next
			}

			if l1 != nil {
				l.Val += l1.Val
				l1 = l1.Next
			}

			if l2 != nil {
				l.Val += l2.Val
				l2 = l2.Next
			}

			if l.Val/10 > 0 {
				// 进位
				l.Val = l.Val % 10
				l.Next = new(ListNode)
				l.Next.Val++
			}
		} else {
			break
		}
	}
	return header
}

func main() {
	l1 := new(ListNode)
	l1.Val = 2
	l1.Next = new(ListNode)
	l1.Next.Val = 4
	l1.Next.Next = new(ListNode)
	l1.Next.Next.Val = 3

	l2 := new(ListNode)
	l2.Val = 5
	l2.Next = new(ListNode)
	l2.Next.Val = 6
	l2.Next.Next = new(ListNode)
	l2.Next.Next.Val = 4

	printList(l1)
	printList(l2)
	printList(addTwoNumbers(l1, l2))
}

/*
19. 删除链表的倒数第N个节点

给定一个链表，删除链表的倒数第 n 个节点并返回头结点。

例如，
给定一个链表: 1->2->3->4->5, 并且 n = 2.
当删除了倒数第二个节点后链表变成了 1->2->3->5.

说明:给的 n 始终是有效的。尝试一次遍历实现。

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
func removeNthFromEnd(head *ListNode, n int) *ListNode {
	newHead, delNode := head, head
	for head != nil && n > 0 {
		head = head.Next
		n--
	}

	for head != nil && head.Next != nil {
		delNode = delNode.Next
		head = head.Next
	}

	if head == nil && delNode == newHead {
		newHead = delNode.Next
	} else {
		delNode.Next = delNode.Next.Next
	}
	return newHead
}

func main() {
	l1 := new(ListNode)
	l1.Val = 1
	l1.Next = new(ListNode)
	l1.Next.Val = 2
	l1.Next.Next = new(ListNode)
	l1.Next.Next.Val = 3
	l1.Next.Next.Next = new(ListNode)
	l1.Next.Next.Next.Val = 4
	l1.Next.Next.Next.Next = new(ListNode)
	l1.Next.Next.Next.Next.Val = 5
	printList(removeNthFromEnd(l1, 2))
}

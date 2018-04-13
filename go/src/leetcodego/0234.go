/*
234. 回文链表
请检查一个链表是否为回文链表。

进阶：
你能在 O(n) 的时间和 O(1) 的额外空间中做到吗？
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
func isPalindrome(head *ListNode) bool {
	if head == nil || head.Next == nil {
		return true
	}

	slow, fast := head, head
	for fast.Next != nil && fast.Next.Next != nil {
		slow = slow.Next
		fast = fast.Next.Next
	}

	//对链表后半段进行反转
	midNode := slow
	firNode := slow.Next //后半段链表的第一个节点
	cur := firNode.Next  //插入节点从第一个节点后面一个开始
	firNode.Next = nil   //第一个节点最后会变最后一个节点
	for cur != nil {
		nextNode := cur.Next //保存下次遍历的节点
		cur.Next = midNode.Next
		midNode.Next = cur
		cur = nextNode
	}

	//反转之后对前后半段进行比较
	slow = head
	fast = midNode.Next
	for fast != nil {
		if fast.Val != slow.Val {
			return false
		}

		slow = slow.Next
		fast = fast.Next
	}

	return true
}

func main() {
	head := new(ListNode)
	head.Val = 1
	head.Next = new(ListNode)
	head.Next.Val = 2

	fmt.Println(isPalindrome(head))
}

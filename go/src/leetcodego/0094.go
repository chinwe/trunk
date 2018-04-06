/*
94. 中序遍历二叉树

给定一个二叉树，返回其中序遍历。

例如：
给定二叉树 [1,null,2,3],

   1
    \
     2
    /
   3
返回 [1,3,2].

说明: 递归算法很简单，你可以通过迭代算法完成吗？

*/

package main

import (
	"container/list"
	"fmt"
)

type TreeNode struct {
	Val   int
	Left  *TreeNode
	Right *TreeNode
}

/**
 * Definition for a binary tree node.
 * type TreeNode struct {
 *     Val int
 *     Left *TreeNode
 *     Right *TreeNode
 * }
 */
func inorderTraversal(root *TreeNode) []int {
	order := []int{}
	stack := list.New()
	pNode := root
	for pNode != nil || stack.Len() != 0 {
		if pNode != nil {
			stack.PushBack(pNode)
			pNode = pNode.Left
		} else {
			pNode = stack.Back().Value.(*TreeNode)
			stack.Remove(stack.Back())
			order = append(order, pNode.Val)
			pNode = pNode.Right
		}
	}

	return order
}

func main() {
	root := new(TreeNode)
	root.Val = 10
	root.Left = new(TreeNode)
	root.Left.Val = 5
	root.Right = new(TreeNode)
	root.Right.Val = 15
	root.Right.Left = new(TreeNode)
	root.Right.Left.Val = 6
	root.Right.Right = new(TreeNode)
	root.Right.Right.Val = 20
	fmt.Println(inorderTraversal(root))
	fmt.Println(inorderTraversal(nil))
}

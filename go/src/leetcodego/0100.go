/*
100. 相同的树

给定两个二叉树，写一个函数来检查它们是否相同。
如果两棵树在结构上相同并且节点具有相同的值，则认为它们是相同的。

示例 1:
输入 :     1          1
          / \       / \
         2   3     2   3

        [1,2,3],   [1,2,3]
输出: true

示例 2:
输入  :    1          1
          /           \
         2             2

        [1,2],     [1,null,2]
输出: false

例 3:
输入 :     1          1
          / \       / \
         2   1     1   2

        [1,2,1],   [1,1,2]
输出: false
*/
package main

import (
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
func isSameTree(p *TreeNode, q *TreeNode) bool {
	if p != nil && q != nil && p.Val == q.Val {
		return isSameTree(p.Left, q.Left) && isSameTree(p.Right, q.Right)
	} else if p == nil && q == nil {
		return true
	} else {
		return false
	}
}

func main() {
	p := new(TreeNode)
	p.Val = 1
	p.Left = new(TreeNode)
	p.Left.Val = 2

	q := new(TreeNode)
	q.Val = 1
	q.Right = new(TreeNode)
	q.Right.Val = 2

	fmt.Println(isSameTree(p, q))
}

/*
98. 验证二叉搜索树

给定一个二叉树，判断其是否是一个有效的二叉搜索树。
一个二叉搜索树有如下定义：
左子树只包含小于当前节点的数。
右子树只包含大于当前节点的数。
所有子树自身必须也是二叉搜索树。

示例 1：
    2
   / \
  1   3
二叉树[2,1,3], 返回 true.

示例 2：
    1
   / \
  2   3
二叉树 [1,2,3], 返回 false.
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
func isValidBST(root *TreeNode) bool {
	return isValid(root, nil, nil)
}

func isValid(root *TreeNode, min *TreeNode, max *TreeNode) bool {
	if root == nil {
		return true
	}

	if (max != nil && root.Val >= max.Val) || (min != nil && root.Val <= min.Val) {
		return false
	}

	return isValid(root.Left, min, root) && isValid(root.Right, root, max)
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
	fmt.Println(isValidBST(root))
}

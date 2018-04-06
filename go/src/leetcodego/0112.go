/*
112. 路径总和

给定一棵二叉树和一个总和，确定该树中是否存在根到叶的路径，这条路径的所有值相加等于给定的总和。

例如：
给定下面的二叉树和 总和 = 22，

              5
             / \
            4   8
           /   / \
          11  13  4
         /  \      \
        7    2      1
返回 true, 因为存在总和为 22 的根到叶的路径 5->4->11->2。

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
func hasPathSum(root *TreeNode, sum int) bool {
	if root == nil {
		return false
	}

	if sum == root.Val && root.Left == nil && root.Right == nil {
		return true
	}

	return (root.Left != nil && hasPathSum(root.Left, sum-root.Val)) || (root.Right != nil && hasPathSum(root.Right, sum-root.Val))
}

func main() {
	root := new(TreeNode)
	root.Val = 5
	root.Left = new(TreeNode)
	root.Left.Val = 2
	root.Right = new(TreeNode)
	root.Right.Val = 1

	fmt.Println(hasPathSum(root, 7))
	fmt.Println(hasPathSum(root, 3))
	fmt.Println(hasPathSum(nil, 0))
}

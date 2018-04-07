/*
155. 最小栈

设计一个支持 push，pop，top 操作，并能在常量时间内检索最小元素的栈。

push(x) -- 将元素x推入栈中。
pop() -- 删除栈顶的元素。
top() -- 获取栈顶元素。
getMin() -- 检索栈中的最小元素。
示例:

MinStack minStack = new MinStack();
minStack.push(-2);
minStack.push(0);
minStack.push(-3);
minStack.getMin();   --> 返回 -3.
minStack.pop();
minStack.top();      --> 返回 0.
minStack.getMin();   --> 返回 -2.
*/

package main

import (
	"fmt"
	"math"
)

type MinStack struct {
	data []int
	min  int
}

/** initialize your data structure here. */
func Constructor() MinStack {
	ms := MinStack{nil, math.MaxInt32}
	return ms
}

func (this *MinStack) Push(x int) {
	if x <= this.min {
		this.data = append(this.data, this.min)
		this.min = x
	}
	this.data = append(this.data, x)
}

func (this *MinStack) Pop() {
	top := this.Top()
	this.data = this.data[0 : len(this.data)-1]
	if top == this.min {
		this.min = this.Top()
		this.data = this.data[0 : len(this.data)-1]
	}
}

func (this *MinStack) Top() int {
	return this.data[len(this.data)-1]
}

func (this *MinStack) GetMin() int {
	return this.min
}

/**
 * Your MinStack object will be instantiated and called as such:
 * obj := Constructor();
 * obj.Push(x);
 * obj.Pop();
 * param_3 := obj.Top();
 * param_4 := obj.GetMin();
 */

func main() {
	minStack := Constructor()
	minStack.Push(-2)
	minStack.Push(0)
	minStack.Push(-3)
	fmt.Println(minStack.GetMin()) // 返回 -3.
	minStack.Pop()
	fmt.Println(minStack.Top())    // 返回 0.
	fmt.Println(minStack.GetMin()) // 返回 -2.
}

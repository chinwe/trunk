//
//  main.swift
//  ASwiftTour
//
//  Created by 张俊伟 on 16/6/26.
//  Copyright © 2016年 张俊伟. All rights reserved.
//

//import Foundation

//Simple Value
var count = 12
let fConstFour : Float = 4
print("fConstFour = ", fConstFour)

//值永远不会被隐式转换为其他类型。需要显示转换。
let label = "The width is "
let width = 94

//不使用String显示转换，会提示一下错误
//Binary operator '+' cannot be applied to operands of type 'String' and 'Int'
let widthLabel = label + String(width)
print(widthLabel)

//有一种更简单的把值转换成字符串的方法:把值写到括号中,并且在括号之前写一个反斜杠
var swift_version = 2.0
var hello = "Hello Swift \(swift_version)"
print(hello)

swift_version = 3.0
hello = "Hello Swift \(swift_version)"
print(hello)

//数组和字典
var alpha_table = ["A", "B", "C", "D"]
var name_dict = ["Zhangshan" : 12, "Lisi" : 18]
print(alpha_table, name_dict)

alpha_table[3] = "E"
name_dict["Zhangshan"] = 19
print(alpha_table, name_dict)

//empty
alpha_table = []
name_dict = [:]
print(alpha_table, name_dict)

let emptyArr = [Int]()
let emptyDic = [String: Int]()

//Control flow
//condition: if switch
//loop: for-in for while repeat-while
var bToday = false
if bToday {
    print("Today")
}
else {
    print("Tommorrow")
}

//在if语句中，条件必须是一个表达式，这意味着 if iScore { ... }这样的代码将报错

var optionName : String? = nil
var greeting = "Hello!"
if let name = optionName {
    greeting = "Hello, \(name)"
}
else {
    greeting = "Hello, nil"
}
print(greeting)

//use ??
let nickName : String? = nil
let fullName = "Lisi"
let informalGreeting = "Hi \(nickName ?? fullName)"
print(informalGreeting)

//switch 支持任意类型的数据以及各种比较操作——不仅仅是整数以及测试相等。
let vegetable = "red pepper"
switch vegetable {
case "celery":
        print("Add some raisins and make ants on a log.")
case "cucumber", "watercress":
    print("That would make a good tea sandwich.")
case let x where x.hasSuffix("pepper"):
    print("Is it a spicy \(x)?")
default:
    print("Everything tastes good in soup.")
}

//for in
let numbers = [1, 23, 12, 45, 67, 222]
var largest = numbers[0]

for i in numbers {
    if i > largest {
        largest = i
    }
}

print(largest)

//while
var n = 2
while n < 100 {
    n = n * 2
}

var m = 2

repeat {
    m = m * 2
} while m < 100

for i in 0..<100 {
    m += i
}

for i in 0...50 {
    m += i
}



























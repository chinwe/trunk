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


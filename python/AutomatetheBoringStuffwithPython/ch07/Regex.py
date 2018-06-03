#!/usr/bin/env python3
# -*- coding:utf-8 -*- 

import re

'''
虽然在Python中使用正则表达式有几个步骤，但每一步都相当简单。
1．用import re导入正则表达式模块。
2．用re.compile()函数创建一个Regex对象（记得使用原始字符串）。
3．向Regex对象的search()方法传入想查找的字符串。它返回一个Match对象。
4．调用Match对象的group()方法，返回实际匹配文本的字符串。
'''

phoneNumRegex = re.compile(r'\d\d\d-\d\d\d-\d\d\d\d', re.I)
mo = phoneNumRegex.search('My number is 415-555-4242.')
print('Phone number found: ' + mo.group())


'''
缩写字符分类 表示
\d 0到9的任何数字
\D 除0到9的数字以外的任何字符
\w 任何字母、数字或下划线字符（可以认为是匹配“单词”字符
\W 除字母、数字和下划线以外的任何字符
\s 空格、制表符或换行符（可以认为是匹配“空白”字符）
\S 除空格、制表符和换行符以外的任何字符
'''

# 用sub()方法替换字符串
agentNamesRegex = re.compile(r'Agent (\w)\w*')
print(agentNamesRegex.sub(r'\1****', 'Agent Alice told Agent Carol that Agent Eve knew Agent Bob was a double agent.'))

# 实践项目
# 强口令检测
# 写一个函数，它使用正则表达式，确保传入的口令字符串是强口令。
# 强口令的定义是：长度不少于8个字符，同时包含大写和小写字符，至少有一位数字。
# 你可能需要用多个正则表达式来测试该字符串，以保证它的强度。

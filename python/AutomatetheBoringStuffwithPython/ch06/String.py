#!/usr/bin/env python
# -*- coding:utf-8 -*- 

import pyperclip

# “原始字符串”完全忽略所有的转义字符，打印出字符串中所有的倒斜杠。
print(r'That is Carol\'s cat.')

# 多行字符串
print('''Dear Alice,
Eve's cat has been arrested for catnapping, cat burglary, and extortion.
Sincerely,
Bob''')

# 多行注释
"""This is a test Python program.
Written by Al Sweigart al@inventwithpython.com

This program was designed for Python 3, not Python 2.
"""

# 大小写转换
spam = 'Hello world!'
spam = spam.upper()
spam = spam.lower()
print(spam.isupper())

# 开始结束判断
'Hello world!'.startswith('Hello')
'Hello world!'.endswith('world!')

# 拼接和分割
print(', '.join(['cats', 'rats', 'bats']))

spam = '''Dear Alice,
How have you been? I am fine.
There is a container in the fridge
that is labeled "Milk Experiment".
Please do not drink it.
Sincerely,
Bob'''
print(spam.split('\n'))

# 用rjust()、ljust()和center()方法对齐文本
print('Hello'.rjust(20, '*'))

# 用strip()、rstrip()和lstrip()删除空白字符
spam = ' Hello World '
print(spam.strip())

# 用pyperclip模块拷贝粘贴字符串
pyperclip.copy('Hello world!')
pyperclip.paste()
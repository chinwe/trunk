#!/usr/bin/env python
# -*- coding:utf-8 -*- 

import copy

spam = ['hello', 'hi', 'howdy', 'heyas']
spam.index('hello')
spam.append('moose')
spam.insert(1, 'chicken')
spam.remove('hi')

spam.sort(reverse=True)

# 如果要复制的列表中包含了列表，那就使用copy.deepcopy()函数来代替。deepcopy()函数将同时复制它们内部的列表。

spam = ['A', 'B', 'C', 'D']
spamOne = spam
cheese = copy.copy(spam)
spamOne[0] = 41
cheese[1] = 42
print(spam, spam, cheese)
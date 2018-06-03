#!/usr/bin/env python3
# -*- coding:utf-8 -*- 

import os
import shelve

'''
8.1.1 Windows上的倒斜杠以及OS X和Linux上的正斜杠
在Windows上，路径书写使用倒斜杠作为文件夹之间的分隔符。但在OS X和Linux上，使用正斜杠作为它们的路径分隔符。如果想要程序运行在所有操作系统上，在编写Python脚本时，就必须处理这两种情况。
好在，用os.path.join()函数来做这件事很简单
'''
print(os.path.join('usr', 'bin', 'spam'))

'''
8.1.2 当前工作目录
利用os.getcwd()函数，可以取得当前工作路径的字符串，并可以利用os.chdir()改变它。
'''
print(os.getcwd())

'''
8.1.4 用os.makedirs()创建新文件夹
程序可以用os.makedirs()函数创建新文件夹（目录）
'''

'''
8.1.6 处理绝对路径和相对路径
调用os.path.abspath(path)将返回参数的绝对路径的字符串。这是将相对路径转换为绝对路径的简便方法。
调用os.path.isabs(path)，如果参数是一个绝对路径，就返回True，如果参数是一个相对路径，就返回False。
调用os.path.relpath(path, start)将返回从start路径到path的相对路径的字符串。如果没有提供start，就使用当前工作目录作为开始路径。
'''
print(os.path.abspath('.'))

'''
8.1.7 查看文件大小和文件夹内容  调用os.path.getsize(path)将返回path参数中文件的字节数。 
调用os.listdir(path)将返回文件名字符串的列表，包含path参数中的每个文件（请注意，这个函数在os模块中，而不是os.path）。
'''
print(os.listdir('.'))

'''
8.1.8 检查路径有效性 
如果path参数所指的文件或文件夹存在，调用os.path.exists(path)将返回True，否则返回False。 如果path参数存在，并且是一个文件，调用os.path.isfile(path)将返回True，否则返回False。 如果path参数存在，并且是一个文件夹，调用os.path.isdir(path)将返回True，否则返回False。
'''

# 在Python中，读写文件有3个步骤：
# 1．调用open()函数，返回一个File对象。
# 2．调用File对象的read()或write()方法。
# 3．调用File对象的close()方法，关闭该文件。
baconFile = open('bacon.txt', 'w')
baconFile.write('Hello world!\n')
baconFile.close()

baconFile = open('bacon.txt')
print(baconFile.read())
baconFile.close()

'''
8.3 用shelve模块保存变量 
利用shelve模块，你可以将Python程序中的变量保存到二进制的shelf文件中。这样，程序就可以从硬盘中恢复变量的数据。shelve模块让你在程序中添加“保存”和“打开”功能。
'''
shelfFile = shelve.open('mydata')
cats = ['Zophie', 'Pooka', 'Simon']
shelfFile['cats'] = cats
shelfFile.close()

shelfFile = shelve.open('mydata')
print(list(shelfFile.values()))
shelfFile.close()
#!/usr/bin/env python3
# -*- coding:utf-8 -*- 

第10章 调试
	第10章 调试 
2017-11-11 07:56
程序员之间流传着一个老笑话：“编码占了编程工作量的90%，调试占了另外90%。”
	10.1 抛出异常
 2017-11-17
抛出异常使用raise语句。在代码中，raise语句包含以下部分：
raise关键字；
对Exception函数的调用；
传递给Exception函数的字符串，包含有用的出错信息。
	10.2 取得反向跟踪的字符串
 2017-11-17
只要抛出的异常没有被处理，Python 就会显示反向跟踪。但你也可以调用traceback.format_exc()，得到它的字符串形式。如果你希望得到异常的反向跟踪的信息，但也希望except语句优雅地处理该异常，这个函数就很有用。
	10.3 断言
 2017-11-17
assert podBayDoorStatus == 'open', 'The pod bay doors need to be "open".'
	10.3.1 在交通灯模拟中使用断言
 2017-11-17
断言针对的是程序员的错误，而不是用户的错误。对于那些可以恢复的错误（诸如文件没有找到，或用户输入了无效的数据），请抛出异常，而不是用assert语句检测它。
	10.3.2 禁用断言
 2017-11-17
在运行Python时传入-O选项，可以禁用断言。
	10.4.1 使用日志模块
 2017-11-19
import logging
logging.basicConfig(level=logging.DEBUG, format=' %(asctime)s - %(levelname)s
-   %(message)s')
	10.4.3 日志级别
 2017-11-19
日志消息的好处在于，你可以随心所欲地在程序中想加多少就加多少，稍后只要加入一次logging.disable（logging.CRITICAL）调用，就可以禁止日志。不像print()，logging模块使得显示和隐藏日志信息之间的切换变得很容易。
	10.4.5 将日志记录到文件
 2017-11-19
除了将日志消息显示在屏幕上，还可以将它们写入文本文件。logging.basic Config() 函数接受filename关键字参数，像这样：import logging
logging.basicConfig(filename='myProgramLog.txt', level=logging.DEBUG, format='
%(asctime)s - %(levelname)s - %(message)s')

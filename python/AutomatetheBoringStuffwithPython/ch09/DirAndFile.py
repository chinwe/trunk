#!/usr/bin/env python3
# -*- coding:utf-8 -*- 

import shutil

'''
9.1 shutil模块 
shutil（或称为shell工具）模块中包含一些函数，让你在Python程序中复制、移动、改名和删除文件。要使用shutil的函数，首先需要import shutil。
9.1.1 复制文件和文件夹 
shutil.copy()将复制一个文件，shutil.copytree()将复制整个文件夹，以及它包含的文件夹和文件。调用shutil.copytree(source, destination)，将路径source处的文件夹，包括它的所有文件和子文件夹，复制到路径destination处的文件夹。source和destination参数都是字符串。该函数返回一个字符串，是新复制的文件夹的路径。
调用shutil.move(source, destination)，将路径source处的文件夹移动到路径destination，并返回新位置的绝对路径的字符串。
9.1.2 文件和文件夹的移动与改名 
利用os模块中的函数，可以删除一个文件或一个空文件夹。但利用shutil模块，可以删除一个文件夹及其所有的内容。 用os.unlink(path)将删除path处的文件。 调用os.rmdir(path)将删除path处的文件夹。该文件夹必须为空，其中没有任何文件和文件夹。 调用shutil.rmtree(path)将删除path处的文件夹，它包含的所有文件和文件夹都会被删除。
9.1.3 永久删除文件和文件夹 
利用send2trash，比Python常规的删除函数要安全得多，因为它会将文件夹和文件发送到计算机的垃圾箱或回收站，而不是永久删除它们。如果因程序缺陷而用send2trash删除了某些你不想删除的东西，稍后可以从垃圾箱恢复。
9.2 遍历目录树 
1．当前文件夹名称的字符串。 2．当前文件夹中子文件夹的字符串的列表。 3．当前文件夹中文件的字符串的列表。 所谓当前文件夹，是指for循环当前迭代的文件夹。程序的当前工作目录，不会因为os.walk()而改变。
9.3 用zipfile模块压缩文件
利用zipfile模块中的函数，Python程序可以创建和打开（或解压）ZIP文件。
9.3.1 读取ZIP文件 
ZipFile对象的extractall()方法从ZIP文件中解压缩所有文件和文件夹，放到当前工作目录中。
9.3.2 从ZIP文件中解压缩 
要创建你自己的压缩ZIP文件，必须以“写模式”打开ZipFile对象，即传入'w'作为第二个参数（这类似于向open()函数传入'w'，以写模式打开一个文本文件）。 如果向ZipFile对象的write()方法传入一个路径，Python就会压缩该路径所指的文件，将它加到ZIP文件
9.3.3 创建和添加到ZIP文件 
>>> import zipfile>>> newZip = zipfile.ZipFile('new.zip', 'w')>>> newZip.write('spam.txt', compress_type=zipfile.ZIP_DEFLATED)>>> newZip.close()
'''


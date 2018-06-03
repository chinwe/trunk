#!/usr/bin/env python
# -*- coding:utf-8 -*- 

import random
 
 # if
name = 'Mary'

if name == 'Mary':
    print('Hello Mary')
elif name == 'Bob':
    print('Hello Bok')
else:
    print('what')

# while
i = 3
while i > 0:
    print(i)
    i = i - 1

# for
for i in range(5):
    print(random.randint(1, 10))
#!/usr/bin/env python
# -*- coding:utf-8 -*- 

import pprint

spam = {'color': 'red', 'age': 42}

# for v in spam.values():
# for k in spam.keys():
# for i in spam.items():

for k, v in spam.items():
    print('Key: ' + k + ' Value: ' + str(v))

# 'color' in spam.keys()
# 'red' not in spam.values()

print(spam.get('age', 1))

message = 'It was a bright cold day in April, and the clocks were striking thirteen.'
count = {}

for character in message:
    count.setdefault(character, 0)
    count[character] = count[character] + 1

# pprint
pprint.pprint(count)
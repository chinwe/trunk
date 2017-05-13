#!/usr/bin/env python
#encoding=utf-8
from itertools import groupby

# list comprehensions
l = [i for i in range(10) if i % 2 == 0]
print l

seq = ["one", "two", "three"]
for i, element in enumerate(seq):
    print "%d : %s" %(i, element)

# iterator and generator
i = iter("abc")
for c in i:
    print c

def fib():
    a, b = 0, 1
    while True:
        yield b
        a, b = b, a + b
f = fib()
fibs = [f.next() for i in range(10)]
print fibs

# genexp
it = (x**2 for x in range(10) if x %2 == 0)
for i in it:
    print i

# itertools
def compress(data):
    return ((len(list(group)), name) for name, group in groupby(data))

compressed = list(compress("get g uuuuuuuup"))
print compressed

def decompress(data):
    return (ch * size for size, ch in data)

print ''.join(decompress(compressed))

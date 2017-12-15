#!/usr/bin/env python
# -*- coding: utf-8 -*-

import os

# get dir abspath
d = os.path.abspath('python')

for old in os.listdir(d):
    # replace 
    new = old.replace('', '')
    # rename file name
    os.rename(os.path.join(d, old), os.path.join(d, new))

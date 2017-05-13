#!/usr/bin/env python

import requests

r = requests.get('https://www.baidu.com/')

print r.text

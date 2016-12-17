#!/usr/bin/env python
# -*- coding: utf-8 -*-

import urllib2
import re
import webbrowser

def get_page(url):
    try:
        headers = {
            'User-Agent': 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.98 Safari/537.36'
        }
        print '开始搜索......'
        print url
        request = urllib2.Request(url=url,headers=headers)
        html = urllib2.urlopen(request).read()

        return html

    except urllib2.HTTPError, e:
        print "[ERR]HTTPError " + str(e.code)

def search_BDPan_address(key):
    url_address = []
    try:
        str_url = "https://www.baidu.com/baidu?wd=%s%%20pan.baidu.com" % key
        html = get_page(str_url)

        pattern = re.compile(r"http[s]?://<em>pan.baidu.com</em>/s/\w+\s*密码\s*:\s*\w{4}")
        match = re.findall(pattern, html)
        for url in match:
            url_address.append(url.replace("<em>", "").replace("</em>", ""))

        pattern = re.compile(r"http[s]?://<em>pan.baidu.com</em>/s/\w+")
        match = re.findall(pattern, html)
        for url in match:
            url_address.append(url.replace("<em>", "").replace("</em>", ""))

        return url_address

    except urllib2.HTTPError, e:
        print "[ERR]HTTPError " + str(e.code)


while True:
    str_key_list = raw_input("请输入要搜索的资源名称(多个名称空格分割，输入回车退出):")
    if len(str_key_list) == 0:
        break

    key_list = str_key_list.split(" ")
    for key in key_list:
        url_address = search_BDPan_address(key)
        print '找到了以下链接：'
        for url_http in url_address:
            print url_http
            #webbrowser.open_new_tab(url_https) 



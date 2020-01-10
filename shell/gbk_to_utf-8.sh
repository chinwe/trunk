#!/bin/bash

# 目录下文件批量由gbk转为utf-8

convert_file()
{
    for file in `find .`
    do
        if [[ -f $file ]]
        then
            if [[ ${file##*.} == h || ${file##*.} == cpp ]]; then
                cp $file $file".bak"
                iconv -f GBK -t UTF-8 $file > $file".tmp"
                mv $file".tmp" $file
                echo $file
            fi
        fi
    done
}

convert_file
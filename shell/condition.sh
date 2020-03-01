#!/bin/bash

echo -n "请输入分数："
read score

if [ $score -ge 60 ]; then
    echo "及格"
else
    echo "未及格"
fi
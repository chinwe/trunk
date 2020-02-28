#!/bin/bash

echo "当前的进程号=$$"

./var.sh &
echo "最后的后台进程号=$!"
echo "执行的数值=$?"
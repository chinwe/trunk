#!/bin/bash

show_params()
{
    # 参数个数
    echo $#

    # 取全部变量
    echo "$*"
    echo "$@"

    # 取参数
    echo '$1'=$1
    echo '$2'=$2
    # echo '$10'=${10}
}

show_params 1 '2'
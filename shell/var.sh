#!/bin/bash

VAR=1
echo $VAR

unset VAR
echo $VAR

readonly CONST_VAR=0
# Error
# CONST_VAR=1
echo $CONST_VAR

# 环境变量
export TOMCAT_HOME='/home/me/catalina'

:<<!
全局生效
/etc/profile
source /etc/profile
!

echo $TOMCAT_HOME
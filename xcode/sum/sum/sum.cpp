/*
 *  sum.cpp
 *  sum
 *
 *  Created by 张俊伟 on 2017/8/4.
 *  Copyright © 2017年 张俊伟. All rights reserved.
 *
 */

#include <iostream>
#include "sum.hpp"
#include "sumPriv.hpp"

void sum::HelloWorld(const char * s)
{
    sumPriv *theObj = new sumPriv;
    theObj->HelloWorldPriv(s);
    delete theObj;
};

void sumPriv::HelloWorldPriv(const char * s) 
{
    std::cout << s << std::endl;
};


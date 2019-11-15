//
//  DLTry.cpp
//  DLTry
//
//  Created by mzx on 2019/9/8.
//  Copyright © 2019 mzx. All rights reserved.
//

#include <iostream>
#include "DLTry.hpp"
#include "DLTryPriv.hpp"

void DLTry::HelloWorld(const char * s)
{
    DLTryPriv *theObj = new DLTryPriv;
    theObj->HelloWorldPriv(s);
    delete theObj;
};

void DLTryPriv::HelloWorldPriv(const char * s) 
{
    std::cout << s << std::endl;
};


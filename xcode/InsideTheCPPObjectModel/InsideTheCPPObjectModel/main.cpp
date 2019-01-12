//
//  main.cpp
//  InsideTheCPPObjectModel
//
//  Created by mzx on 2018/8/26.
//  Copyright © 2018年 mzx. All rights reserved.
//

#include <iostream>

class Point2D {
public:
    float x;
    float y;
};

template <class class_type, class data_type1, class data_type2>
const char* access_order(data_type1 class_type::*mem1, data_type2 class_type::*mem2) {
    
    assert(mem1 != mem2);
    
    printf("mem1 = %p\n", mem1);
    printf("mem2 = %p\n", mem2);
    
    return mem1 == mem2 ? "mem1 occurs first" : "mem2 occurs first";
}

int main(int argc, const char * argv[]) {
    
    std::cout << access_order(&Point2D::x, &Point2D::y) << std::endl;
    return 0;
}

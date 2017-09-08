//
//  main.cpp
//  Cpp11
//
//  Created by zhangjunwei on 2017/8/5.
//  Copyright © 2017年 zhangjunwei. All rights reserved.
//

#include <iostream>
#include <vector>

int main(int argc, const char * argv[]) {

    std::vector<int> vi = { 1, 2, 3, 4, 5 };
    std::cout << *vi.rbegin() << std::endl;
    
    return 0;
}

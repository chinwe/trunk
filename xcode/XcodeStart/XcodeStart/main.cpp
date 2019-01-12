//
//  main.cpp
//  XcodeStart
//
//  Created by 张俊伟 on 2018/6/10.
//  Copyright © 2018年 zhangjunwei. All rights reserved.
//

#include <iostream>
#include <boost/unordered_map.hpp>

enum class Device {
    EncodingDevice,
};

int main(int argc, const char * argv[]) {
    // insert code here...
    std::cout << "Hello, World!\n";
    
    boost::unordered_map<int, int> hash;
    hash[0] = 1;
    
    for (auto kv : hash) {
        std::cout << kv.first << std::endl;
    }
    return 0;
}

//
//  main.cpp
//  createId
//
//  Created by mzx on 2019/11/15.
//  Copyright © 2019 mzx. All rights reserved.
//

#include <iostream>
#include <vector>
#include <bitset>

using namespace std;

constexpr size_t kMaxIdOversea = 3200;
constexpr size_t kMaxIdDomestic = 5000;

constexpr size_t maxId()
{
    return (kMaxIdDomestic > kMaxIdOversea) ? kMaxIdDomestic : kMaxIdOversea;
}

vector<size_t> existIds() {
    vector<size_t> vecIds;
    for (size_t i = 1; i <= maxId(); ++i) {
        if (i != 999) {
            vecIds.emplace_back(i);
        }
    }
    return vecIds;
}

#define MAX 10000
#define SHIFT 5
#define MASK 0x1F
#define DIGITS 32

unsigned int* pBitmap = new unsigned int[MAX / DIGITS + 1]{};

void setBit(int n)
{
    // 将逻辑位置为n的二进制位置为1
    // n>>SHIFT右移5位相当于除以32求算字节位置，n&MASK相当于对32取余即求位位置
    pBitmap[n >> SHIFT] |= (1 << (n & MASK));
}

int testBit(int n)
{
    // 测试逻辑位置为n的二进制位是否为1
    return pBitmap[n >> SHIFT] & (1 << (n & MASK));
}

void clearBit(int n)
{
    // 将逻辑位置为n的二进制位置为0
    pBitmap[n >> SHIFT] &= (~(1 << (n & MASK)));
}

int main(int argc, const char * argv[]) {
    
    constexpr size_t kMaxId = maxId() + 1;
    
    auto&& vecIds = existIds();

    // cpp
    bitset<kMaxId> bitmap;
    
    for (size_t i : vecIds) {
        bitmap.set(i);
        setBit((int)i);
    }
    
    for (size_t i = 1; i <= kMaxId; ++i) {
        if (!bitmap.test(i)) {
            std::cout << i << endl;
            break;
        }
    }
    
    // c
    for (size_t i = 1; i <= kMaxId; ++i) {
        if (!testBit((int)i)) {
            std::cout << i << endl;
            break;
        }
    }
    return 0;
}

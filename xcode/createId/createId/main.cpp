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

int main(int argc, const char * argv[]) {
    
    constexpr size_t kMaxId = maxId() + 1;
    bitset<kMaxId> bitmap;
    
    auto&& vecIds = existIds();
    
    for (size_t i : vecIds) {
        bitmap.set(i);
    }
    
    for (size_t i = 1; i <= kMaxId; ++i) {
        if (!bitmap.test(i)) {
            std::cout << i << endl;
            break;
        }
    }
    return 0;
}

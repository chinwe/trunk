//
//  main.cpp
//  LockFreeList
//
//  Created by mzx on 2019/9/11.
//  Copyright © 2019 mzx. All rights reserved.
//

#include <iostream>       // std::cout
#include <atomic>         // std::atomic
#include <thread>         // std::thread
#include <vector>         // std::vector

// a simple global linked list:
struct Node {
    int value;
    Node* next;
};

std::atomic<Node*> list_head { nullptr };

void append(int val) {     // append an element to the list
    Node* oldHead = list_head;
    Node* newNode = new Node { val, oldHead };
    
    // what follows is equivalent to: list_head = newNode, but in a thread-safe way:
    while (!list_head.compare_exchange_weak(oldHead, newNode)) {
        newNode->next = oldHead;
    }
}


int main(int argc, const char * argv[]) {

    // spawn n threads to fill the linked list:
    // const int n = std::thread::hardware_concurrency() * 20;
    int n = 30;
    std::vector<std::thread> threads;
    for (int i = 0; i < n; ++i) {
        threads.push_back(std::thread(append,i));
    }
    
    for (auto& th : threads) {
        th.join();
    }
    threads.clear();
    
    // print contents:
    for (Node* it = list_head; it != nullptr; it = it->next) {
        std::cout << ' ' << it->value;
    }
    std::cout << std::endl;
    
    // cleanup:
    Node* it;
    while ((it = list_head)) {
        list_head = it->next;
        delete it;
    }
    
    std::atomic<int> sum(0);
    n = 50;
    for (int i = 0; i < n; ++i) {
        threads.push_back(std::thread([&sum](){
            std::this_thread::sleep_for(std::chrono::seconds(1));
            sum.fetch_add(10);
        }));
    }
    
    for (auto& th : threads) {
        th.join();
    }
    
    std::cout << sum << std::endl;

    std::mutex m;
    // C++ 17
    std::scoped_lock guard(m);
    
    return 0;
}

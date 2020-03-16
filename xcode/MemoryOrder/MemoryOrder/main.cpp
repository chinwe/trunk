//
//  main.cpp
//  MemoryOrder
//
//  Created by mxz on 2020/3/13.
//  Copyright © 2020 mzx. All rights reserved.
//

#include <iostream>
#include <thread>
#include <atomic>
#include <vector>
#include <chrono>

using namespace std;

atomic<int> a;
atomic<int> b;

void thread_1() {
    int t = 1;
    a = t;
    b = 2;
}

void thread_2() {
    while(b != 2);
    cout << a << endl;
}

/*
void thread_3() {
    int t = 1;
    a.store(t,memory_order_relaxed);
    b.store(2,memory_order_release);
}

void thread_4() {
    while(b.load(memory_order_acquire) != 2);
    cout << a.load(memory_order_relaxed);
}
*/

class spinlock_mutex
{
  std::atomic_flag flag;
public:
  spinlock_mutex():
    flag(ATOMIC_FLAG_INIT)
  {}
  void lock()
  {
    while(flag.test_and_set(std::memory_order_acquire));
  }
  void unlock()
  {
    flag.clear(std::memory_order_release);
  }
};

std::vector<int> data;
std::atomic<bool> data_ready(false);

void reader_thread()
{
  while(!data_ready.load())  // 1
  {
      std::this_thread::sleep_for(std::chrono::milliseconds(1));
  }
  std::cout<<"The answer="<<data[0]<<"\n";  // 2
}
void writer_thread()
{
  data.push_back(42);  // 3
  data_ready=true;  // 4
}

std::atomic<bool> x,y;
std::atomic<int> z;

void write_x_then_y()
{
  x.store(true,std::memory_order_relaxed);  // 1
  std::atomic_thread_fence(std::memory_order_release);  // 2
  y.store(true,std::memory_order_relaxed);  // 3
}

void read_y_then_x()
{
  while(!y.load(std::memory_order_relaxed));  // 4
  std::atomic_thread_fence(std::memory_order_acquire);  // 5
  if(x.load(std::memory_order_relaxed))  // 6
    ++z;
}

void testFence()
{
  x=false;
  y=false;
  z=0;
  std::thread a(write_x_then_y);
  std::thread b(read_y_then_x);
  a.join();
  b.join();
  assert(z.load()!=0);  // 7
}

void testOrder()
{
    auto t1 = std::thread(thread_1);
    auto t2 = std::thread(thread_2);

    t1.join();
    t2.join();
}

void testAtomic()
{
    auto t1 = std::thread(reader_thread);
    auto t2 = std::thread(writer_thread);

    t1.join();
    t2.join();
}

int main(int argc, const char * argv[]) {
    // insert code here...
    testFence();
    
    return 0;
}

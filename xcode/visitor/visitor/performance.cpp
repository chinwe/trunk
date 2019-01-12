#include <iostream>
#include <chrono>
#include "detail/animal.h"

struct animal{
    virtual int move()=0;
    virtual ~animal() = default;
};

struct dog : public animal{
    int move() override {
        return 4;
    }
};

struct bird : public animal{
    int move() override {
        return 2;
    }
};

int test_dynmic_cast(animal* anim){
    auto p = dynamic_cast<dog*>(anim);
    if (p) {
        p->move();
    }

    return 0;
}

int test_virtual_mtd(animal* anim){
    if(anim) {
        return anim->move();
    }

    return 0;
}

int test(zoo::animal* anim){
    if(anim->get_type()==zoo::AnimalType::Dog) {
        return anim->move();
    }

    return 0;
}

const int SIZE = 10000000;
animal* anim = new dog;

void test1(){
    using namespace std::chrono;
    auto begin = high_resolution_clock::now();
    for (int i = 0; i < SIZE; ++i) {
        test_dynmic_cast(anim);
    }
    auto end = high_resolution_clock::now();
    std::cout<<duration_cast<milliseconds>(end - begin).count()<<'\n';
}

void test2(){
    using namespace std::chrono;
    auto begin = high_resolution_clock::now();
    for (int i = 0; i < SIZE; ++i) {
        test_virtual_mtd(anim);
    }
    auto end = high_resolution_clock::now();
    std::cout<<duration_cast<milliseconds>(end - begin).count()<<'\n';
}

zoo::animal* anim1 = new zoo::dog;
void test3(){
    using namespace std::chrono;
    auto begin = high_resolution_clock::now();
    for (int i = 0; i < SIZE; ++i) {
        test(anim1);
    }
    auto end = high_resolution_clock::now();
    std::cout<<duration_cast<milliseconds>(end - begin).count()<<'\n';
}

int main(){
    test1();
    test2();
    test3();

    return 0;
}

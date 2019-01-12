#include <iostream>
#include <chrono>

struct animal{
    virtual int move()=0;
    virtual ~animal() = default;
};

struct dog : public animal{
    int move() override {
        return 4;
    }

    void swim(){
        std::cout<<"swim"<<std::endl;
    }
};

struct bird : public animal{
    int move() override {
        return 2;
    }

    void fly(){
        std::cout<<"fly"<<std::endl;
    }
};

void test(animal* anim){
    if(dynamic_cast<dog*>(anim)){
        dynamic_cast<dog*>(anim)->swim();
    }else if(dynamic_cast<bird*>(anim)){
        dynamic_cast<bird*>(anim)->fly();
    }
}


void test1(animal* anim){
    if(auto p = dynamic_cast<dog*>(anim);p){
        p->swim();
    }else if(auto p = dynamic_cast<bird*>(anim);p){
        p->fly();
    }
}

int main() {
    animal* anim = new dog;
    test(anim);
    delete anim;

    animal* anim1 = new bird;
    test1(anim1);
    delete anim1;

    return 0;
}
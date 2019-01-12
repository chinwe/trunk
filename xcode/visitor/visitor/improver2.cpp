//
// Created by qicosmos on 1/8/19.
//
#include <iostream>
struct dog;
struct bird;

struct visitor{
    virtual void visit(dog*)=0;
    virtual void visit(bird*)=0;
};

struct animal{
    virtual int move()=0;
    virtual void accept(visitor*) = 0;
    virtual ~animal() = default;
};

//crtp
template<typename T>
struct visitable : public animal{
    void accept(visitor* v) override {
        v->visit(static_cast<T*>(this));
    }
};

struct dog : public visitable<dog>{
    int move() override {
        return 4;
    }

    void swim(){
        std::cout<<"swim"<<std::endl;
    }
};

struct bird : public visitable<bird>{
    int move() override {
        return 2;
    }

    void fly(){
        std::cout<<"fly"<<std::endl;
    }
};

struct visitor_impl : public visitor{
    void visit(dog* d) override{
        d->swim();
    }

    void visit(bird* b) override{
        b->fly();
    }
};

int main()
{
    animal* a = new dog;
    visitor* v = new visitor_impl;

    a->accept(v);

    animal* b = new bird;
    b->accept(v);
}

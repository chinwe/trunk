#include <iostream>
#include <chrono>

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

struct dog : public animal{
    int move() override {
        return 4;
    }

    void accept(visitor* v){
        v->visit(this);
    }

    void swim(){
        std::cout<<"swim"<<std::endl;
    }
};

struct bird : public animal{
    int move() override {
        return 2;
    }

    void accept(visitor* v){
        v->visit(this);
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

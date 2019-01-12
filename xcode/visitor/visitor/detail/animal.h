//
// Created by qicosmos on 1/8/19.
//

#ifndef TEST2_ANIMAL_H
#define TEST2_ANIMAL_H

namespace zoo{

    enum class AnimalType{
        Dog,
        Bird,
        Fish,
        Unknown
    };

    struct animal{
        virtual int move()=0;
        virtual AnimalType get_type() = 0;
        virtual ~animal() = default;
    };

    struct dog : public animal{
        int move() override {
            return 4;
        }

        AnimalType get_type(){
            return AnimalType::Dog;
        }

        void swim(){
            std::cout<<"swim"<<std::endl;
        }
    };

    struct bird : public animal{
        int move() override {
            return 2;
        }

        AnimalType get_type(){
            return AnimalType::Bird;
        }

        void fly(){
            std::cout<<"fly"<<std::endl;
        }
    };
}

#endif //TEST2_ANIMAL_H

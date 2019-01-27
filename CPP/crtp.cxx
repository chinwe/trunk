#include<iostream>
#include<stddef.h>

using namespace std;

template<class Derived>
struct Base  
{  
    void Interface()  
    {  
        cout <<"come from Interface"<<endl;      
        // 转换为子类指针，编译期将绑定至子类方法  
        static_cast<Derived*>(this)->Implementation(); 
    }  
             
    static void StaticInterface()  
    {  
        // 编译期将绑定至子类方法  
        cout <<"come from StaticInterface"<<endl;
        Derived::StaticImplementation();  
    }  

    void Implementation()
    {
        cout <<"Base Implementation"<<endl;
        return;
    }
    static void StaticImplementation()
    {
        cout << "Base StaticImplementation"<<endl;
        return;
    }
};  

// The Curiously Recurring Template Pattern (CRTP)  
struct Derived1 : Base<Derived1>  
{  
    static void StaticImplementation();  
};  
       
struct Derived2 : Base<Derived2>  
{  
    void Implementation();  
};  

void Derived1::StaticImplementation()
{
    cout << "StaticImplementation from Derived1"<<endl;
    return;
}
void Derived2::Implementation()
{
    cout <<"Implementation from Derived2"<<endl;
    return;
}

int main()
{
    cout << "***********************************" << endl;
    Derived1 derive1;
    Derived2 derive2;
    derive1.Implementation();
    derive1.StaticImplementation();
    derive2.Implementation();
    derive2.StaticImplementation();
    cout << "***********************************" << endl << endl;

    Base<Derived1> base_derive1;
    Base<Derived2> base_derive2;
    base_derive1.Implementation();
    base_derive1.StaticImplementation();
    base_derive2.Implementation();
    base_derive2.StaticImplementation();
    cout << "***********************************" << endl << endl;

    base_derive1.StaticInterface();
    base_derive1.Interface();
    base_derive2.StaticInterface();
    base_derive2.Interface();
    cout << "***********************************" << endl << endl;

    return 0;
}
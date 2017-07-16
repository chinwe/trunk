// IoC(Inversion of Control)
#include <iostream>
#include <string>
#include <memory>
#include <functional>
#include <map>

using namespace std;

template <class T>
class IocContainer
{
public:
    IocContainer(void) {}
    ~IocContainer(void) {}

    template <class Derived>
    void RegisterType(const string& strKey)
    {
        std::function<T* ()> funcConstruct = [] { return new(std::nothrow) Derived(); };
        RegisterType(strKey, funcConstruct);
    }

    T* Resolve(const string& strKey)
    {
        if (m_mapConstructer.find(strKey) == m_mapConstructer.end())
        {
            return nullptr;
        }

        auto funcConstruct = m_mapConstructer[strKey];

        return funcConstruct();
    }

    std::shared_ptr<T> ResolveShared(const string& strKey)
    {
        T* ptr = Resolve(strKey);
        return std::shared_ptr<T>(ptr);
    }

private:
    void RegisterType(const string& strKey, std::function<T* ()> funcConstruct)
    {
        if (m_mapConstructer.find(strKey) != m_mapConstructer.end())
        {
            throw std::invalid_argument("this key has already exist.");
        }

        m_mapConstructer.emplace(strKey, funcConstruct);
    }

private:
    map<string, std::function<T* ()>> m_mapConstructer; 
};

struct ICar
{
    virtual ~ICar() {}
    virtual void Test() const = 0;
};

struct Bus : ICar
{
    Bus() {}
    virtual void Test() const
    {
        std::cout << "Bus::Test()" << std::endl;
    }
};

struct Car : ICar
{
    Car() {}
    virtual void Test() const
    {
        std::cout << "Car::Test()" << std::endl;
    }
};

int main(int argc, char** argv)
{
    IocContainer<ICar> carioc;
    carioc.RegisterType<Bus>("Bus");
    carioc.RegisterType<Car>("Car");

    auto bus = carioc.ResolveShared("Bus");
    bus->Test();

    auto car = carioc.ResolveShared("Car");
    car->Test();

	return 0;
}

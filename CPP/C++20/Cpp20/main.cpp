#include <iostream>
#include <thread>
#include "FiboGenerator.h"
#include "Generator.h"
#include "Resumeable.h"
#include "Awaitee.h"
#include "Lazy.h"

using namespace std::chrono_literals;
using namespace std;

Resumeable HelloCoroutine()
{
    std::cout << "Hello" << std::endl;
    co_await suspend_always{};
    std::cout << "Coroutine" << std::endl;
}

void TestHelloCoroutine()
{
    auto r = HelloCoroutine();
    r.Resume();
}

Resumeable UseAwaitee()
{
    Awaitee t([]()
    {
        std::this_thread::sleep_for(4s);
        std::cout << "task finish." << std::endl;
    });
    co_await t;
}

void TestAwaitee()
{
    auto r = UseAwaitee();
    r.Resume();
}

void TestFiboGenerator()
{
    for (auto x = FiboGenerator(); x < 1000; x.Resume())
    {
        std::cout << "fibo: " << x << std::endl;
    }
}

void TestIntGenerator()
{
    auto g = GetGenerator(1, 1);
    for (int i = 0; i < 10; ++i, g.Next())
    {
        std::cout << g.Value() << std::endl;
    }
}

void TestLazyValue()
{
    auto lazy = LazyValue();
    std::cout << "lazy" << std::endl;
    std::cout << lazy.Value() << std::endl;
}

int main(int argc, char** argv)
{
    TestIntGenerator();

    TestLazyValue();

#ifdef __WIN
    system("pause");
#endif

    return 0;
}

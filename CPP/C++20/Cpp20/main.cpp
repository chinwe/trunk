#include <iostream>
#include <thread>
#include "FiboGenerator.h"
#include "Generator.h"
#include "Resumable.h"
#include "Awaiter.h"
#include "Lazy.h"
#include <experimental/generator>

using namespace std::chrono_literals;
using namespace std;

Resumable HelloCoroutine()
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

Resumable UseAwaiter()
{
    Awaiter t([]()
    {
        std::this_thread::sleep_for(4s);
        std::cout << "task finish." << std::endl;
    });
    co_await t;
}

void TestAwaiter()
{
    auto r = UseAwaiter();
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

experimental::generator<int> GetSequenceGenerator(int start = 0, int stop = 10)
{
    int i = start;
    for (int i = start; i < stop; ++i)
    {
        co_yield i;
    }
}

void TestGetSequenceGenerator()
{
    auto gen = GetSequenceGenerator();

    for (const auto& value : gen)
    {
        std::cout << value << std::endl;
    }
}

int main(int argc, char** argv)
{
    TestHelloCoroutine();

    TestAwaiter();

    TestIntGenerator();

    TestLazyValue();

    TestGetSequenceGenerator();

#ifdef __WIN
    system("pause");
#endif

    return 0;
}

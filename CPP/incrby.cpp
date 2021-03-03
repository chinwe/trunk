#include <thread>
#include <vector>
#include <atomic>
#include <cassert>

using namespace std;

atomic<int> g_value = 8;

atomic<int> cnt = 0;

int getValue()
{
    return g_value;
}

int incrBy(int increment)
{
    return g_value += increment;
}

void eit1()
{
    std::this_thread::sleep_for(std::chrono::microseconds(100));

    int value = getValue();
    if (value > 1)
    {
        incrBy(-1);
    }
    else
    {
        ++cnt;
        printf("[0x%x] %d last device\n", std::this_thread::get_id(), value);
    }
}

void eit2()
{
    std::this_thread::sleep_for(std::chrono::microseconds(100));

    int ret = incrBy(-1);
    int value = getValue();
    if (0 == value)
    {
        ++cnt;
        printf("[0x%x] ret=%d value=%d last device\n", std::this_thread::get_id(), ret, value);
    }
}

void eit3()
{
    std::this_thread::sleep_for(std::chrono::microseconds(50));

    int value = incrBy(-1);
    if (0 == value)
    {
        printf("[0x%x] %d last device\n", std::this_thread::get_id(), ++cnt);
    }
}

int main()
{
    for (size_t j = 0; j < 1000; j++)
    {
        printf("Round %d\n", j);

        vector<thread> ts;
        for (int i = 0; i < 10; i++)
        {
            ts.emplace_back(std::thread(eit3));
        }

        for (auto& t : ts)
        {
            t.join();
        }

        g_value = 10;
    }

    printf("cnt=%d\n", cnt);
    system("pause");


    return 0;
}


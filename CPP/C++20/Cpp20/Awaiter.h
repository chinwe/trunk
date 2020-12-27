#pragma once
#ifdef __cpp_impl_coroutine
#include <coroutine>
#else
#include <experimental/coroutine>
using namespace std::experimental;
#endif
#include <functional>

struct Awaiter
{
    Awaiter(std::function<void()> f_in)
        : f(f_in)
    {
    }

    constexpr bool await_ready() const noexcept
    {
        return false;
    }

    constexpr void await_suspend(coroutine_handle<>) const noexcept
    {
    }

    void await_resume() const noexcept
    {
        if (f)
        {
            f();
        }
    }

    std::function<void()> f;
};

#pragma once

#include <experimental/coroutine>
#include <functional>

struct Awaitee
{
    Awaitee(std::function<void()> f_in)
        : f(f_in)
    {
    }

    constexpr bool await_ready() const noexcept
    {
        return false;
    }

    constexpr void await_suspend(std::experimental::coroutine_handle<>) const noexcept
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

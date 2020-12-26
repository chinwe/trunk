#pragma once

#include "BaseCoroutine.h"
#include <tuple>

struct CoFiboFuture : CBaseCoroutine<CoFiboFuture>
{
    struct promise_type : base_promise_type<promise_type>
    {
        int value_;

        auto yield_value(int value)
        {
            value_ = value;
            return suspend_always{};
        }

        void return_void()
        {
        }
    };

    CoFiboFuture(coroutine_handle<promise_type> handle)
        : co_handle_(handle)
    {
    }

    using CBaseCoroutine::Resume;

    operator int()
    {
        return co_handle_.promise().value_;
    }

    coroutine_handle<promise_type> co_handle_{};
};

CoFiboFuture FiboGenerator()
{
    int i = 0, j = 1;
    while (true)
    {
        co_yield j;
        std::tie(i, j) = std::make_pair(j, i + j);
    }
}

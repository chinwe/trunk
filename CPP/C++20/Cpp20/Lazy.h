#pragma once

#include "BaseCoroutine.h"

struct Lazy : CBaseCoroutine<Lazy>
{
    struct promise_type : base_promise_type<promise_type>
    {
        int value_;

        auto initial_suspend()
        {
            return suspend_always{};
        }

        void return_value(int value)
        {
            value_ = value;
        }
    };

    Lazy(coroutine_handle<promise_type> handle)
        : co_handle_(handle)
    {
    }

    int Value()
    {
        Resume();
        return co_handle_.promise().value_;
    }

    coroutine_handle<promise_type> co_handle_{};
};

Lazy LazyValue()
{
    int value = 42;
    co_return value;
}

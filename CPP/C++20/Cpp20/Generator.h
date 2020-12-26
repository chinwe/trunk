#pragma once

#include "BaseCoroutine.h"

struct Generator : CBaseCoroutine<Generator>
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

    Generator(coroutine_handle<promise_type> handle)
        : co_handle_(handle)
    {
    }

    using CBaseCoroutine::Resume;

    bool Next()
    {
        Resume();
        return !co_handle_.done();
    }

    int Value()
    {
        return co_handle_.promise().value_;
    }
    coroutine_handle<promise_type> co_handle_{};
};

Generator GetGenerator(int start = 0, int step = 1)
{
    while (true)
    {
        co_yield start;
        start += step;
    }
}

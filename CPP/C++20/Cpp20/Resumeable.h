#pragma once
#include "BaseCoroutine.h"

struct Resumeable : CBaseCoroutine<Resumeable>
{
    struct promise_type : base_promise_type<promise_type>
    {
        void return_void()
        {
        }
    };

    Resumeable(coroutine_handle<promise_type> handle)
        : co_handle_(handle)
    {
    }

    using CBaseCoroutine::Resume;

    coroutine_handle<promise_type> co_handle_{};
};

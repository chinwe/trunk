#pragma once

#include <coroutine>

template<typename Future>
class CBaseCoroutine
{
public:
    virtual ~CBaseCoroutine()
    {
        if (GetFuture().co_handle_)
        {
            GetFuture().co_handle_.destroy();
        }
    }

protected:
    using suspend_never = std::suspend_never;
    using suspend_always = std::suspend_always;

    template<typename... U>
    using coroutine_handle = std::coroutine_handle<U...>;

    template<typename Promise>
    struct base_promise_type
    {
        auto initial_suspend()
        {
            return suspend_never{};
        }
        auto final_suspend() noexcept
        {
            return suspend_always{};
        }
        Future get_return_object()
        {
            return coroutine_handle<Promise>::from_promise(static_cast<Promise&>(*this));
        }
        void unhandled_exception()
        {
            std::terminate();
        }
    };

    void Resume()
    {
        if (GetFuture().co_handle_ && !GetFuture().co_handle_.done())
        {
            GetFuture().co_handle_.resume();
        }
    }

private:
    Future& GetFuture()
    {
        return static_cast<Future&>(*this);
    }
};

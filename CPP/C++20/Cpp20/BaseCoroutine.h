#pragma once

#ifdef __cpp_impl_coroutine
#include <coroutine>
using namespace std;
#else
#include <experimental/coroutine>
using namespace std::experimental;
#endif

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
    template<typename... U>
    using handle_type = coroutine_handle<U...>;

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
            return handle_type<Promise>::from_promise(static_cast<Promise&>(*this));
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

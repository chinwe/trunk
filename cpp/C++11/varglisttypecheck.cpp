#include <iostream>
#include <string>

using namespace std;

template <typename T>
struct is_types : std::true_type
{};


template <>
struct is_types<string> : std::false_type
{};

template <typename T>
inline void Allow(const T& t)
{
    static_assert(is_types<T>::value, "string no allowed.");
}

inline void CheckVargs()
{
}

template<typename T>
inline void CheckVargs(const T& t)
{
    Allow(t);
}

template<typename T, typename... Args>
inline void CheckVargs(const T& t, Args... args)
{
    Allow(t);
    CheckVargs(args...);
}

int main(int argc, char** argv)
{
    string sTest;
    //CheckVargs(1.0, 2.0, sTest);
    CheckVargs(1.0, 2.0, 1);
 
    return 0;
}

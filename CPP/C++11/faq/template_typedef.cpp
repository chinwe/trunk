#include <iostream>

// idea	: int_exact_traits<N>::type用于表达含有N个bit的数值类型
template <int>
struct int_exact_traits
{
	typedef int type;
};

template<>
struct int_exact_traits<8>
{
	typedef char type;
};

template<>
struct int_exact_traits<16>
{
	typedef short type;
};

template<int N>
using int_exact=typename int_exact_traits<N>::type;

int main(int argc, char** argv)
{
	int_exact<8> a = 7;

	typedef void (*PFD)(double);    // C 样式
	using PF = void (*)(double);    // using加上C样式的类型
	
	//error
	//using P = [](double)->void;  	// using和函数返回类型后置语法

	return 0;
}

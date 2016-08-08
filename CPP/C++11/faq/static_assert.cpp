#include <iostream>

int main(int argc, char** argv)
{
	//静态断言，编译器断言
	static_assert(4 == sizeof(long), "32-bit code generation required for this library.");

	return 0;
}

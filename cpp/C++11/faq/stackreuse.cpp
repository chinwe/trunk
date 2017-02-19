
#include <iostream>

void foo()
{
	int a = 12345678;
	std::cout << a << std::endl;
}

void bar()
{
	int b;
	std::cout << b << std::endl;
}

int main(int argc, char** argv)
{
	foo();
	bar();

	return 0;
}

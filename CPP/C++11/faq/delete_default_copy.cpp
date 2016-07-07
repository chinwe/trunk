#include <iostream>
class X
{
	X& operator=(const X&) = delete;
	X(const X&) = delete;
};

class Y
{
	Y& operator=(const Y&) = default;
	Y(const Y&) = default;
};

class Z
{
	void foo(long long)
	{
	}

	void foo(long) = delete;
};

int main(int argc, char** argv)
{
	return 0;
}

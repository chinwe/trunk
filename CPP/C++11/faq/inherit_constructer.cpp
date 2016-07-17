#include <iostream>

using namespace std;

struct Base
{
	Base(int xIn) : x(xIn)
	{
	}

	int x = 0;
};

struct Derived : public Base
{
	//隐式声明构造函数Derived(int)
	using Base::Base;

	int y{1};
};

int main(int argc, char** argv)
{
	Derived d(2);

	cout << d.x << " " << d.y << endl;

	return 0;
}

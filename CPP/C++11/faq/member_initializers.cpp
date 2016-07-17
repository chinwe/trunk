#include <iostream>
#include <string>

using namespace std;

class A
{
public:
	A() : b(3)
	{
	}

public:
	int a = 1;
	string hello{"1222"};
	int b = 2;
};

int main(int argc, char** argv)
{
	A a;
	cout << a.a << " " << a.b << " " << a.hello << endl;
	return 0;
}



#include <iostream>
#include <string>

using namespace std;

class X
{
	int a;
i
public:
	X(int x)
	{
		if (0 < x && x <= 100)
		{
			a = x;
		}
		else
		{
			throw "bad x";
		}
	}

	X() : X(200) { }

	//X(string s) : X{lexical_cast<int>(s)} { } 

};

int main(int argc, char** argv)
{
	X x = X();

	return 0;
}

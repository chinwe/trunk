#include <iostream>

enum Flags {good = 0, fail = 1, bad = 2, eof = 4};

constexpr int operator|(Flags f1, Flags f2)
{
	return Flags(int(f1) | int(f2));
}

void f(Flags x)
{
	switch (x)
	{
		case bad:		/**/ break;
		case eof:		/**/ break;
		case bad|eof:	/**/ break;
		default:		/**/ break;
	}
}

struct Point
{
	int x, y;
	
	constexpr Point(int xIn, int yIn)
		:x(xIn),
		 y(yIn)
	{}
};

int main(int argc, char** argv)
{
	constexpr int x1 = bad | eof;

	constexpr Point origo(0, 0);
	constexpr int z = origo.x;

	return 0;
}

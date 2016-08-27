#include <iostream>

int main(int argc, char** argv)
{
	int maxleak = 100;
	for (int i = 0; i != maxleak; ++i)
	{
		int* p = new int[1024]();
	}

	return 0;
}

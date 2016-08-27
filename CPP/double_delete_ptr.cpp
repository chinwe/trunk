#include <iostream>

int main(int argc, char** argv)
{
	int* ptr = new int(11);

	if (NULL != ptr)
	{
		delete ptr;
	}

	delete ptr;

	return 0;
}

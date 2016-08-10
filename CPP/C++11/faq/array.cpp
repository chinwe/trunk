#include <iostream>
#include <array>

int main(int argc, char** argv)
{
	std::array<int, 8> a = {1, 2, 3, 4};
	int* p = a.data();

	for (size_t i = 0; i < a.size(); ++i)
	{
		std::cout << a[i] << std::endl;
	}

	return 0;
}

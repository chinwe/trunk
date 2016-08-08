#include <iostream>

int one()
{
	return 1;
}

int main(int argc, char** argv)
{
	int&& rv = one();
	int i = 1111;
	
	//“错误：不能将右值引用rv2绑定到左值”
	//int&& rv2 = i;

	int&& j = std::move(i);

	std::cout << rv << " " << i << " " << j << std::endl;

	rv = j = 2222;

	std::cout << rv << " " << i << " " << j << std::endl;

	return 0;
}

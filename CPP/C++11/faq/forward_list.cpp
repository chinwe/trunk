#include <iostream>
#include <forward_list>

int main(int argc, char** argv)
{
	std::forward_list<int> fl = {1, 2, 3};
	fl.push_front(11);

	for(auto&& i : fl)
	{
		std::cout << i << std::endl;
	}

	std::cout << fl.empty();

	return 0;
}

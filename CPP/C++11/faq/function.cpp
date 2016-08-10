#include <iostream>
#include <functional>

void yearold(int age)
{
	std::cout << "I'm " << age << " years old." << std::endl;
}

class hello
{
public:
	hello() = default;
	~hello() = default;
	
	void say()
	{
		std::cout << "hello::say" << std::endl;
	}
};


int main(int argc, char** argv)
{
	auto ff = std::bind(yearold, std::placeholders::_1);
	ff(20);


	std::function<void (int)> func = ff;
	ff(18);

	hello h;
	std::function<void (hello*)> fmemb = &hello::say;

	fmemb(&h);

	return 0;
}

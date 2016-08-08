#include <iostream>
#include <string>

int main(int argc, char** argv)
{
	std::string strA = R"(\W\\W)";

	std::cout << strA << std::endl;

	std::string strB = R"("quoted string"))";

	std::cout << strB << std::endl;

	std::string strC = R"***("quoted string containing the usual terminator (")")***";

	std::cout << strC << std::endl;

	return 0;
}

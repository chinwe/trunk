#include <iostream>

//强类型枚举
enum class Color : char { red, green, blue, yellow };

int main(int argc, char** argv)
{
	Color color = Color::red;
	
	//compile error
	//strong_type_class_enum.cpp:10:6: error: cannot initialize a variable of type
    //'int' with an rvalue of type 'Color'
	//int iColor = Color::red;

	char cColor = static_cast<char>(color);
	std::cout << cColor << std::endl;	
	return 0;
}

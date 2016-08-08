#include <iostream>

void foo(int i)
{
	++i;
}

void bar(char* p)
{
	p = nullptr;
}

int main(int argc, char** argv)
{
	int* p = nullptr;

	//warning
	//foo(NULL);

	//error
	//foo(nullptr);

	/*
		“实际上，我们这里可以看到nullptr和NULL两者本质的差别，NULL是一个整型数0，而nullptr可以看成是一个char *”
		摘录来自: wizardforcel. “C++11 FAQ 中文版”。 iBooks. 
	*/
	bar(nullptr);

	return 0;
}

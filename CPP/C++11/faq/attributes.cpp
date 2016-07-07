#include <iostream>

//f() 永不返回
void f [[ noreturn ]] ()
{
	throw "error";	//虽然不得返回，但可以抛出异常

	//如果返回值会报错
	//return 1;
}

int* g(int* x, int* y [[carries_dependency]])
{
	//编译哟偶话指示
	return x;
}

int main(int argc, char** argv)
{
	f();

	int x = 1;
	int y = 2;
	
	g(&x, &y);

	return 0;
}

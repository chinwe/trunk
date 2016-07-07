#include <iostream>
#include <vector>

using namespace std;

void f(const vector<int>& a, vector<float>& b)
{
	//推断表达式数据类型，并定义为Tmp
	typedef decltype(a[0] * b[0]) Tmp;

	//使用Tmp作为类型
	Tmp sum;
	for (size_t i = 0; i < b.size(); ++i)
	{
		sum +=  Tmp(a[i] * b[i]);
	}

	cout << sum <<endl;
	bool bType = (typeid(sum) == typeid(float));
	cout << bType << endl;
}

int main(int argc, char** argv)
{
	vector<int> vecNumbers = {0, 1, 2};
	vector<float> vecNumbersF = {0.0f, 1.1f, 2.2f};

	f(vecNumbers, vecNumbersF);

	/*
	“如果你仅仅是想根据初始化值为一个变量推断合适的数据类型，那么使用auto是
	一个更加简单的选择。当你只有需要推断某个表达式的数据类型，例如某个函数
	调用表达式的计算结果的数据类型，而不是某个变量的数据类型时，你才真正需
	要delctype。”

	摘录来自: wizardforcel. “C++11 FAQ 中文版”。 iBooks. 
	*/
	
	return 0;
}

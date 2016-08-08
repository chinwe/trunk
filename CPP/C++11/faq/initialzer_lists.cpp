#include <iostream>
//#include <pair>
#include <list>

using namespace std;

void plusplusone(initializer_list<int> list)
{
	for (auto& i : list)
	{

		cout << i << endl;
	}
}

int main(int argc, char** argv)
{
	/*
	list<pair<string, string>> languages = {
		{"Nygaard", "Simula"},
		{"Richards", "BCPL"},
		{"Richie", "C"}
	};*/

	plusplusone({1, 2, 3, 4});
	
	// compile error
	//int i{1.2};
	//char cc{256};
	char c{1};

	return 0;
}

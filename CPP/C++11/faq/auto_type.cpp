#include <iostream>
#include <vector>

using namespace std;

int main(int argc, char** argv)
{
	auto x = 12;
	vector<int> vecNumbers;
	vecNumbers.push_back(x);
	vecNumbers.push_back(x);
	vecNumbers.push_back(x);
	vecNumbers.push_back(x);
	vecNumbers.push_back(x);

	for (auto it = vecNumbers.begin(); it != vecNumbers.end(); ++it)
	{
		//cout << *it << endl;
	}

	for (auto& it : vecNumbers)
	{
		cout << ++it << endl;
	}

	return 0;
}

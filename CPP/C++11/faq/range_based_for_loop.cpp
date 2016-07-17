#include <iostream>
#include <vector>

using namespace std;

int main(int argc, char** argv)
{
	vector<int> vi = {1, 2, 3, 4};

	for (auto x : vi)
	{
		cout << x << "\n";
	}

	for (auto& x : vi)
	{
		++x;

		cout << x << "\n";
	}

	for (const auto x : {1, 2, 3, 5, 8})
	{
		cout << x << "\n";
	}

	return 0;
}

#include <iostream>
#include <vector>

using namespace std;

int main(int argc, char** argv)
{
	vector<int> v = {50, -10, 20, -30};
	
	sort(v.begin(), v.end(), [](int a, int b)
			{
				return abs(a) < abs(b);
			});
	
	for (const auto& i : v)
	{
		cout << i << " ";
	}
	cout << endl;

	vector<int> indices(v.size());
	int count = 0;
	generate(indices.begin(), indices.end(), [&]() { return ++count; });

	for (const auto& i : indices)
	{
		cout << i << " ";
	}
	cout << endl;

	generate(indices.begin(), indices.end(), [&count]() { return ++count; });

	generate(indices.begin(), indices.end(), [&]() {return --count;});

	for (const auto& i : indices)
	{
		cout << i << " ";
	}
	cout << endl;

	return 0;
}

#include <iostream>
#include <vector>

using namespace std;

int my_comoputationv(const vector<int>& v) noexcept
{
	vector<int> res(v.size());
	int iSum = 0;

	for (const auto& i : v)
	{
		iSum += i;
	}
}

int main(int argc, char** argv)
{
	return 0;
}

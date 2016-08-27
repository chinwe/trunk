#include <iostream>
#include <map>
#include <unordered_map>

using namespace std;

int main(int argc, char** argv)
{
	map<string,int> m {
    {"Dijkstra",1972}, {"Scott",1976},
    {"Wilkes",1967}, {"Hamming",1968}};

	m["Ritchie"] = 1983;
	for(auto x : m)
	{
		cout << "{" << x.first << "," << x.second << "}" << endl;
	}

	cout << "---------------------------------" << endl;
	
	// 使用优化之后的unordered_map
	unordered_map<string,int> um {
    {"Dijkstra",1972}, {"Scott",1976},
    {"Wilkes",1967}, {"Hamming",1968}};

	um["Ritchie"] = 1983;
	for(auto x : um)
	{
		cout << "{" << x.first << "," << x.second << "}" << endl;
	}

	return 0;
}

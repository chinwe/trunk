#include <boost/bind.hpp>
#include <boost/ref.hpp>
#include <boost/function.hpp>
#include <boost/lambda/lambda.hpp> 
#include <iostream> 
#include <vector> 
#include <algorithm> 

void add(int i, int j) 
{ 
	std::cout << i + j << std::endl; 
}

bool les(int i, int j)
{
	return i < j;
}

void add(int i, int j, std::ostream &os) 
{ 
	os << i + j << std::endl; 
} 

int main() 
{ 
	std::vector<int> v; 
	v.push_back(2); 
	v.push_back(1); 
	v.push_back(3); 

	// sort
	std::sort(v.begin(), v.end(), boost::bind(les, _1, _2)); 

	std::for_each(v.begin(), v.end(), boost::bind(add, 10, _1)); 

	std::for_each(v.begin(), v.end(), boost::bind(add, 10, _1, boost::ref(std::cout))); 

	boost::function<int (const char*)> f = std::atoi; 
	std::cout << f("1609") << std::endl; 
	f = std::strlen; 
	std::cout << f("1609") << std::endl;

	std::for_each(v.begin(), v.end(), std::cout << boost::lambda::_1 << "\n"); 

	//c++11 lambda
	std::for_each(v.begin(), v.end(), [](int i) { std::cout << i << "\n"; }); 
}
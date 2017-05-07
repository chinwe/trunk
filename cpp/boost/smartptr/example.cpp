#include <boost/smart_ptr.hpp>
#include <iostream>
#include <string>
#include <vector>

class Base
{
public:
	Base()
	{
		std::cout << "Base" << std::endl;
	}
	~Base()
	{
		std::cout << "~Base" << std::endl;
	}
};

int main()
{
	// scoped_ptr
	boost::scoped_ptr<Base> scp(new Base);

	// shared_ptr use ref-count 
	std::vector<boost::shared_ptr<Base>> v; 
	auto shp = boost::shared_ptr<Base>(new Base());
	v.push_back(shp);

	// weak_ptr
	boost::shared_ptr<int> sh(new int(99)); 
	boost::weak_ptr<int> w(sh);

	return 0;
}

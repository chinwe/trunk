#include <iostream>
#include <memory>

using namespace std;

void test_shared_ptr()
{
	shared_ptr<int> spi(new int(10));

	shared_ptr<int> spi2(spi);

}

void test_unique_ptr()
{
	unique_ptr<int> spi(new int(10));

}

int main(int argc, char** argv)
{
	return 0;
}

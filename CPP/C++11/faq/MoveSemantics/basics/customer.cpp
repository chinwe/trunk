#include <string>
#include <vector>
#include <iostream>
#include <cassert>
#include <utility>

class Customer
{
private:
	std::string name;		 // name of the customer
	std::vector<int> values; // some values of the customer
public:
	Customer(const std::string &n)
		: name{n}
	{
		assert(!name.empty());
	}
	std::string getName() const
	{
		return name;
	}
	void addValue(int val)
	{
		values.push_back(val);
	}
	friend std::ostream &operator<<(std::ostream &strm, const Customer &cust)
	{
		strm << '[' << cust.name << ": ";
		for (int val : cust.values)
		{
			strm << val << ' ';
		}
		strm << ']';
		return strm;
	}
};

int main(int argc, char **argv)
{
	// create a customer with some initial values:
	Customer c{"Wolfgang Amadeus Mozart"};
	for (int val : {0, 8, 15})
	{
		c.addValue(val);
	}
	std::cout << "c: " << c << '\n'; // print value of initialized c
	// insert the customer twice into a collection of customers:
	std::vector<Customer> customers;
	customers.push_back(c);			   // copy into the vector
	customers.push_back(std::move(c)); // move into the vector
	std::cout << "c: " << c << '\n';   // print value of moved-from c
	// print all customers in the collection:
	std::cout << "customers:\n";
	for (const Customer &cust : customers)
	{
		std::cout << " " << cust << '\n';
	}

	// Therefore, the automatic generation of move operations is disabled when at least one of the following special member functions is user-declared:
	//   Copy constructor
	//   Copy assignment operator
	//   Another move operation
	//   Destructor

	return 0;
}

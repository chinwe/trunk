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

	// copy constructor (copy all members):
	Customer(const Customer &cust)
		: name{cust.name}, values{cust.values}
	{
		std::cout << "COPY " << cust.name << '\n';
	}
	// move constructor (move all members):
	Customer(Customer &&cust) // noexcept declaration missing
		: name{std::move(cust.name)}, values{std::move(cust.values)}
	{
		std::cout << "MOVE " << name << '\n';
	}
	// copy assignment (assign all members):
	Customer &operator=(const Customer &cust)
	{
		std::cout << "COPYASSIGN " << cust.name << '\n';
		name = cust.name;
		values = cust.values;
		return *this;
	}
	// move assignment (move all members):
	Customer &operator=(Customer &&cust)
	{ // noexcept declaration missing
		std::cout << "MOVEASSIGN " << cust.name << '\n';
		if (this != &cust)
		{
			name = std::move(cust.name);
			values = std::move(cust.values);
		}
		
		return *this;
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
	customers.emplace_back(c);	// copy into the vector
	customers.emplace_back(std::move(c)); // move into the vector
	std::cout << "c: " << c << '\n';   // print value of moved-from c

	// print all customers in the collection:
	std::cout << "customers:\n";
	for (const Customer &cust : customers)
	{
		std::cout << " " << cust << '\n';
	}

	return 0;
}

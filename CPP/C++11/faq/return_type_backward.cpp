#include <iostream>

template<class T, class U>
auto mul(T x, U y) -> decltype(x*y)
{
	return x * y;
}

struct MyList
{
	struct Node
	{
		int data;
		Node* next;
	};

	Node* erase(Node* p);
};

auto MyList::erase(Node* p) -> Node*
{
	return p->next;
}

int main(int argc, char** argv)
{
	std::cout << mul(1, 23.4);
	return 0;
}

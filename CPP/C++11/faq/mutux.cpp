#include <iostream>
#include <mutex>

using namespace std;

mutex m;

int sh; //share data

void f()
{
	if (m.try_lock())
	{
		sh += 1;
		m.unlock();
	}
	else
	{
		//...
	}
}

recursive_mutex rm;

void g(int i)
{
	rm.lock();

	sh += 1;

	if (-i > 0)
	{
		g(i);
	}

	rm.unlock();
}

timed_mutex tm;

void h()
{
	if (tm.try_lock_for(std::chrono::seconds(10)))
	{
		sh += 1;
		tm.unlock();
	}

	auto now = std::chrono::steady_clock::now();
	if (tm.try_lock_until(now + std::chrono::seconds(60)))
	{
		sh += 1;
		tm.unlock();
	}
}

int main(int argc, char** argv)
{
	cout << sh << endl;
	f();
	h();
	cout << sh << endl;

	return 0;
}

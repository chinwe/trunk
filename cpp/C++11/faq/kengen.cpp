#include <iostream>
#include <string>

using namespace std;

int main(int argc, char** argv)
{
	string strUserName;

	cout << "Please input user name:";
	cin >> strUserName;

	cout << "Serial:" << endl;

	for (int i = 0; i < strUserName.size(); ++i)
	{
		int edx = (int)strUserName[i] % 10;
		edx = edx ^ i;
  		edx += 2;
  		edx = edx & 0xFF;
  		if (edx >= 10)
  		{
  			edx -= 10;
  		}

  		cout << char('d' + edx);
	}

	cout << endl;

	return 0;
}

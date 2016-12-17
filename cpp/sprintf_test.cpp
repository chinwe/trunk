#include <iostream>
#include <string>

using namespace std;

int main(int argc, char** argv)
{
    string ss("hello world");
    char szBuffer[100] = { 0 };

    sprintf(szBuffer, "%s %s", ss.c_str(), ss.c_str());

    cout << szBuffer << endl;

    return 0;
}

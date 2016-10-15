#include <iostream>

double mypower(int x, int k)
{
    if (0 == k)
    {
        return 1;
    }

    double dTemp = mypower(x, k / 2);
    if (x % 2)
    {
        return dTemp * dTemp * x;
    }
    else
    {
        return dTemp * dTemp;
    }
}

int main(int argc, char** argv)
{
    double dResult = mypower(2, 32);

    std::cout << dResult << std::endl;

    return 0;
}

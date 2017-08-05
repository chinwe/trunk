#include <iostream>
#include <string>
#include <sstream>

using namespace std;

void _sprintf(std::stringstream & ss, const char *s)
{
    while (*s) {
        if (*s == '%') {
            if (*(s + 1) == '%') {
                ++s;
            }
            else {
                throw std::runtime_error("invalid format string: missing arguments");
            }
        }
        ss << *s++;
    }
}

template<typename T, typename... Args>
void _sprintf(std::stringstream & ss, const char *s, T value, Args... args)
{
    
    while (*s) {
        if (*s == '%') {
            if (*(s + 1) == '%') {
                ++s;
            }
            else {
                ss << value;
                _sprintf(ss, s + 1, args...); // call even when *s == 0 to detect extra arguments
                return;
            }
        }
        ss << *s++;
    }
    throw std::logic_error("extra arguments provided to lyw::sprintf");
}

template<typename T,typename... Args>
std::string _sprintf(const char *s, T value, Args... args) {
    std::stringstream ss;
    _sprintf(ss, s, value, args...);
    return ss.str();
}

int main(int argc, char** argv) {
    
    string str = _sprintf("I tried % times in % ", 10, "Monday");

    cout << str << endl;
}
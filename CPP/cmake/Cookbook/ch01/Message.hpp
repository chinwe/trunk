#pragma once

#include <iosfwd>
#include <string>

#ifdef _WIN32
  #ifdef MESSAGE_API_DEFINE
    #define MESSAGE_EXPORT _declspec(dllexport)
  #else
    #define MESSAGE_EXPORT _declspec(dllimport)
  #endif
#else 
    #define MESSAGE_EXPORT 
#endif

class MESSAGE_EXPORT Message {
public:
  Message(const std::string &m) : message_(m) {}

  friend std::ostream &operator<<(std::ostream &os, Message &obj) {
    return obj.printObject(os);
  }

private:
  std::string message_;
  std::ostream &printObject(std::ostream &os);
};

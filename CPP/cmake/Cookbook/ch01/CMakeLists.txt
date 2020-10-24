# cmake最低版本
cmake_minimum_required(VERSION 3.10 FATAL_ERROR)

# 项目名称和语言
project(hello LANGUAGES CXX)

# 添加静态库
add_library(message STATIC
    Message.hpp
    Message.cpp
)

# 添加可执行文件
add_executable(hello main.cpp)

# 链接
target_link_libraries(hello message)
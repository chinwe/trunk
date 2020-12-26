
cmake_minimum_required(VERSION 3.10.0 FATAL_ERROR)

project(cpp20)

set(CMAKE_CXX_STANDARD 20)

set(SRC_LIST
    Cpp20/main.cpp
)

add_executable(main
    ${SRC_LIST}
)

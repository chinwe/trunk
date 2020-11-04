cmake_minimum_required(VERSION 3.10.0 FATAL_ERROR)

project(demo4)

add_definitions(-std=c++11)

set(CMAKE_BUILD_TYPE "Debug")
set(CMAKE_CXX_FLAGS_DEBUG "$ENV{CXXFLAGS} -o0 -Wall -g")
set(CMAKE_CXX_FLAGS_RELEASE "$ENV{CXXFLAGS} -o3 -Wall")

add_subdirectory(mymathlib)
add_subdirectory(src)

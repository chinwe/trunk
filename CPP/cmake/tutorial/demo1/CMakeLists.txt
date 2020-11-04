cmake_minimum_required(VERSION 3.10.0 FATAL_ERROR)

project(demo1)

set(SRC_LIST
    main.cpp
)

add_executable(demo1
    ${SRC_LIST}
)

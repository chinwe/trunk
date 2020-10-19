cmake_minimum_required(VERSION 3.10)

project ( helloworld )

set( src
    ${CMAKE_CURRENT_SOURCE_DIR}/main.cpp
)

add_executable( helloworld
    ${src}
)
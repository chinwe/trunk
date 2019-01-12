cmake_minimum_required(VERSION 3.10)
project(test2)

set(CMAKE_CXX_STANDARD 17)

set(BASIC
        main.cpp
        )

set(PERFORMANCE
        performance.cpp
    )

set(IMPROVER1
        improver1.cpp
    )

set(IMPROVER2
        improver2.cpp
        )

set(IMPROVER3
        improver3.cpp
        )

add_executable(basic 	${BASIC})
add_executable(performance ${PERFORMANCE})
add_executable(improver1 ${IMPROVER1})
add_executable(improver2 ${IMPROVER2})
add_executable(improver3 ${IMPROVER3})
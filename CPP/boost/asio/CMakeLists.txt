cmake_minimum_required(VERSION 3.12.0)

add_definitions(-std=c++11)

add_executable(echo_server echo_server.cpp)
target_link_libraries(echo_server PRIVATE
    boost_thread
    pthread)

add_executable(echo_client echo_client.cpp)
target_link_libraries(echo_client PRIVATE
    boost_thread
    pthread)

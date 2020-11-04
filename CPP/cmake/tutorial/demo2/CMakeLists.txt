cmake_minimum_required(VERSION 3.10.0 FATAL_ERROR)

project(demo2)

aux_source_directory(${CMAKE_CURRENT_SOURCE_DIR} DIR_SRCS)

add_executable(demo2 ${DIR_SRCS})

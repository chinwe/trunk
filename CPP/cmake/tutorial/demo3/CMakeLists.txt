cmake_minimum_required(VERSION 3.10.0 FATAL_ERROR)

project(demo3)

add_subdirectory(mymathlib)

aux_source_directory(. DIR_SRCS)

add_executable(demo3 ${DIR_SRCS})

include_directories(${PROJECT_SOURCE_DIR}/mymathlib)

target_link_libraries(demo3 mymathlib)

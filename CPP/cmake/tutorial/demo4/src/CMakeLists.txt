include_directories(${PROJECT_SOURCE_DIR}/mymathlib)

aux_source_directory(. DIR_SRCS)

add_executable(demo4 ${DIR_SRCS})

target_link_libraries(demo4 mymath)

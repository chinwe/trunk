add_rules("mode.debug", "mode.release")

target("tutorial")
    set_kind("binary")
    add_deps("mymath")
    add_files("src/main.cpp")
    add_includedirs("mymathlib")

add_subdirs("mymathlib")
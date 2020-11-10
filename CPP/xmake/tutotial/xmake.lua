
target("mymathlib")
    -- shared or static
    set_kind("shared")
    add_files("mymathlib/*.cpp")

target("tutorial")
    set_kind("binary")
    add_files("src/*.cpp")
    add_includedirs("mymathlib")
    add_deps("mymathlib")

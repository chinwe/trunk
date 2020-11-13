add_rules("mode.debug", "mode.release")

target("server")
    set_kind("binary")
    set_languages("cxx")
    add_files("src/server/main.cpp")

target("client")
    set_kind("binary")
    set_languages("cxx")
    add_files("src/client/main.cpp")

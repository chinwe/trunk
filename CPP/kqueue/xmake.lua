add_rules("mode.debug", "mode.release")

target("kqueue")
    set_kind("binary")
    set_languages("c")
    add_files("src/main.c")

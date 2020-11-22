add_rules("mode.debug", "mode.release")
set_languages("cxx11")

target("ringbuffer")
    set_kind("static")
    add_files("src/RingBuffer.cpp")

target("main")
    set_kind("binary")
    add_files("src/main.cpp")
    add_deps("ringbuffer")
cmake_minimum_required(VERSION 3.18.1)

project(whisper)

add_library( whisper SHARED
        whisper.cpp  # your native whisper.cpp wrapper source file (create this)
        whisper_jni.cpp # JNI glue code (create this)
        # Add other whisper.cpp sources or headers as needed
)

find_library(log-lib log)

target_include_directories(whisper PRIVATE
        ${CMAKE_CURRENT_SOURCE_DIR}/include  # your whisper includes
)

target_link_libraries( whisper
        ${log-lib}
)

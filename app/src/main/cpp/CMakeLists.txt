cmake_minimum_required(VERSION 3.10.2)
project("whisper")

add_library( # Name of the library
             whisper SHARED
             whisper.cpp
             whisper_jni.cpp
             ggml.c
			 ggml-backend.h)

find_library( # This tells it to link against Android's native libraries
              log-lib
              log)

target_include_directories(whisper PRIVATE
                           ${CMAKE_SOURCE_DIR}
                           ${CMAKE_CURRENT_SOURCE_DIR})

target_link_libraries(whisper ${log-lib})

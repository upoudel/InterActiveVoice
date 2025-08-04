package com.example.interactivevoice

object Whisper {
    // Load native lib
    init {
        System.loadLibrary("whisper")
    }

    external fun transcribeFromJNI(audioPath: String): String
}

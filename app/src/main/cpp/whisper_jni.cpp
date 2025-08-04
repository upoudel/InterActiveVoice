#include <jni.h>
#include <string>
#include <android/log.h>

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_interactivevoice_Whisper_transcribeFromJNI(
        JNIEnv *env,
        jobject /* this */,
        jstring audioPath
) {
    const char *nativePath = env->GetStringUTFChars(audioPath, nullptr);

    // For now, just return a fake result to test it works.
    std::string result = "Transcribed text from: ";
    result += nativePath;

    env->ReleaseStringUTFChars(audioPath, nativePath);
    return env->NewStringUTF(result.c_str());
}

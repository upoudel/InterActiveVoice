package com.example.interactivevoice

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Brush
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.io.IOException

class MainActivity : ComponentActivity() {
    private var recorder: MediaRecorder? = null
    private var outputFile: String = ""

    private val outputPath: String
        get() = File(getExternalFilesDir(null), "voice_input.3gp").absolutePath

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        copyModelIfNeeded(this)

        outputFile = outputPath

        setContent {
            VoiceRecorderUI(
                onStart = { startRecording() },
                onStop = {
                    stopRecording()
                    transcribeRecording()
                },
                outputPath = outputFile
            )
        }

        requestMicPermission()
    }

    fun transcribeRecording() {
        if (!File(outputPath).exists()) {
            Toast.makeText(this, "Audio file not found for transcription", Toast.LENGTH_SHORT).show()
            return
        }

        val result = Whisper.transcribeFromJNI(outputPath)

        Toast.makeText(this, "Transcription:\n$result", Toast.LENGTH_LONG).show()
    }

    private fun requestMicPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                RECORD_AUDIO_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun startRecording() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            recorder = MediaRecorder(this).apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(outputFile)
                try {
                    prepare()
                    start()
                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(this@MainActivity, "Prepare failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    release()
                    recorder = null
                } catch (e: IllegalStateException) {
                    e.printStackTrace()
                    Toast.makeText(this@MainActivity, "Start failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    release()
                    recorder = null
                }
            }
        } else {
            @Suppress("DEPRECATION")
            recorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(outputFile)
                try {
                    prepare()
                    start()
                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(this@MainActivity, "Prepare failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    release()
                    recorder = null
                } catch (e: IllegalStateException) {
                    e.printStackTrace()
                    Toast.makeText(this@MainActivity, "Start failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    release()
                    recorder = null
                }
            }
        }
    }

    private fun stopRecording() {
        try {
            recorder?.apply {
                stop()
                reset()
                release()
            }
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        } finally {
            recorder = null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        recorder?.release()
        recorder = null
    }

    fun copyModelIfNeeded(context: Context) {
        val modelName = "ggml-base.bin"
        val modelFile = File(context.filesDir, modelName)

        if (!modelFile.exists()) {
            context.assets.open(modelName).use { input ->
                modelFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        }
    }

    companion object {
        private const val RECORD_AUDIO_PERMISSION_REQUEST_CODE = 101
    }
}

@Composable
fun VoiceRecorderUI(
    onStart: () -> Unit,
    onStop: () -> Unit,
    outputPath: String
) {
    var isRecording by remember { mutableStateOf(false) }
    var useMyVoice by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF2D2D7D), Color(0xFF4A90E2))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = "Interactive Voice",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Use My Voice", color = Color.White)
                Switch(
                    checked = useMyVoice,
                    onCheckedChange = { useMyVoice = it }
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = {
                    if (isRecording) {
                        onStop()
                        Toast.makeText(
                            context,
                            "Recording stopped. Saved to $outputPath",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        onStart()
                    }
                    isRecording = !isRecording
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRecording) Color.Red else Color.Green
                ),
                shape = CircleShape,
                modifier = Modifier.size(120.dp)
            ) {
                Text(if (isRecording) "⏹" else "🎤", style = MaterialTheme.typography.headlineLarge)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = if (isRecording) "Recording..." else "Tap to Talk",
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

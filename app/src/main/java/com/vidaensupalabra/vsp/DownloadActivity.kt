package com.vidaensupalabra.vsp

import android.content.Intent
import android.os.Bundle
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.vidaensupalabra.vsp.otros.downloadUpdate
import kotlinx.coroutines.launch

class DownloadActivity : AppCompatActivity() {
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_download)

        progressBar = findViewById(R.id.progressBar)

        val downloadUrl = intent.getStringExtra("downloadUrl") ?: return
        val outputPath = intent.getStringExtra("outputPath") ?: return

        lifecycleScope.launch {
            val success = downloadUpdate(downloadUrl, outputPath) { progress ->
                runOnUiThread {
                    progressBar.progress = progress
                }
            }
            if (success) {
                // Notify MainActivity to install the APK
                val installIntent = Intent().apply {
                    putExtra("outputPath", outputPath)
                }
                setResult(RESULT_OK, installIntent)
                finish()
            } else {
                // Handle failure
                setResult(RESULT_CANCELED)
                finish()
            }
        }
    }
}

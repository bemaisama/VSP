// DownloadActivity.kt

package com.vidaensupalabra.vsp

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.vidaensupalabra.vsp.otros.downloadUpdate
import kotlinx.coroutines.launch

class DownloadActivity : AppCompatActivity() {
    private lateinit var progressBar: ProgressBar
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_download)

        progressBar = findViewById(R.id.progressBar)

        progressDialog = ProgressDialog(this).apply {
            setTitle("Descargando actualización")
            setMessage("Por favor espera...")
            setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
            isIndeterminate = false
            setCancelable(false)
            max = 100
        }
        progressDialog.show()

        val downloadUrl = intent.getStringExtra("downloadUrl") ?: return
        val outputPath = intent.getStringExtra("outputPath") ?: return

        lifecycleScope.launch {
            val success = downloadUpdate(downloadUrl, outputPath) { progress ->
                runOnUiThread {
                    progressDialog.progress = progress
                    progressBar.progress = progress
                }
            }
            progressDialog.dismiss()
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


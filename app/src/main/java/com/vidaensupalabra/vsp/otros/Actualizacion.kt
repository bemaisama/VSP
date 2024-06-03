package com.vidaensupalabra.vsp.otros

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

suspend fun downloadUpdate(downloadUrl: String, outputPath: String): Boolean = withContext(Dispatchers.IO) {
    Log.d("DownloadUpdate", "Starting download from URL: $downloadUrl")
    val url = URL(downloadUrl)
    val connection = url.openConnection() as HttpURLConnection

    try {
        connection.requestMethod = "GET"
        connection.connect()
        Log.d("DownloadUpdate", "Response Code: ${connection.responseCode}")

        if (connection.responseCode == HttpURLConnection.HTTP_OK) {
            val inputStream: InputStream = connection.inputStream
            val outputStream = FileOutputStream(outputPath)

            val buffer = ByteArray(4096)
            var bytesRead: Int

            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
            }

            outputStream.close()
            inputStream.close()
            Log.d("DownloadUpdate", "Download successful, saved to $outputPath")
            return@withContext true
        } else {
            Log.e("DownloadUpdate", "Failed to download file: ${connection.responseCode}")
        }
    } catch (e: Exception) {
        e.printStackTrace()
        Log.e("DownloadUpdate", "Exception: ${e.message}")
    } finally {
        connection.disconnect()
    }
    Log.d("DownloadUpdate", "Download failed")
    return@withContext false
}

suspend fun checkForUpdate(currentVersion: String): String? = withContext(Dispatchers.IO) {
    val url = URL("https://raw.githubusercontent.com/bemaisama/VSP/master/app/src/main/java/com/vidaensupalabra/vsp/version.json")
    val connection = url.openConnection() as HttpURLConnection

    try {
        connection.requestMethod = "GET"
        connection.connect()
        Log.d("CheckForUpdate", "Response Code: ${connection.responseCode}")

        if (connection.responseCode == HttpURLConnection.HTTP_OK) {
            val inputStream = connection.inputStream
            val response = inputStream.bufferedReader().use { it.readText() }

            Log.d("CheckForUpdate", "Response: $response")

            val jsonObject = JSONObject(response)
            val latestVersion = jsonObject.getString("latestVersion")
            val downloadUrl = jsonObject.getString("url")

            Log.d("CheckForUpdate", "Current Version: $currentVersion, Latest Version: $latestVersion")

            if (latestVersion != currentVersion) {
                Log.d("CheckForUpdate", "Update Available: $downloadUrl")
                return@withContext downloadUrl
            }
        } else {
            Log.e("CheckForUpdate", "Failed to fetch update: ${connection.responseCode}")
        }
    } catch (e: Exception) {
        e.printStackTrace()
        Log.e("CheckForUpdate", "Exception: ${e.message}")
    } finally {
        connection.disconnect()
    }
    return@withContext null
}

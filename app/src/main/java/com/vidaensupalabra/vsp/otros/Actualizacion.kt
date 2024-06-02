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
    val url = URL(downloadUrl)
    val connection = url.openConnection() as HttpURLConnection

    try {
        connection.requestMethod = "GET"
        connection.connect()

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

            return@withContext true
        }
    } finally {
        connection.disconnect()
    }
    return@withContext false
}

suspend fun checkForUpdate(currentVersion: String): String? = withContext(Dispatchers.IO) {
    val url = URL("https://raw.githubusercontent.com/bemaisama/VSP/master/version.json")
    val connection = url.openConnection() as HttpURLConnection

    try {
        connection.requestMethod = "GET"
        connection.connect()

        if (connection.responseCode == HttpURLConnection.HTTP_OK) {
            val inputStream = connection.inputStream
            val response = inputStream.bufferedReader().use { it.readText() }

            val jsonObject = JSONObject(response)
            val latestVersion = jsonObject.getString("latestVersion")
            val downloadUrl = jsonObject.getString("url")

            Log.d("UpdateChecker", "Current version: $currentVersion, Latest version: $latestVersion")

            if (latestVersion != currentVersion) {
                Log.d("UpdateChecker", "Update available: $downloadUrl")
                return@withContext downloadUrl
            }
        } else {
            Log.e("UpdateChecker", "Failed to fetch update: ${connection.responseCode}")
        }
    } catch (e: Exception) {
        e.printStackTrace()
        Log.e("UpdateChecker", "Exception: ${e.message}")
    } finally {
        connection.disconnect()
    }
    return@withContext null
}

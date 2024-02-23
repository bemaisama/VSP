package com.example.vsp.otros

import android.content.Context
import android.os.AsyncTask
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class CheckUpdateTask(private val context: Context) : AsyncTask<Void, Void, String>() {
    private val versionUrl = "https://github.com/bemaisama/VSP/blob/d6c288156e305ed5fbdaabc48634c4dbc9685f7b/version.json"

    override fun doInBackground(vararg params: Void?): String? {
        val url = URL(versionUrl)
        val connection = url.openConnection() as HttpURLConnection
        try {
            connection.connect()
            val inputStream = connection.inputStream
            val jsonResponse = inputStream.bufferedReader().use { it.readText() }
            val jsonObject = JSONObject(jsonResponse)
            return jsonObject.getString("latestVersion")
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        } finally {
            connection.disconnect()
        }
    }

    override fun onPostExecute(result: String?) {
        super.onPostExecute(result)
        if (result != null) {
            val currentVersion = context.packageManager.getPackageInfo(context.packageName, 0).versionName
            if (result != currentVersion) {
                // Notifica al usuario sobre la actualización disponible
                notifyUpdateAvailable()
            }
        }
    }

    private fun notifyUpdateAvailable() {
        // Implementa tu lógica de notificación aquí
    }
}

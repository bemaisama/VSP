// fetchTextFromUrl.kt

package com.vidaensupalabra.vsp.otros

import android.content.Context
import android.content.SharedPreferences
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import java.io.IOException

fun fetchTextFromUrl(context: Context, url: String, callback: (String?, String?) -> Unit) {
    val cachedContent = getHtmlContent(context, url)
    if (cachedContent.first != null && cachedContent.second != null) {
        callback(cachedContent.first, cachedContent.second)
        return
    }

    val documentId = try {
        url.split("/d/")[1].split("/")[0]
    } catch (e: Exception) {
        null
    }

    if (documentId == null) {
        callback(null, null)
        return
    }

    val exportUrl = "https://docs.google.com/document/d/$documentId/export?format=html"

    if (exportUrl.isEmpty() || !(exportUrl.startsWith("http://") || exportUrl.startsWith("https://"))) {
        callback(null, null)
        return
    }

    val client = OkHttpClient()

    val request = Request.Builder()
        .url(exportUrl)
        .build()

    client.newCall(request).enqueue(object : okhttp3.Callback {
        override fun onFailure(call: okhttp3.Call, e: IOException) {
            e.printStackTrace()
            callback(null, null)
        }

        override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
            if (!response.isSuccessful) {
                callback(null, null)
                return
            }

            val responseBody = response.body?.string()
            if (responseBody != null) {
//                Log.d("FetchedHTML", responseBody) // Log del HTML completo
                val document = Jsoup.parse(responseBody)
                val title = document.select("h1").first()?.text() ?: "Sin título"
                val content = document.body().html()
                saveHtmlContent(context, url, title, content)
                callback(title, content)
            } else {
                callback(null, null)
            }
        }
    })
}
fun saveHtmlContent(context: Context, url: String, title: String, content: String) {
    val sharedPreferences: SharedPreferences = context.getSharedPreferences("HTML_CACHE", Context.MODE_PRIVATE)
    with(sharedPreferences.edit()) {
        putString("${url}_title", title)
        putString("${url}_content", content)
        apply()
    }
}

fun getHtmlContent(context: Context, url: String): Pair<String?, String?> {
    val sharedPreferences: SharedPreferences = context.getSharedPreferences("HTML_CACHE", Context.MODE_PRIVATE)
    val title = sharedPreferences.getString("${url}_title", null)
    val content = sharedPreferences.getString("${url}_content", null)
    return Pair(title, content)
}

package com.vidaensupalabra.vsp.otros

import android.content.Context
import com.vidaensupalabra.vsp.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar

suspend fun getCurrentArdeReference(context: Context): String {
    return withContext(Dispatchers.IO) {
        val db = AppDatabase.getInstance(context)
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1 // Meses en Calendar son 0-indexed
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val ardeEntity = db.ardeDao().findByDate(year, month, day).firstOrNull()
        ardeEntity?.reference ?: "No ARDE reference found"
    }
}


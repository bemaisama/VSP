// Arde_find.kt

package com.vidaensupalabra.vsp.otros

import android.content.Context
import android.util.Log
import com.vidaensupalabra.vsp.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar

suspend fun getCurrentArdeReference(context: Context): String {
    return withContext(Dispatchers.IO) {
        Log.d("DATABASE_INIT", "Initializing database...")
        val db = AppDatabase.getInstance(context)
        Log.d("DATABASE_INIT", "Database initialized successfully.")

        val calendar = Calendar.getInstance()
        val year = 1 // Año fijo en la base de datos
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        Log.d("ARDE_REF", "Getting ARDE reference for date: Year: $year, Month: $month, Day: $day")
        val ardeEntity = db.ardeDao().findByDate(year, month, day).firstOrNull()
        val reference = ardeEntity?.reference ?: "No ARDE reference found"
        Log.d("ARDE_REF", "ARDE reference for date: Year: $year, Month: $month, Day: $day is: $reference")
        reference
    }
}

suspend fun printAllArdeData(context: Context) {
    return withContext(Dispatchers.IO) {
        Log.d("DATABASE_INIT", "Initializing database...")
        val db = AppDatabase.getInstance(context)
        Log.d("DATABASE_INIT", "Database initialized successfully.")

        val allArdeData = db.ardeDao().getAll()
        if (allArdeData.isEmpty()) {
            Log.d("ARDE_DATA", "No ARDE data found.")
        } else {
            allArdeData.forEach {
                Log.d("ARDE_DATA", "ID: ${it.id}, Year: ${it.year}, Month: ${it.month}, Day: ${it.day}, Reference: ${it.reference}")
            }
        }
    }
}


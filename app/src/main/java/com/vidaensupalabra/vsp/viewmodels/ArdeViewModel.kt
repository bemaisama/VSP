// ArdeViewModel.kt

package com.vidaensupalabra.vsp.viewmodels

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.vidaensupalabra.vsp.room.AppDatabase
import com.vidaensupalabra.vsp.room.ArdeEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

class ArdeViewModel(application: Application) : AndroidViewModel(application) {
    private val db: AppDatabase = AppDatabase.getInstance(application.applicationContext)

    val currentArde: MutableLiveData<ArdeEntity?> = MutableLiveData(null)
    val dataLoaded: MutableLiveData<Boolean> = MutableLiveData(false)
    val navigationEvent = MutableSharedFlow<String>()

    init {
        checkAndLoadData()
    }

    private fun checkAndLoadData() {
        viewModelScope.launch(Dispatchers.IO) {
            val count = db.ardeDao().count()
            if (count == 0) {
                val ardeList = loadArdeDataFromCsv(getApplication<Application>().applicationContext)
                if (ardeList.isNotEmpty()) {
                    db.ardeDao().insertAll(*ardeList.toTypedArray())
                    Log.d("ArdeViewModel", "Data loaded from CSV: ${ardeList.size} records")
                } else {
                    Log.d("ArdeViewModel", "No data found in CSV or error reading CSV")
                }
            } else {
                Log.d("ArdeViewModel", "Database already contains data. Not loading from CSV.")
            }
            dataLoaded.postValue(true)
        }
    }

    fun loadArdeDataForSelectedDate(year: Int, month: Int, day: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val ardeData = db.ardeDao().findByDate(year, month, day).firstOrNull()
            currentArde.postValue(ardeData)
            if (ardeData != null) {
                navigationEvent.emit("arde_detail/${ardeData.year}/${ardeData.month}/${ardeData.day}")
            }
        }
    }

    fun updateArde(arde: ArdeEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            db.ardeDao().updateArde(arde)
        }
    }

    private suspend fun loadArdeDataFromCsv(context: Context): List<ArdeEntity> =
        withContext(Dispatchers.IO) {
            val ardeList = mutableListOf<ArdeEntity>()
            context.assets.open("Datos_Arde.csv").use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        val tokens = line!!.split(",")
                        if (tokens.size >= 4) {
                            val year = tokens[0].toInt()
                            val month = tokens[1].toInt()
                            val day = tokens[2].toInt()
                            val reference = tokens[3]
                            val devocional = ""
                            ardeList.add(
                                ArdeEntity(
                                    year = year,
                                    month = month,
                                    day = day,
                                    reference = reference,
                                    devocional = devocional
                                )
                            )
                        }
                    }
                }
            }
            return@withContext ardeList
        }
}

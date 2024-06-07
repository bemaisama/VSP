package com.vidaensupalabra.vsp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vidaensupalabra.vsp.otros.getCurrentArdeReference
import com.vidaensupalabra.vsp.otros.printAllArdeData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class DeveloperOptionsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_developer_options)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = DeveloperOptionsAdapter(getDeveloperOptions())

        val testNotificationButton = findViewById<Button>(R.id.testNotificationButton)
        testNotificationButton.setOnClickListener {
            sendTestNotification()
        }
    }

    private fun getDeveloperOptions(): List<DeveloperOption> {
        return listOf(
            DeveloperOption("Enviar Notificación ARDE") {
                sendTestNotification()
            },
            DeveloperOption("Comprobar Permisos") {
                checkPermissions()
            },
            DeveloperOption("Mostrar Datos ARDE") {
                showArdeData()
            }
        )
    }

    private fun sendTestNotification() {
        lifecycleScope.launch(Dispatchers.IO) {
            val ardeReference = getCurrentArdeReference(this@DeveloperOptionsActivity)
            launch(Dispatchers.Main) {
                Toast.makeText(this@DeveloperOptionsActivity, "Notificación enviada: $ardeReference", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkPermissions() {
        val requiredPermissions = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.REQUEST_INSTALL_PACKAGES,
            Manifest.permission.POST_NOTIFICATIONS
        )

        val missingPermissions = requiredPermissions.filter {
            ActivityCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isEmpty()) {
            Toast.makeText(this, "Todos los permisos están concedidos", Toast.LENGTH_SHORT).show()
        } else {
            ActivityCompat.requestPermissions(this, missingPermissions.toTypedArray(), 1)
        }
    }

    private fun showArdeData() {
        lifecycleScope.launch {
            try {
                printAllArdeData(this@DeveloperOptionsActivity)
                Toast.makeText(this@DeveloperOptionsActivity, "Datos ARDE mostrados en el Log", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e("DeveloperOptionsActivity", "Error mostrando datos ARDE", e)
                Toast.makeText(this@DeveloperOptionsActivity, "Error mostrando datos ARDE", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

class DeveloperOptionsAdapter(private val options: List<DeveloperOption>) :
    RecyclerView.Adapter<DeveloperOptionsAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val button: Button = itemView.findViewById(R.id.optionButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_developer_option, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val option = options[position]
        holder.button.text = option.name
        holder.button.setOnClickListener {
            option.action.invoke()
        }
    }

    override fun getItemCount() = options.size
}

data class DeveloperOption(val name: String, val action: () -> Unit)

package com.cristina.tfg_android_indoor_app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.cristina.tfg_android_indoor_app.R
import com.cristina.tfg_android_indoor_app.data.repository.AuthRepository
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class ThresholdSettingsActivity : AppCompatActivity() {

    private val authRepository = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_threshold_settings)

        // Referencias a los campos
        val etEntrada = findViewById<TextInputEditText>(R.id.etEntrada)
        val etSalon = findViewById<TextInputEditText>(R.id.etSalon)
        val etCocina = findViewById<TextInputEditText>(R.id.etCocina)
        val etHab1 = findViewById<TextInputEditText>(R.id.etHab1)
        val etBano1 = findViewById<TextInputEditText>(R.id.etBano1)
        val etBano2 = findViewById<TextInputEditText>(R.id.etBano2)
        val etHab2 = findViewById<TextInputEditText>(R.id.etHab2)
        val etHab3 = findViewById<TextInputEditText>(R.id.etHab3)

        val btnSave = findViewById<MaterialButton>(R.id.btnSaveThresholds)

        // 1️⃣ Cargar valores actuales desde la API
        lifecycleScope.launch {
            val token = getSharedPreferences("auth", MODE_PRIVATE)
                .getString("token", "") ?: ""

            val result = authRepository.getMyThresholds(token)

            result.onSuccess { thresholds ->
                etEntrada.setText(thresholds["ENTRADA"]?.toString() ?: "")
                etSalon.setText(thresholds["SALON"]?.toString() ?: "")
                etCocina.setText(thresholds["COCINA"]?.toString() ?: "")
                etHab1.setText(thresholds["HAB1"]?.toString() ?: "")
                etBano1.setText(thresholds["BAN1"]?.toString() ?: "")
                etBano2.setText(thresholds["BAN2"]?.toString() ?: "")
                etHab2.setText(thresholds["HAB2"]?.toString() ?: "")
                etHab3.setText(thresholds["HAB3"]?.toString() ?: "")
            }
        }

        // 2️⃣ Guardar cambios
        btnSave.setOnClickListener {

            val thresholds = mapOf(
                "ENTRADA" to etEntrada.text.toString().toIntOrNull(),
                "SALON" to etSalon.text.toString().toIntOrNull(),
                "COCINA" to etCocina.text.toString().toIntOrNull(),
                "HAB1" to etHab1.text.toString().toIntOrNull(),
                "BAN1" to etBano1.text.toString().toIntOrNull(),
                "BAN2" to etBano2.text.toString().toIntOrNull(),
                "HAB2" to etHab2.text.toString().toIntOrNull(),
                "HAB3" to etHab3.text.toString().toIntOrNull()
            ).filterValues { it != null } as Map<String, Int>

            lifecycleScope.launch {
                val token = getSharedPreferences("auth", MODE_PRIVATE)
                    .getString("token", "") ?: ""

                val result = authRepository.saveThresholds(token, thresholds)

                result.onSuccess {
                    Snackbar.make(findViewById(android.R.id.content), "Umbrales guardados", Snackbar.LENGTH_LONG).show()
                }.onFailure {
                    Snackbar.make(findViewById(android.R.id.content), "Error al guardar", Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }
}

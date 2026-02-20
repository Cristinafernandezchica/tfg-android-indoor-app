package com.cristina.tfg_android_indoor_app.training

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.cristina.tfg_android_indoor_app.R
import com.cristina.tfg_android_indoor_app.data.repository.MLRepository
import kotlinx.coroutines.launch

class TrainingAdminActivity : AppCompatActivity() {

    private val repo = MLRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_training_admin)

        val btnReset = findViewById<Button>(R.id.btnReset)
        val btnTrain = findViewById<Button>(R.id.btnTrain)
        val btnReload = findViewById<Button>(R.id.btnReload)
        val btnStatus = findViewById<Button>(R.id.btnStatus)
        val tvOutput = findViewById<TextView>(R.id.tvOutput)
        val btnCapture = findViewById<Button>(R.id.btnCapture)

        btnReset.setOnClickListener {
            lifecycleScope.launch {
                repo.resetTraining()
                tvOutput.text = "Entrenamiento borrado"
            }
        }

        btnTrain.setOnClickListener {
            lifecycleScope.launch {
                repo.trainModel()
                tvOutput.text = "Modelo entrenado"
            }
        }

        btnReload.setOnClickListener {
            lifecycleScope.launch {
                repo.reloadModels()
                tvOutput.text = "Modelos recargados"
            }
        }

        btnStatus.setOnClickListener {
            lifecycleScope.launch {
                val status = repo.getStatus().body()
                tvOutput.text = status.toString()
            }
        }

        btnCapture.setOnClickListener {
            startActivity(Intent(this, TrainingActivity::class.java))
        }

    }
}

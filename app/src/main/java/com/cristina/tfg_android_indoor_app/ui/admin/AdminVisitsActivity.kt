package com.cristina.tfg_android_indoor_app.ui.admin

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cristina.tfg_android_indoor_app.BaseActivity
import com.cristina.tfg_android_indoor_app.R
import com.cristina.tfg_android_indoor_app.data.model.VisitDataResponse
import com.cristina.tfg_android_indoor_app.data.repository.RoomRepository
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AdminVisitsActivity : BaseActivity() {

    private val repo = RoomRepository()
    private lateinit var etDate: TextInputEditText
    private lateinit var btnCurrent: MaterialButton
    private lateinit var btnLoad: MaterialButton
    private lateinit var progressBar: ProgressBar
    private lateinit var rvVisits: RecyclerView
    private lateinit var tvEmpty: TextView

    private lateinit var adapter: VisitAdapter
    private val visitsList = mutableListOf<VisitItem>()
    private val calendar = Calendar.getInstance()
    private var selectedDate: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_visits)

        hideBottomNavigation()
        hideTopAppBar()

        val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        initViews()
        setupRecyclerView()
        setupDatePicker()
        setupListeners()

        // Cargar datos actuales al iniciar
        loadCurrentVisits()
    }

    private fun initViews() {
        etDate = findViewById(R.id.etDate)
        btnCurrent = findViewById(R.id.btnCurrent)
        btnLoad = findViewById(R.id.btnLoad)
        progressBar = findViewById(R.id.progressBar)
        rvVisits = findViewById(R.id.rvVisits)
        tvEmpty = findViewById(R.id.tvEmpty)
    }

    private fun setupRecyclerView() {
        adapter = VisitAdapter(visitsList)
        rvVisits.layoutManager = LinearLayoutManager(this)
        rvVisits.adapter = adapter
    }

    private fun setupDatePicker() {
        etDate.setOnClickListener {
            showDatePicker()
        }
        updateDateDisplay()
    }

    private fun showDatePicker() {
        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                updateDateDisplay()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun updateDateDisplay() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        selectedDate = dateFormat.format(calendar.time)
        etDate.setText(selectedDate)
    }

    private fun setupListeners() {
        btnCurrent.setOnClickListener {
            loadCurrentVisits()
        }

        btnLoad.setOnClickListener {
            loadVisitsByDate()
        }
    }

    private fun loadCurrentVisits() {
        progressBar.visibility = View.VISIBLE
        btnCurrent.isEnabled = false
        btnLoad.isEnabled = false

        lifecycleScope.launch {
            try {
                val response = repo.getVisitsCurrent()
                if (response.isSuccessful) {
                    val visits = response.body() ?: emptyMap()
                    updateList(visits, "Actual")
                } else {
                    Toast.makeText(this@AdminVisitsActivity, "Error al cargar datos", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@AdminVisitsActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                progressBar.visibility = View.GONE
                btnCurrent.isEnabled = true
                btnLoad.isEnabled = true
            }
        }
    }

    private fun loadVisitsByDate() {
        if (selectedDate.isEmpty()) {
            Toast.makeText(this, "Selecciona una fecha", Toast.LENGTH_SHORT).show()
            return
        }

        progressBar.visibility = View.VISIBLE
        btnCurrent.isEnabled = false
        btnLoad.isEnabled = false

        lifecycleScope.launch {
            try {
                val response = repo.getVisitsAt(selectedDate)
                if (response.isSuccessful) {
                    val visits = response.body() ?: emptyMap()
                    updateList(visits, selectedDate)
                } else {
                    Toast.makeText(this@AdminVisitsActivity, "Error al cargar datos", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@AdminVisitsActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                progressBar.visibility = View.GONE
                btnCurrent.isEnabled = true
                btnLoad.isEnabled = true
            }
        }
    }

    private fun updateList(visits: Map<String, VisitDataResponse>, dateInfo: String) {
        visitsList.clear()

        visits.forEach { (roomId, data) ->
            visitsList.add(VisitItem(roomId, data.name, data.visits))
        }

        visitsList.sortByDescending { it.visits }
        adapter.notifyDataSetChanged()

        if (visitsList.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
            rvVisits.visibility = View.GONE
        } else {
            tvEmpty.visibility = View.GONE
            rvVisits.visibility = View.VISIBLE
        }
    }
}

data class VisitItem(
    val roomId: String,
    val roomName: String,
    val visits: Int
)
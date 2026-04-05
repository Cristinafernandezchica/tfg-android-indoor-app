package com.cristina.tfg_android_indoor_app

import android.app.Application
import android.content.Intent
import com.cristina.tfg_android_indoor_app.services.BeaconScanService
import org.altbeacon.beacon.BeaconManager
import org.altbeacon.beacon.BeaconParser

class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // Configurar BeaconManager
        val beaconManager = BeaconManager.getInstanceForApplication(this)
        beaconManager.beaconParsers.add(
            BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24")
        )
        beaconManager.backgroundScanPeriod = 1100L
        beaconManager.backgroundBetweenScanPeriod = 0L
        beaconManager.foregroundScanPeriod = 1100L
        beaconManager.foregroundBetweenScanPeriod = 0L

        // Iniciar servicio de escaneo continuo automáticamente (para TODOS los usuarios)
        startBeaconScanService()
    }

    private fun startBeaconScanService() {
        val intent = Intent(this, BeaconScanService::class.java)
        startService(intent)
    }
}
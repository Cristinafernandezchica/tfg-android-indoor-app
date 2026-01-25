package com.cristina.tfg_android_indoor_app

import android.app.Application
import org.altbeacon.beacon.BeaconManager
import org.altbeacon.beacon.BeaconParser

class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()

        val beaconManager = BeaconManager.getInstanceForApplication(this)

        // Layout estándar para iBeacon
        beaconManager.beaconParsers.add(
            BeaconParser().setBeaconLayout(
                "m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"
            )
        )

        // Ajusta intervalos si quieres menor consumo / más rapidez
        beaconManager.backgroundScanPeriod = 1100L
        beaconManager.backgroundBetweenScanPeriod = 0L
        beaconManager.foregroundScanPeriod = 1100L
        beaconManager.foregroundBetweenScanPeriod = 0L
    }
}

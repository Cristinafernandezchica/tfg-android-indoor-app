package com.cristina.tfg_android_indoor_app.beacons

import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import org.altbeacon.beacon.BeaconConsumer
import org.altbeacon.beacon.BeaconManager
import org.altbeacon.beacon.Region

class BeaconScanner(
    private val context: Context,
    private val onBeaconsDetected: (List<DetectedBeacon>) -> Unit
) : BeaconConsumer {

    private val beaconManager: BeaconManager =
        BeaconManager.getInstanceForApplication(context)

    override fun getApplicationContext(): Context = context

    override fun bindService(
        intent: Intent,
        serviceConnection: ServiceConnection,
        flags: Int
    ): Boolean {
        return context.bindService(intent, serviceConnection, flags)
    }

    override fun unbindService(serviceConnection: ServiceConnection) {
        context.unbindService(serviceConnection)
    }

    override fun onBeaconServiceConnect() {
        beaconManager.removeAllRangeNotifiers()

        beaconManager.addRangeNotifier { beacons, _ ->
            val detected = beacons.map { beacon ->
                val uuid = beacon.id1.toString()
                val major = beacon.id2.toInt()
                val minor = beacon.id3.toInt()
                val roomId = BeaconConfig.findRoomFor(uuid, major, minor)

                DetectedBeacon(
                    uuid = uuid,
                    major = major,
                    minor = minor,
                    rssi = beacon.rssi,
                    roomId = roomId
                )
            }

            onBeaconsDetected(detected)
        }

        // Región genérica: escanea todos los beacons iBeacon
        val region = Region("all-beacons-region", null, null, null)
        beaconManager.startRangingBeaconsInRegion(region)
    }

    fun start() {
        beaconManager.bind(this)
    }

    fun stop() {
        beaconManager.unbind(this)
    }
}

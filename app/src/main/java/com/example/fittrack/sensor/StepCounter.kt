package com.example.fittrack.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/** Hardware feature: emits steps taken since the flow started collecting. Null = no sensor. */
fun stepsSinceStart(context: Context): Flow<Int>? {
    val sm = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    val sensor = sm.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) ?: return null
    return callbackFlow {
        var baseline = -1f
        val listener = object : SensorEventListener {
            override fun onSensorChanged(e: SensorEvent) {
                if (baseline < 0) baseline = e.values[0]
                trySend((e.values[0] - baseline).toInt())
            }
            override fun onAccuracyChanged(s: Sensor?, a: Int) {}
        }
        sm.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)
        awaitClose { sm.unregisterListener(listener) }
    }
}

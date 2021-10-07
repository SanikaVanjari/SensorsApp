package com.practice.sensorsapp

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

private const val TAG = "ProximityActivity"

class ProximityActivity : AppCompatActivity(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_proximity)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    override fun onResume() {
        super.onResume()
        sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)?.also { proximity ->
            sensorManager.registerListener(this, proximity, SensorManager.SENSOR_DELAY_NORMAL)
            Log.d(TAG, "onResume: ${proximity.maximumRange}")
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        val distance = event?.values?.get(0)
        Log.d(TAG, "onSensorChanged: $distance")
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }
}

/**
 * Some proximity sensors return binary values that represent "near" or "far."
 * In this case, the sensor usually reports its maximum range value in the far state and a lesser value in the near state.
 * Typically, the far value is a value > 5 cm, but this can vary from sensor to sensor.
 * You can determine a sensor's maximum range by using the getMaximumRange() method.
 * */
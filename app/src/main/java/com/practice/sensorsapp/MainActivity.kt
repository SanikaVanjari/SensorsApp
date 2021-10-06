package com.practice.sensorsapp

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {
    private lateinit var sensorManager: SensorManager
    private var mSensor: Sensor? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        // To get all types of sensors
        val detectedSensors: List<Sensor> = sensorManager.getSensorList(Sensor.TYPE_ALL)

        val textView: TextView = findViewById(R.id.textView)

        textView.text = detectedSensors.toString()

        // To check if a specific sensor is present or not
        if (sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null) {
            Log.d(TAG, "onCreate: yes success")
        } else {
            Log.d(TAG, "onCreate: sad")
        }

        // To check specific version of a sensor exists
        if (sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY) != null) {
            val gravSensors: List<Sensor> = sensorManager.getSensorList(Sensor.TYPE_GRAVITY)
            mSensor =
                gravSensors.firstOrNull { it.vendor.contains("Google LLC") && it.version == 3 }
        }
        if (mSensor == null) {
            mSensor = if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            } else {
                null
            }
        }
        Log.d(TAG, "onCreate: ${mSensor?.name}")

        //To check if a sensor is a streaming sensor - if value greater than 0 then yes,
        // or will only send data when the parameters change
        Log.d(TAG, "onCreate: ${mSensor?.minDelay}")
    }
}
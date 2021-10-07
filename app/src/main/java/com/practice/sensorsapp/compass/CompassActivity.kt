package com.practice.sensorsapp.compass

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.practice.sensorsapp.R

class CompassActivity : AppCompatActivity() {

    private lateinit var directionTextView: TextView
    private lateinit var compassImageView: ImageView
    private lateinit var broadcastReceiver: BroadcastReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_compass)
        directionTextView = findViewById(R.id.directionTextView)
        compassImageView = findViewById(R.id.compassImageView)

        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val direction = intent.getStringExtra(CompassService.KEY_DIRECTION)
                val angle = intent.getDoubleExtra(CompassService.KEY_ANGLE, 0.0)
                val angleWithDirection = "$angle  $direction"
                directionTextView.text = angleWithDirection
                compassImageView.rotation = angle.toFloat() * -1
            }
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(
            broadcastReceiver,
            IntentFilter(CompassService.KEY_ON_SENSOR_CHANGED_ACTION)
        )
    }

    override fun onResume() {
        super.onResume()
        startForegroundServiceForSensors(false)
    }

    override fun onPause() {
        super.onPause()
        startForegroundServiceForSensors(true)
    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver)
        super.onDestroy()
    }

    private fun startForegroundServiceForSensors(background: Boolean) {
        val intent = Intent(this, CompassService::class.java)
        intent.putExtra(CompassService.KEY_BACKGROUND, background)
        ContextCompat.startForegroundService(this, intent)
    }
}
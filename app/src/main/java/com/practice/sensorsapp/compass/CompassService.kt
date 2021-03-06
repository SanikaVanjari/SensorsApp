package com.practice.sensorsapp.compass

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.practice.sensorsapp.R
import com.practice.sensorsapp.compass.CompassService.Companion.KEY_NOTIFICATION_ID
import com.practice.sensorsapp.compass.CompassService.Companion.KEY_NOTIFICATION_STOP_ACTION
import kotlin.math.round

class CompassService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private val accelerometerReading = FloatArray(3)
    private val magnetometerReading = FloatArray(3)

    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    private var background = false

    private val notificationActivityRequestCode = 0
    private val notificationId = 1
    private val notificationStopRequestCode = 2


    companion object {
        val KEY_ANGLE = "angle"
        val KEY_DIRECTION = "direction"
        val KEY_BACKGROUND = "background"
        val KEY_NOTIFICATION_ID = "notificationId"
        val KEY_ON_SENSOR_CHANGED_ACTION = "com.practice.sensorsapp.compass.ON_SENSOR_CHANGED"
        val KEY_NOTIFICATION_STOP_ACTION = "com.practice.sensorsapp.compass.NOTIFICATION_STOP"
    }

    override fun onCreate() {
        super.onCreate()

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also { accelerometer ->
            sensorManager.registerListener(
                this,
                accelerometer,
                SensorManager.SENSOR_DELAY_NORMAL,
                SensorManager.SENSOR_DELAY_UI
            )
        }
        sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)?.also { magnetometer ->
            sensorManager.registerListener(
                this,
                magnetometer, SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI
            )
        }

        val notification = createNotification(getString(R.string.not_available), 0.0)
        startForeground(notificationId, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            background = it.getBooleanExtra(KEY_BACKGROUND, false)
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) {
            return
        }
        if (event.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, accelerometerReading, 0, accelerometerReading.size)
        } else if (event.sensor?.type == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, magnetometerReading, 0, magnetometerReading.size)
        }
        updateOrientationAngles()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }

    private fun updateOrientationAngles() {
        SensorManager.getRotationMatrix(
            rotationMatrix,
            null,
            accelerometerReading,
            magnetometerReading
        )
        val orientation = SensorManager.getOrientation(rotationMatrix, orientationAngles)
        val degrees = (Math.toDegrees(orientation[0].toDouble()) + 360.0) % 360.0
        val angle = round(degrees * 100) / 100
        val direction = getDirection(degrees)

        val intent = Intent()
        intent.putExtra(KEY_ANGLE, angle)
        intent.putExtra(KEY_DIRECTION, direction)
        intent.action = KEY_ON_SENSOR_CHANGED_ACTION
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)

        if (background) {
            val notification = createNotification(direction, angle)
            startForeground(notificationId, notification)
        } else {
            stopForeground(true)
        }
    }

    private fun getDirection(angle: Double): String {
        var direction = ""
        when {
            angle >= 350 || angle <= 10 -> direction = "N"
            angle < 350 && angle > 280 -> direction = "NW"
            angle <= 280 && angle > 260 -> direction = "W"
            angle <= 260 && angle > 190 -> direction = "SW"
            angle <= 190 && angle > 170 -> direction = "S"
            angle <= 170 && angle > 100 -> direction = "SE"
            angle <= 100 && angle > 80 -> direction = "E"
            angle <= 80 && angle > 10 -> direction = "NE"
        }
        return direction
    }

    private fun createNotification(direction: String, angle: Double): Notification {
        // 1
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                application.packageName,
                "Notifications", NotificationManager.IMPORTANCE_DEFAULT
            )

            // Configure the notification channel.
            notificationChannel.enableLights(false)
            notificationChannel.setSound(null, null)
            notificationChannel.enableVibration(false)
            notificationChannel.vibrationPattern = longArrayOf(0L)
            notificationChannel.setShowBadge(false)
            notificationManager.createNotificationChannel(notificationChannel)
        }

        val notificationBuilder = NotificationCompat.Builder(baseContext, application.packageName)
        // 2
        val contentIntent = PendingIntent.getActivity(
            this, notificationActivityRequestCode,
            Intent(this, CompassActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT
        )
        // 3
        val stopNotificationIntent = Intent(this, ActionListener::class.java)
        stopNotificationIntent.action = KEY_NOTIFICATION_STOP_ACTION
        stopNotificationIntent.putExtra(KEY_NOTIFICATION_ID, notificationId)
        val pendingStopNotificationIntent =
            PendingIntent.getBroadcast(
                this,
                notificationStopRequestCode,
                stopNotificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )

        notificationBuilder.setAutoCancel(true)
            .setDefaults(Notification.DEFAULT_ALL)
            .setContentTitle(resources.getString(R.string.app_name))
            .setContentText("You're currently facing $direction at an angle of $angle??")
            .setWhen(System.currentTimeMillis())
            .setDefaults(0)
            .setVibrate(longArrayOf(0L))
            .setSound(null)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentIntent(contentIntent)
            .addAction(
                R.mipmap.ic_launcher_round,
                getString(R.string.stop_notifications),
                pendingStopNotificationIntent
            )

        return notificationBuilder.build()
    }
}

class ActionListener : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {

        if (intent != null && intent.action != null) {
            // 1
            if (intent.action.equals(KEY_NOTIFICATION_STOP_ACTION)) {
                context?.let {
                    // 2
                    val notificationManager =
                        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    val intent = Intent(context, CompassService::class.java)
                    // 3
                    context.stopService(intent)
                    val notificationId = intent.getIntExtra(KEY_NOTIFICATION_ID, -1)
                    if (notificationId != -1) {
                        // 4
                        notificationManager.cancel(notificationId)
                    }
                }
            }
        }
    }
}

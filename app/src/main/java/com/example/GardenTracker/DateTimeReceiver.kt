package com.example.GardenTracker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.GardenTracker.fragments.CropFragment
import java.util.*

// This Broadcast Receiver will be registered so that
// we're (hopefully) constantly listening to the time
// and it will then update our crops time variables and check if they're ready
class DateTimeReceiver(timeData: MainActivity.DateTimeHolder) : BroadcastReceiver() {

    private val TAG = "DATE_TIME_RECEIVER"

    override fun onReceive(context: Context, intent: Intent) {

        Log.d(TAG, "Received watering schedule time.")

        val wateringTime = intent.getLongExtra("EXTRA_WATERING_TIME", -1)

        if (wateringTime != -1L) {
            // Check if current time matches the watering time, then show the notification
            showWateringNotification(context)
        }
    }

    private fun showWateringNotification(context: Context) {

        Log.d(TAG, "Building notification..")

        val builder = NotificationCompat.Builder(context, "WaterReminderChannel")
            .setSmallIcon(R.drawable.ic_baseline_opacity_24)
            .setContentTitle("Water Alert")
            .setContentText("Your plants need watering!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(1, builder.build())
    }
}
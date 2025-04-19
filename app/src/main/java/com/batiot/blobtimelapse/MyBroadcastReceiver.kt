package com.batiot.blobtimelapse

import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.util.Log
import android.widget.Toast
import androidx.camera.core.impl.utils.ContextUtil.getApplicationContext
import androidx.core.app.NotificationCompat


class MyBroadcastReceiver : BroadcastReceiver() {

    companion object {
        const val REQUEST_CODE = 12345
    }

    override fun onReceive(context: Context, intent: Intent?) {
        // Handle the broadcast
        Log.d("Blob", "MyBroadcastReceiver receive "+context)


        //forgroundNotif(context)

        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = pm.newWakeLock(
            (PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.FULL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP),
            "Blob:wake"
        )
        wakeLock.acquire()


        scheduleAlarm(context)

        // You can start an activity
        var intent = Intent(context, PhotoActivity::class.java)
        intent.setAction(MyWorker.ACTION_TAKE_PHOTO)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        //val options = ActivityOptions.makeBasic().;//.setPendingIntentBackgroundActivityStartMode(ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED)
        context.startActivity(intent);
        wakeLock.release();
    }

    private fun forgroundNotif(context: Context){
        val CHANNEL_ID = "blobl_channel_id"
        val NOTIFICATION_ID = 4321
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Blob title")
            .setContentText("Blob text")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, builder)
    }


    @SuppressLint("ScheduleExactAlarm")
    private fun scheduleAlarm(context: Context) {
        Log.d("Blob", "Scheduled alarm begin ")
        // Construct an intent that will execute the AlarmReceiver
        val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context,MyBroadcastReceiver::class.java)
        // Create a PendingIntent to be triggered when the alarm goes off
        val pIntent = PendingIntent.getBroadcast(
            context, REQUEST_CODE,
            intent, PendingIntent.FLAG_UPDATE_CURRENT
        )
        alarm.cancel(pIntent)
        val firstMillis = System.currentTimeMillis() + (15*60*1000); //dand 15 min
        alarm.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            firstMillis,
            pIntent
        )
        Log.d("Blob", "Scheduled alarm end")
        Toast.makeText(context, "Scheduled alarm", Toast.LENGTH_SHORT).show()
    }
}
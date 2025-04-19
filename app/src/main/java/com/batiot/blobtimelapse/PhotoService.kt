package com.batiot.blobtimelapse



import android.app.ForegroundServiceStartNotAllowedException
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat


class PhotoService : JobService() {

    override fun onStartJob(params: JobParameters): Boolean {
        // This is where you put the code to perform the background task

        // For this example, we'll just log a message
        Log.d("Blob", "Job started")

        var intent = Intent(this, PhotoActivity::class.java)
        intent.setAction(MyWorker.ACTION_TAKE_PHOTO)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        this.startActivity(intent);
        // Return true if the job needs to continue in a separate thread, false otherwise
        return false
    }

    override fun onStopJob(params: JobParameters): Boolean {
        // This is called if the job is interrupted, you can return true to reschedule the job
        return false
    }


    private fun startForeground() {
        Log.d("Blob", "startForeground() was called")
        try {

            val channel = NotificationChannel(
                "CHANNEL_ID",
                "PennSkanvTicChannel",
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = "PennSkanvTic channel for foreground service notification"

            val notificationManager =  getSystemService<NotificationManager>(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)

            val notification = NotificationCompat.Builder(this, "CHANNEL_ID")
                // Create the notification to display while the service is running
                .setOngoing(true)
                .setContentTitle("Blob Titre")
                .setContentText("Blob Text")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build()
            ServiceCompat.startForeground(
                /* service = */ this,
                /* id = */ 100, // Cannot be 0
                /* notification = */ notification,
                /* foregroundServiceType = */
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA
                } else {
                    0
                },
            )
        } catch (e: Exception) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                && e is ForegroundServiceStartNotAllowedException) {
                Log.d("Blob", "Exception startForeground")
            }
            // ...
        }
        Log.d("Blob", "startForeground() end")
    }
}
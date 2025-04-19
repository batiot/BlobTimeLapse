package com.batiot.blobtimelapse;

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.text.SimpleDateFormat
import java.util.Locale

class MyWorker(appContext: Context, workerParams: WorkerParameters) :  Worker(appContext, workerParams) {

    companion object {
        const val ACTION_TAKE_PHOTO = "com.batiot.blobtimelapse.ACTION_TAKE_PHOTO"
    }

    override fun doWork(): Result {
        // Do your work here, for example, take a photo
        val name = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.FRANCE)
                .format(System.currentTimeMillis())
        Log.d("Blob", "MyWorker Doing some work! $name")

        // Send a broadcast to trigger To My broadcast receiver
        //that will start the PhotoActivity
        val intent= Intent(ACTION_TAKE_PHOTO).apply {
            setPackage("com.batiot.blobtimelapse")
        }
        applicationContext.sendBroadcast(intent)

        // Indicate whether the work finished successfully with the Result
        return Result.success()
    }
}
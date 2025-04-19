package com.batiot.blobtimelapse

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.batiot.blobtimelapse.MyWorker.Companion.ACTION_TAKE_PHOTO
import com.batiot.blobtimelapse.databinding.ActivityMainBinding
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityMainBinding

    private lateinit var periodicWorkRequest: PeriodicWorkRequest

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        //API 29 android 10
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        // Set up the listener for the button
        viewBinding.launchPhotoButton.setOnClickListener {
            val intent = Intent(this, PhotoActivity::class.java)
            startActivity(intent)
        }

        viewBinding.simuEventButton.setOnClickListener {
            Log.d("Blob", "Simu event")
            val intent = Intent(
                this,
                MyBroadcastReceiver::class.java
            )
            this.sendBroadcast(intent)
        }

        viewBinding.imageTimelapseButton.setOnClickListener {
            //schedulePeriodicJob()
            this.sendBroadcast(Intent(
                this,
                MyBroadcastReceiver::class.java
            ))
        }
    }

    private fun schedulePeriodicJob() {
        val jobScheduler = getSystemService(JOB_SCHEDULER_SERVICE) as JobScheduler
        jobScheduler.cancelAll()
        viewBinding.imageTimelapseButton.text = "TimeLapse"

        // The JobService that we want to run
        val name = ComponentName(this, PhotoService::class.java)

        // Schedule the job
        val result = jobScheduler.schedule(JobInfo.Builder(123, name)
            .setPeriodic(TimeUnit.MINUTES.toMillis(15),TimeUnit.MINUTES.toMillis(1) )//toutes les 15 minutesà une minute pret
            .build())

        // If successfully scheduled, log this thing
        if (result == JobScheduler.RESULT_SUCCESS) {
            Log.d("Blob", "Scheduled job successfully!")
            viewBinding.imageTimelapseButton.text = "TimeLapse en cours"
            Toast.makeText(this, "Scheduled job", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    private fun scheduleAlarm() {
        // Construct an intent that will execute the AlarmReceiver
        Log.d("Blob", "scheduleAlarm")
        val alarm = getSystemService(ALARM_SERVICE) as AlarmManager
        viewBinding.imageTimelapseButton.text = "TimeLapse en cours"

        val intent = Intent(
            this,
            MyBroadcastReceiver::class.java
        )
        // Create a PendingIntent to be triggered when the alarm goes off
        val pIntent = PendingIntent.getBroadcast(
            this, MyBroadcastReceiver.REQUEST_CODE,
            intent, PendingIntent.FLAG_UPDATE_CURRENT
        )
        alarm.cancel(pIntent)

        val firstMillis = System.currentTimeMillis()+ AlarmManager.INTERVAL_FIFTEEN_MINUTES //dand 15 min
        alarm.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            firstMillis,
            pIntent
        )
        Toast.makeText(this, "Scheduled alarm", Toast.LENGTH_SHORT).show()
    }


    private fun scheduleRecurringWork() {
        periodicWorkRequest = PeriodicWorkRequest.Builder(
            MyWorker::class.java,
            15,
            TimeUnit.MINUTES
        ).build()
        WorkManager.getInstance(this).cancelAllWork()
        WorkManager.getInstance(this).enqueue(periodicWorkRequest)

        // Get the LiveData<WorkInfo>
        val workInfoLiveData =
            WorkManager.getInstance(this).getWorkInfoByIdLiveData(periodicWorkRequest.id)

        // Observe the LiveData
        workInfoLiveData.observe(this, { workInfo ->
            // Handle the WorkInfo
            if (workInfo != null) {
                val state = workInfo.state
                // Check the state
                when (state) {
                    WorkInfo.State.ENQUEUED -> {
                        // Work is enqueued, it will run soon
                        Log.d("Blob", "MyWorker will run soon")
                    }

                    WorkInfo.State.RUNNING -> {
                        // Work is running, it will run again after the period
                        Log.d("Blob", "MyWorker is running, it will run again after the period")
                    }

                    WorkInfo.State.SUCCEEDED -> {
                        // Work succeeded, it won't run again
                        Log.d("Blob", "MyWorker succeeded, it won't run again")
                    }

                    WorkInfo.State.FAILED -> {
                        // Work failed, it won't run again
                        Log.d("Blob", "MyWorker failed, it won't run again")
                    }

                    WorkInfo.State.BLOCKED -> {
                        // Work is blocked, it will run when the constraints are met
                        Log.d(
                            "Blob",
                            "MyWorker is blocked, it will run when the constraints are met"
                        )
                    }

                    WorkInfo.State.CANCELLED -> {
                        // Work was cancelled, it won't run again
                        Log.d("Blob", "MyWorker was cancelled, it won't run again")
                    }
                }
            }
        })

        Log.d("Blob", "MyWorker schedule")
        Toast.makeText(this, "MyWorker schedule", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        WorkManager.getInstance(this).cancelAllWork()
        val jobScheduler = getSystemService(JOB_SCHEDULER_SERVICE) as JobScheduler
        jobScheduler.cancelAll()
        Log.d("Blob", "jobScheduler Off")
        Toast.makeText(this, "jobScheduler Off", Toast.LENGTH_SHORT).show()
        viewBinding.imageTimelapseButton.text = "TimeLapse"
    }


}
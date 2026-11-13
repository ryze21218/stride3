package com.example.stride

import android.app.Application
import androidx.room.Room
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.stride.data.AppDatabase
import com.example.stride.data.Prefs
import com.example.stride.data.StepRepository
import com.example.stride.sensor.StepSyncWorker
import java.util.concurrent.TimeUnit

class StrideApp : Application() {

    lateinit var prefs: Prefs
        private set
    lateinit var repository: StepRepository
        private set

    override fun onCreate() {
        super.onCreate()
        prefs = Prefs(this)
        val notifier = GoalNotifier(this).also { it.ensureChannel() }
        val db = Room.databaseBuilder(this, AppDatabase::class.java, "stride.db").build()
        repository = StepRepository(db.stepDao(), prefs, notifier)

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "step-sync",
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<StepSyncWorker>(15, TimeUnit.MINUTES).build(),
        )
    }
}

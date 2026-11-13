package com.example.stride.sensor

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.stride.StrideApp
import com.example.stride.hasActivityPermission

/** Runs about every 15 minutes so steps are recorded even when the app is closed. */
class StepSyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        if (!hasActivityPermission(applicationContext)) return Result.success()
        val raw = StepSensor.readOnce(applicationContext) ?: return Result.success()
        (applicationContext as StrideApp).repository.onSensorReading(raw)
        return Result.success()
    }
}

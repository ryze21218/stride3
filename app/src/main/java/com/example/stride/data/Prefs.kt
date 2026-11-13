package com.example.stride.data

import android.content.Context

class Prefs(context: Context) {
    private val sp = context.getSharedPreferences("stride", Context.MODE_PRIVATE)

    var goal: Int
        get() = sp.getInt("goal", 8000)
        set(v) = sp.edit().putInt("goal", v).apply()

    var heightCm: Int
        get() = sp.getInt("height_cm", 170)
        set(v) = sp.edit().putInt("height_cm", v).apply()

    var weightKg: Int
        get() = sp.getInt("weight_kg", 70)
        set(v) = sp.edit().putInt("weight_kg", v).apply()

    /** Last raw value read from TYPE_STEP_COUNTER (steps since boot), or -1 if never read. */
    var lastRaw: Long
        get() = sp.getLong("last_raw", -1L)
        set(v) = sp.edit().putLong("last_raw", v).apply()

    /** Date we last sent the "goal reached" notification, so it fires once per day. */
    var notifiedDate: String?
        get() = sp.getString("notified_date", null)
        set(v) = sp.edit().putString("notified_date", v).apply()
}

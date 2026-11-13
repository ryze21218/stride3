package com.example.stride.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.stride.StrideApp
import com.example.stride.sensor.StepSensor
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import kotlin.math.roundToInt

data class Settings(val goal: Int, val heightCm: Int, val weightKg: Int)

data class DayBar(val date: LocalDate, val steps: Int)

data class UiState(
    val date: LocalDate,
    val settings: Settings,
    val today: Int = 0,
    val week: List<DayBar> = emptyList(),
    val streak: Int = 0,
    val distanceKm: Double = 0.0,
    val calories: Int = 0,
)

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(app: Application) : AndroidViewModel(app) {

    private val strideApp = app as StrideApp
    private val repo = strideApp.repository
    private val prefs = strideApp.prefs

    val sensorAvailable: Boolean = StepSensor.isAvailable(app)

    private val settings = MutableStateFlow(
        Settings(prefs.goal, prefs.heightCm, prefs.weightKg)
    )
    private val today = MutableStateFlow(LocalDate.now())
    private var trackingJob: Job? = null

    val state: StateFlow<UiState> = today
        .flatMapLatest { day ->
            combine(repo.observeSince(day.minusDays(59)), settings) { rows, s ->
                buildState(day, rows.associate { LocalDate.parse(it.date) to it.steps }, s)
            }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            UiState(date = today.value, settings = settings.value),
        )

    /** Listen to the step sensor while the screen is visible. */
    fun startTracking() {
        if (trackingJob?.isActive == true) return
        trackingJob = viewModelScope.launch {
            StepSensor.readings(getApplication()).collect { repo.onSensorReading(it) }
        }
    }

    fun stopTracking() {
        trackingJob?.cancel()
        trackingJob = null
    }

    /** Call when the app returns to the foreground so "today" rolls over at midnight. */
    fun refreshDate() {
        today.value = LocalDate.now()
    }

    fun updateSettings(new: Settings) {
        val clean = Settings(
            goal = new.goal.coerceIn(1_000, 100_000),
            heightCm = new.heightCm.coerceIn(100, 250),
            weightKg = new.weightKg.coerceIn(30, 250),
        )
        prefs.goal = clean.goal
        prefs.heightCm = clean.heightCm
        prefs.weightKg = clean.weightKg
        settings.value = clean
    }

    private fun buildState(day: LocalDate, byDate: Map<LocalDate, Int>, s: Settings): UiState {
        val todaySteps = byDate[day] ?: 0
        val week = (6 downTo 0).map {
            val d = day.minusDays(it.toLong())
            DayBar(d, byDate[d] ?: 0)
        }

        // Streak: consecutive days at or above the goal. Today only counts once it's reached.
        var streak = 0
        var d = if (todaySteps >= s.goal) day else day.minusDays(1)
        while ((byDate[d] ?: 0) >= s.goal && streak < 60) {
            streak++
            d = d.minusDays(1)
        }

        // Estimates: stride length is about 41.5% of height; walking burns ~0.57 kcal per kg per km.
        val strideMeters = s.heightCm * 0.415 / 100.0
        val km = todaySteps * strideMeters / 1000.0
        val kcal = (0.57 * s.weightKg * km).roundToInt()

        return UiState(day, s, todaySteps, week, streak, km, kcal)
    }
}

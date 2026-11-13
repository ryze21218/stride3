package com.example.stride.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.stride.hasActivityPermission
import com.example.stride.permissionsToRequest
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HomeScreen(vm: HomeViewModel = viewModel()) {
    val context = LocalContext.current
    val state by vm.state.collectAsStateWithLifecycle()
    var hasPermission by remember { mutableStateOf(hasActivityPermission(context)) }
    var showSettings by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        hasPermission = hasActivityPermission(context)
        if (hasPermission) vm.startTracking()
    }

    LifecycleEventEffect(Lifecycle.Event.ON_START) {
        hasPermission = hasActivityPermission(context)
        vm.refreshDate()
        if (hasPermission) vm.startTracking()
    }
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) { vm.stopTracking() }

    Column(
        Modifier
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                state.date.format(DateTimeFormatter.ofPattern("EEEE d MMMM", Locale.getDefault())),
                style = MaterialTheme.typography.titleMedium,
            )
            TextButton(onClick = { showSettings = true }) { Text("Edit goal") }
        }
        Spacer(Modifier.height(16.dp))

        when {
            !vm.sensorAvailable -> Notice(
                "No step counter found",
                "This phone doesn't have a step counter sensor, so Stride can't count steps here.",
            )
            !hasPermission -> Column {
                Notice(
                    "Allow step counting",
                    "Stride reads your phone's step counter. It needs the Physical activity permission to do that.",
                )
                Spacer(Modifier.height(16.dp))
                Button(onClick = { permissionLauncher.launch(permissionsToRequest()) }) {
                    Text("Allow access")
                }
            }
            else -> Dashboard(state)
        }
    }

    if (showSettings) {
        SettingsDialog(
            current = state.settings,
            onDismiss = { showSettings = false },
            onSave = { vm.updateSettings(it); showSettings = false },
        )
    }
}

@Composable
private fun Notice(title: String, body: String) {
    Column {
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        Text(body, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun Dashboard(state: UiState) {
    val nf = remember { NumberFormat.getInstance() }
    val goal = state.settings.goal
    val reached = state.today >= goal

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        ProgressRing(
            progress = state.today / goal.toFloat(),
            reached = reached,
            modifier = Modifier.size(280.dp),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    nf.format(state.today),
                    fontSize = 52.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Ink,
                )
                Text(
                    if (reached) "Goal reached" else "of ${nf.format(goal)} steps",
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
    }

    Spacer(Modifier.height(28.dp))

    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        Stat(String.format(Locale.getDefault(), "%.1f km", state.distanceKm), "Distance")
        Stat("${nf.format(state.calories)} kcal", "Active energy")
        Stat(if (state.streak == 1) "1 day" else "${state.streak} days", "Goal streak")
    }

    Spacer(Modifier.height(32.dp))
    Text("Last 7 days", style = MaterialTheme.typography.titleMedium)
    Spacer(Modifier.height(12.dp))
    WeekChart(state.week, goal)
    Spacer(Modifier.height(8.dp))
    Text(
        "The dashed line is your daily goal.",
        style = MaterialTheme.typography.bodySmall,
        textAlign = TextAlign.Start,
    )
    Spacer(Modifier.height(4.dp))
    Text(
        "Distance and energy are estimates based on your height and weight.",
        style = MaterialTheme.typography.bodySmall,
    )
}

@Composable
private fun Stat(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Text(label, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun SettingsDialog(current: Settings, onDismiss: () -> Unit, onSave: (Settings) -> Unit) {
    var goal by remember { mutableStateOf(current.goal.toString()) }
    var height by remember { mutableStateOf(current.heightCm.toString()) }
    var weight by remember { mutableStateOf(current.weightKg.toString()) }
    val numeric = KeyboardOptions(keyboardType = KeyboardType.Number)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Your details") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    goal, { goal = it.filter(Char::isDigit) },
                    label = { Text("Daily step goal") }, singleLine = true, keyboardOptions = numeric,
                )
                OutlinedTextField(
                    height, { height = it.filter(Char::isDigit) },
                    label = { Text("Height (cm)") }, singleLine = true, keyboardOptions = numeric,
                )
                OutlinedTextField(
                    weight, { weight = it.filter(Char::isDigit) },
                    label = { Text("Weight (kg)") }, singleLine = true, keyboardOptions = numeric,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(
                    Settings(
                        goal.toIntOrNull() ?: current.goal,
                        height.toIntOrNull() ?: current.heightCm,
                        weight.toIntOrNull() ?: current.weightKg,
                    )
                )
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

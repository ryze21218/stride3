package com.example.stride.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.max

/**
 * The goal ring. The track is a trail of dots (a footpath); the solid arc is how far you've walked.
 */
@Composable
fun ProgressRing(
    progress: Float,
    reached: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val animated by animateFloatAsState(progress.coerceIn(0f, 1f), tween(900), label = "ring")
    Box(modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val arcWidth = 22.dp.toPx()
            val dot = 6.dp.toPx()
            val inset = arcWidth / 2
            val topLeft = Offset(inset, inset)
            val arcSize = Size(size.width - arcWidth, size.height - arcWidth)

            // Footpath track: round-capped zero-length dashes render as evenly spaced dots.
            val circumference = (Math.PI * arcSize.width).toFloat()
            val dots = 48
            drawArc(
                color = Mist,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(
                    width = dot,
                    cap = StrokeCap.Round,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(0.1f, circumference / dots)),
                ),
            )
            drawArc(
                color = if (reached) Sunrise else Stride,
                startAngle = -90f,
                sweepAngle = 360f * animated,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = arcWidth, cap = StrokeCap.Round),
            )
        }
        content()
    }
}

@Composable
fun WeekChart(days: List<com.example.stride.ui.DayBar>, goal: Int, modifier: Modifier = Modifier) {
    if (days.isEmpty()) return
    val top = max(goal, days.maxOf { it.steps }) * 1.15f

    androidx.compose.foundation.layout.Column(modifier) {
        Canvas(Modifier.fillMaxWidth().height(150.dp)) {
            val slot = size.width / days.size
            val barWidth = slot * 0.5f
            val radius = CornerRadius(8.dp.toPx())

            days.forEachIndexed { i, day ->
                val h = size.height * (day.steps / top)
                val isToday = i == days.lastIndex
                val color = when {
                    day.steps >= goal -> Sunrise
                    isToday -> Stride
                    else -> Stride.copy(alpha = 0.45f)
                }
                if (h > 0f) {
                    drawRoundRect(
                        color = color,
                        topLeft = Offset(i * slot + (slot - barWidth) / 2, size.height - h),
                        size = Size(barWidth, h),
                        cornerRadius = radius,
                    )
                }
            }

            val goalY = size.height * (1 - goal / top)
            drawLine(
                color = Ink.copy(alpha = 0.55f),
                start = Offset(0f, goalY),
                end = Offset(size.width, goalY),
                strokeWidth = 1.5.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10.dp.toPx(), 8.dp.toPx())),
            )
        }
        Row(Modifier.fillMaxWidth()) {
            days.forEachIndexed { i, day ->
                Text(
                    text = if (i == days.lastIndex) "Today"
                    else day.date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    fontWeight = if (i == days.lastIndex) FontWeight.SemiBold else FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
        }
    }
}

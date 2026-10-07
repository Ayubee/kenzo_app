package com.example.kunlikvazifalar.ui.components

import android.animation.ValueAnimator
import android.database.ContentObserver
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.kunlikvazifalar.theme.CalmGreenCheckDark
import com.example.kunlikvazifalar.theme.CalmGreenCheckLight

@Composable
fun systemAnimationsEnabled(): Boolean {
    val context = LocalContext.current
    fun enabled() = if (Build.VERSION.SDK_INT >= 26) ValueAnimator.areAnimatorsEnabled()
        else Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) != 0f
    var value by remember { mutableStateOf(enabled()) }
    DisposableEffect(context) {
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) { value = enabled() }
        }
        context.contentResolver.registerContentObserver(Settings.Global.getUriFor(Settings.Global.ANIMATOR_DURATION_SCALE), false, observer)
        onDispose { context.contentResolver.unregisterContentObserver(observer) }
    }
    return value
}

/**
 * 4-variant "Iliq minimal" talabi:
 * Bajarilgan vazifada sokin yashil galochka bo'lsin.
 * 200 ms sokin silliq animatsiya.
 */
@Composable
fun ProgressCheckbox(
    checked: Boolean,
    enabled: Boolean,
    animate: Boolean,
    description: String,
    onClick: () -> Unit
) {
    val progress by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = tween(if (animate) 200 else 0),
        label = "task-check-green"
    )
    val scheme = MaterialTheme.colorScheme
    val isDark = scheme.background.luminance() < 0.5f

    val greenColor = if (isDark) CalmGreenCheckDark else CalmGreenCheckLight
    val neutralBorderColor = if (isDark) Color(0xFF5E544A) else Color(0xFFC8BCB0)

    Box(
        modifier = Modifier
            .size(48.dp)
            .toggleable(checked, enabled = enabled, role = Role.Checkbox, onValueChange = { onClick() })
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.size(24.dp)) {
            val corner = CornerRadius(6.dp.toPx())

            // Unchecked outline border
            if (progress < 1f) {
                drawRoundRect(
                    color = neutralBorderColor,
                    cornerRadius = corner,
                    style = Stroke(1.75.dp.toPx())
                )
            }

            // Checked green fill & checkmark
            if (progress > 0f) {
                // Sokin yashil fon
                drawRoundRect(
                    color = greenColor.copy(alpha = progress),
                    cornerRadius = corner,
                    style = Fill
                )
                // Yashil chegara
                drawRoundRect(
                    color = greenColor.copy(alpha = progress),
                    cornerRadius = corner,
                    style = Stroke(1.75.dp.toPx())
                )

                // Oq galochka
                val path = Path().apply {
                    moveTo(size.width * 0.24f, size.height * 0.50f)
                    lineTo(size.width * 0.44f, size.height * 0.70f)
                    lineTo(size.width * 0.78f, size.height * 0.32f)
                }
                val measure = PathMeasure().apply { setPath(path, false) }
                val visible = Path()
                measure.getSegment(0f, measure.length * progress, visible)
                drawPath(
                    path = visible,
                    color = Color.White.copy(alpha = progress),
                    style = Stroke(2.2.dp.toPx(), cap = StrokeCap.Round)
                )
            }
        }
    }
}

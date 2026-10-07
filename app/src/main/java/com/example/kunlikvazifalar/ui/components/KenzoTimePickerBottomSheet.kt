package com.example.kunlikvazifalar.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kunlikvazifalar.data.preferences.UserPreferences
import java.util.Calendar
import java.util.Locale

/**
 * 3-variant asosida tayyorlangan pastdan chiquvchi vaqt tanlagich (ModalBottomSheet).
 * Material 3, katta 24 soatlik ko'rsatkich, kumulyativ tezkor tugmalar (+15, +30, +1 soat, Boshqa vaqt).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KenzoTimePickerBottomSheet(
    initialTime: String? = null,
    onSave: (String) -> Unit,
    onCancel: () -> Unit,
    onClear: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Boshlang'ich vaqtni aniqlash
    val calendar = Calendar.getInstance()
    var parsedHour = calendar.get(Calendar.HOUR_OF_DAY)
    var parsedMinute = calendar.get(Calendar.MINUTE)

    if (!initialTime.isNullOrBlank()) {
        val parts = initialTime.split(":")
        if (parts.size == 2) {
            val h = parts[0].toIntOrNull()
            val m = parts[1].toIntOrNull()
            if (h != null && m != null) {
                parsedHour = h
                parsedMinute = m
            }
        }
    }

    var hour by remember { mutableIntStateOf(parsedHour) }
    var minute by remember { mutableIntStateOf(parsedMinute) }

    // Animatsiya sozlamalari (ilovadagi va tizimdagi animatsiya cheklovlarini hurmat qilish)
    val prefs = remember { UserPreferences(context) }
    val appAnimations = prefs.animationsEnabled()
    val sysAnimations = systemAnimationsEnabled()
    val shouldAnimate = appAnimations && sysAnimations

    // Kumulyativ vaqt hisoblash (24 soat doirasida aylanib ketadi)
    fun addMinutes(minutesToAdd: Int) {
        val (h, m) = com.example.kunlikvazifalar.util.DateUtils.addMinutes(hour, minute, minutesToAdd)
        hour = h
        minute = m
    }

    val formattedTime = String.format(Locale.US, "%02d:%02d", hour, minute)

    ModalBottomSheet(
        onDismissRequest = onCancel,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 28.dp)
        ) {
            // Sarlavha: "Vaqtni tanlang" (chapga, qalin)
            Text(
                text = "Vaqtni tanlang",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Katta vaqt ko'rsatkichi: yumaloq (~16dp), ingichka chegarali konteyner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                    .border(
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clickable {
                        TimePickerHelper.showTimePicker(context, formattedTime) { customTime ->
                            val p = customTime.split(":")
                            if (p.size == 2) {
                                hour = p[0].toIntOrNull() ?: hour
                                minute = p[1].toIntOrNull() ?: minute
                            }
                        }
                    }
                    .padding(vertical = 18.dp),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = formattedTime,
                    transitionSpec = {
                        if (shouldAnimate) {
                            (fadeIn(animationSpec = tween(200)) + scaleIn(initialScale = 0.96f, animationSpec = tween(200)))
                                .togetherWith(fadeOut(animationSpec = tween(150)))
                        } else {
                            EnterTransition.None togetherWith ExitTransition.None
                        }
                    },
                    label = "time_display_anim"
                ) { displayTime ->
                    Text(
                        text = displayTime,
                        style = TextStyle(
                            fontSize = 60.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            letterSpacing = 2.sp,
                            textAlign = TextAlign.Center
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Tezkor tugmalar, 2x2 setka, balandligi ~48dp, yumaloq (~14dp), ingichka chegara
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickButton(
                        text = "+15 daqiqa",
                        onClick = { addMinutes(15) },
                        modifier = Modifier.weight(1f)
                    )
                    QuickButton(
                        text = "+30 daqiqa",
                        onClick = { addMinutes(30) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickButton(
                        text = "+1 soat",
                        onClick = { addMinutes(60) },
                        modifier = Modifier.weight(1f)
                    )
                    QuickButton(
                        text = "Boshqa vaqt",
                        onClick = {
                            TimePickerHelper.showTimePicker(context, formattedTime) { customTime ->
                                val p = customTime.split(":")
                                if (p.size == 2) {
                                    hour = p[0].toIntOrNull() ?: hour
                                    minute = p[1].toIntOrNull() ?: minute
                                }
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // "Saqlash": to'liq kenglik, sariq/oltin (#F2C044 atrofida), qalin to'q matn, ~52dp
            Button(
                onClick = { onSave(formattedTime) },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF2C044),
                    contentColor = Color(0xFF1E1B18)
                )
            ) {
                Text(
                    text = "Saqlash",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // "Bekor qilish": to'liq kenglik, ikkilamchi uslub, ingichka chegara
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                colors = ButtonDefaults.outlinedButtonColors()
            ) {
                Text(
                    text = "Bekor qilish",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Vaqtsiz qoldirish imkoniyati (agar berilgan bo'lsa)
            if (onClear != null) {
                Spacer(modifier = Modifier.height(6.dp))
                TextButton(
                    onClick = onClear,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Vaqtsiz qoldirish",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = 48.dp),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.UserProfile
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.BrandAccent
import com.example.ui.theme.CharcoalCard
import com.example.ui.theme.CharcoalCardElevated
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.TextInverse
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSubtle

/**
 * Profile screen.
 *
 * Only settings that this build actually applies are listed. Account, sync, appearance and
 * leaderboard-visibility controls are intentionally absent because nothing backs them yet.
 */
@Composable
fun ProfileScreen(
    userProfile: UserProfile,
    onOpenCalibration: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenScoringWorks: () -> Unit = {},
    soundEnabled: Boolean = true,
    onSoundToggle: (Boolean) -> Unit = {},
    hapticsEnabled: Boolean = true,
    onHapticsToggle: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp)
            .padding(top = 8.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = "Profile",
            color = TextPrimary,
            fontSize = 26.sp,
            fontWeight = FontWeight.Black
        )

        // 1. Identity + real performance stats in a single card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(CharcoalCard)
                .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
                .padding(18.dp)
                .testTag("identity_card")
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .background(CharcoalCardElevated, CircleShape)
                            .border(1.5.dp, BrandAccent, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userProfile.name.split(" ")
                                .mapNotNull { it.firstOrNull()?.uppercase() }
                                .take(2)
                                .joinToString("")
                                .ifBlank { "A" },
                            color = BrandAccent,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = userProfile.name,
                            color = TextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (userProfile.memberSince.isNotBlank()) {
                                "Training since ${userProfile.memberSince}"
                            } else {
                                "No sessions recorded yet"
                            },
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ProfileStat(label = "RPI", value = "${userProfile.rpi}")
                    StatDivider()
                    ProfileStat(
                        label = "BEST",
                        value = if (userProfile.fastestMs > 0) "${userProfile.fastestMs} ms" else "--"
                    )
                    StatDivider()
                    ProfileStat(label = "SESSIONS", value = "${userProfile.totalSessions}")
                }
            }
        }

        // 2. Settings that this build actually applies
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "SETTINGS",
                color = TextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(CharcoalCard)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
            ) {
                Column {
                    SettingNavRow(
                        title = "Calibration",
                        subtitle = "Measure this device's input latency",
                        icon = Icons.Default.Speed,
                        onClick = onOpenCalibration
                    )
                    RowDivider()
                    SettingNavRow(
                        title = "How scoring works",
                        subtitle = "Median, consistency and false starts explained",
                        icon = Icons.Outlined.Info,
                        onClick = onOpenScoringWorks
                    )
                    RowDivider()
                    SettingNavRow(
                        title = "Notifications",
                        subtitle = "Personal bests and streaks from your sessions",
                        icon = Icons.Default.Notifications,
                        onClick = onOpenNotifications
                    )
                    RowDivider()
                    SettingToggleRow(
                        title = "Sound cues",
                        icon = Icons.AutoMirrored.Filled.VolumeUp,
                        checked = soundEnabled,
                        onCheckedChange = onSoundToggle
                    )
                    RowDivider()
                    SettingToggleRow(
                        title = "Haptics",
                        icon = Icons.Default.Vibration,
                        checked = hapticsEnabled,
                        onCheckedChange = onHapticsToggle
                    )
                }
            }
        }

        Text(
            text = "Your sessions are stored only on this device. There is no account or cloud sync yet.",
            color = TextSubtle,
            fontSize = 11.sp,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}

@Composable
private fun ProfileStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun StatDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(32.dp)
            .background(BorderSubtle)
    )
}

@Composable
private fun RowDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(BorderSubtle.copy(alpha = 0.5f))
    )
}

@Composable
private fun SettingNavRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .testTag("setting_${title.lowercase().replace(" ", "_")}"),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Icon(imageVector = icon, contentDescription = null, tint = BrandAccent, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = title, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Text(text = subtitle, color = TextSubtle, fontSize = 11.sp)
            }
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = TextSubtle,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun SettingToggleRow(
    title: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("setting_${title.lowercase().replace(" ", "_")}"),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Icon(imageVector = icon, contentDescription = null, tint = BrandAccent, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = title, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = TextInverse,
                checkedTrackColor = BrandAccent,
                uncheckedThumbColor = TextMuted,
                uncheckedTrackColor = CharcoalCardElevated,
                uncheckedBorderColor = BorderSubtle
            )
        )
    }
}

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
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DisplaySettings
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.example.ui.theme.CoolBlue
import com.example.ui.theme.CoralWarning
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSubtle

@Composable
fun ProfileScreen(
    userProfile: UserProfile,
    onOpenCalibration: () -> Unit,
    onOpenNotifications: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var showSignOutConfirm by remember { mutableStateOf(false) }
    var activeFeedbackNotice by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp)
            .padding(top = 8.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Page title
        Text(
            text = "Profile",
            color = TextPrimary,
            fontSize = 26.sp,
            fontWeight = FontWeight.Black
        )

        // 1. Identity card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(CharcoalCard)
                .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
                .padding(18.dp)
                .testTag("identity_card")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .background(CharcoalCardElevated, CircleShape)
                            .border(1.5.dp, BrandAccent, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "AM",
                            color = BrandAccent,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = userProfile.name,
                                color = TextPrimary,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = userProfile.countryFlag, fontSize = 15.sp)
                        }
                        Text(
                            text = "${userProfile.handle} · ${userProfile.country}",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }
                }

                OutlinedButton(
                    onClick = { activeFeedbackNotice = "Profile editing enabled in cloud sync mode." },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Edit", fontSize = 11.sp, color = TextPrimary)
                }
            }
        }

        // 2. Performance snapshot
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(CharcoalCardElevated)
                .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Speed, contentDescription = null, tint = TextMuted, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(text = "RPI", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = "${userProfile.rpi}", color = BrandAccent, fontSize = 22.sp, fontWeight = FontWeight.Black)
                }
                Box(modifier = Modifier.width(1.dp).height(32.dp).background(BorderSubtle))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.EmojiEvents, contentDescription = null, tint = TextMuted, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(text = "GLOBAL", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = userProfile.rankLabel, color = CoolBlue, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                Box(modifier = Modifier.width(1.dp).height(32.dp).background(BorderSubtle))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null, tint = TextMuted, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(text = "MEMBER", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = userProfile.memberSince, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // 3. Settings list (Grouped)
        val settingsGroups = listOf(
            Triple("Account", Icons.Default.AccountCircle, listOf(
                SettingRowData("Edit profile", Icons.Default.Person) { activeFeedbackNotice = "Profile details synced." },
                SettingRowData("Notifications", Icons.Default.Notifications, onOpenNotifications)
            )),
            Triple("Performance", Icons.Default.Speed, listOf(
                SettingRowData("Device & input", Icons.Default.PhoneAndroid) { activeFeedbackNotice = "Hardware specs verified." },
                SettingRowData("Calibration", Icons.Default.Speed, onOpenCalibration),
                SettingRowData("Measurement guide", Icons.Default.Tune) { activeFeedbackNotice = "Measurement guide loaded." }
            )),
            Triple("Experience", Icons.Default.Tune, listOf(
                SettingRowData("Accessibility", Icons.Default.DisplaySettings) { activeFeedbackNotice = "High contrast theme enabled." },
                SettingRowData("Sound & haptics", Icons.AutoMirrored.Filled.VolumeUp) { activeFeedbackNotice = "Sound and haptics active." },
                SettingRowData("Appearance", Icons.Default.Tune) { activeFeedbackNotice = "Sport dark mode active." }
            )),
            Triple("Privacy", Icons.Default.Lock, listOf(
                SettingRowData("Leaderboard visibility", Icons.Default.Visibility) { activeFeedbackNotice = "Leaderboard visibility: Public." },
                SettingRowData("Data controls", Icons.Default.Lock) { activeFeedbackNotice = "Data stored securely in local database." }
            )),
            Triple("Support", Icons.AutoMirrored.Filled.HelpOutline, listOf(
                SettingRowData("Help & documentation", Icons.AutoMirrored.Filled.HelpOutline) { activeFeedbackNotice = "Documentation ready." },
                SettingRowData("Sign out", Icons.Default.Person) { showSignOutConfirm = true }
            ))
        )

        settingsGroups.forEach { (groupTitle, groupIcon, items) ->
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
                ) {
                    Icon(imageVector = groupIcon, contentDescription = null, tint = TextMuted, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = groupTitle.uppercase(),
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(CharcoalCard)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
                ) {
                    Column {
                        items.forEachIndexed { index, item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { item.action() }
                                    .padding(horizontal = 16.dp, vertical = 14.dp)
                                    .testTag("setting_${item.title.lowercase().replace(" ", "_")}"),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = null,
                                        tint = if (item.title == "Sign out") CoralWarning else BrandAccent,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = item.title,
                                        color = if (item.title == "Sign out") CoralWarning else TextPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = TextSubtle,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            if (index < items.size - 1) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(BorderSubtle.copy(alpha = 0.5f))
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Active notice toast / feedback
    if (activeFeedbackNotice != null) {
        AlertDialog(
            onDismissRequest = { activeFeedbackNotice = null },
            confirmButton = {
                TextButton(onClick = { activeFeedbackNotice = null }) {
                    Text(text = "OK", color = BrandAccent, fontWeight = FontWeight.Bold)
                }
            },
            title = { Text(text = "System Notice", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = { Text(text = activeFeedbackNotice ?: "", color = TextMuted, fontSize = 13.sp) },
            containerColor = CharcoalCard,
            shape = RoundedCornerShape(14.dp)
        )
    }

    // Sign out confirm dialog
    if (showSignOutConfirm) {
        AlertDialog(
            onDismissRequest = { showSignOutConfirm = false },
            confirmButton = {
                Button(
                    onClick = { showSignOutConfirm = false },
                    colors = ButtonDefaults.buttonColors(containerColor = CoralWarning, contentColor = Color.White),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(text = "Sign Out", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutConfirm = false }) {
                    Text(text = "Cancel", color = TextMuted)
                }
            },
            title = { Text(text = "Confirm Sign Out", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = { Text(text = "Your local training history will remain cached on this device.", color = TextMuted, fontSize = 13.sp) },
            containerColor = CharcoalCard,
            shape = RoundedCornerShape(14.dp)
        )
    }
}

private data class SettingRowData(
    val title: String,
    val icon: ImageVector,
    val action: () -> Unit
)

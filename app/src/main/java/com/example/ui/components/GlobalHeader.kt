package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.BrandAccent
import com.example.ui.theme.CharcoalCard
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

@Composable
fun GlobalHeader(
    hasUnreadNotifications: Boolean,
    onWordmarkClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onAvatarClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(DarkBackground)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Wordmark R/
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable { onWordmarkClick() }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .testTag("wordmark_home_button")
            ) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = TextPrimary, fontWeight = FontWeight.Black, fontSize = 24.sp, fontFamily = FontFamily.SansSerif)) {
                            append("R")
                        }
                        withStyle(SpanStyle(color = BrandAccent, fontWeight = FontWeight.Black, fontSize = 24.sp, fontFamily = FontFamily.SansSerif)) {
                            append("/")
                        }
                    }
                )
            }

            // Right: Notifications & Avatar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Notification bell with unread dot
                Box(contentAlignment = Alignment.TopEnd) {
                    IconButton(
                        onClick = onNotificationsClick,
                        modifier = Modifier
                            .size(44.dp)
                            .testTag("notification_bell_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = "Notifications",
                            tint = if (hasUnreadNotifications) TextPrimary else TextMuted
                        )
                    }
                    if (hasUnreadNotifications) {
                        Box(
                            modifier = Modifier
                                .padding(top = 8.dp, end = 8.dp)
                                .size(8.dp)
                                .background(BrandAccent, CircleShape)
                        )
                    }
                }

                // Avatar button (Alex Morgan)
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(CharcoalCard)
                        .border(1.5.dp, BorderSubtle, CircleShape)
                        .clickable { onAvatarClick() }
                        .testTag("profile_avatar_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "AM",
                        color = BrandAccent,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

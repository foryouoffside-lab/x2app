package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LeaderboardPlayer
import com.example.ui.theme.AmberAlert
import com.example.ui.theme.BorderActive
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.BrandAccent
import com.example.ui.theme.CharcoalCard
import com.example.ui.theme.CharcoalCardElevated
import com.example.ui.theme.CoolBlue
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.SportGreen
import com.example.ui.theme.TextInverse
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSubtle

@Composable
fun CompeteScreen(
    currentTab: String,
    onTabSelected: (String) -> Unit,
    players: List<LeaderboardPlayer>,
    onPlayToday: () -> Unit,
    onPlayerClick: (LeaderboardPlayer) -> Unit,
    onSeeRecords: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var joinedWaitlist by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp)
            .padding(top = 8.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 1. Page title + rating chip R 742
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Compete",
                color = TextPrimary,
                fontSize = 26.sp,
                fontWeight = FontWeight.Black
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(BrandAccent.copy(alpha = 0.15f))
                    .border(1.dp, BrandAccent.copy(alpha = 0.4f), RoundedCornerShape(50.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "R 742",
                    color = BrandAccent,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }

        // 2. Daily challenge hero
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(CharcoalCard)
                .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
                .padding(18.dp)
                .testTag("compete_daily_challenge_hero")
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.EmojiEvents, contentDescription = null, tint = AmberAlert, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "CHALLENGE",
                            color = AmberAlert,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Timer, contentDescription = null, tint = CoolBlue, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "08:42:16", color = CoolBlue, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .background(CharcoalCardElevated, RoundedCornerShape(12.dp))
                            .border(1.dp, BorderActive, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.GridOn, contentDescription = null, tint = BrandAccent, modifier = Modifier.size(24.dp))
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(text = "Flash Grid", color = TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Group, contentDescription = null, tint = TextMuted, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(text = "2.4k", color = TextMuted, fontSize = 12.sp)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Public, contentDescription = null, tint = TextMuted, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(text = "Global", color = TextMuted, fontSize = 12.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onPlayToday,
                    colors = ButtonDefaults.buttonColors(containerColor = BrandAccent, contentColor = TextInverse),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("play_today_button")
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Play", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // 3. Your position card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(CharcoalCardElevated)
                .border(1.dp, BrandAccent.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                .padding(16.dp)
                .testTag("your_position_card")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Public, contentDescription = null, tint = TextMuted, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "GLOBAL", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "#18,492", color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Black)
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .background(BrandAccent.copy(alpha = 0.15f), RoundedCornerShape(50.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(text = "Top 18%", color = BrandAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null, tint = SportGreen, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "+214", color = SportGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // 4. Leaderboard tabs (Global, Country, Friends with icons)
        val tabList = listOf(
            "Global" to Icons.Default.Public,
            "Country" to Icons.Default.Flag,
            "Friends" to Icons.Default.Group
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(CharcoalCard)
                .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            tabList.forEach { (tab, icon) ->
                val isSelected = currentTab == tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) BrandAccent else Color.Transparent)
                        .clickable { onTabSelected(tab) }
                        .padding(vertical = 8.dp)
                        .testTag("leaderboard_tab_${tab.lowercase()}"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isSelected) TextInverse else TextMuted,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = tab,
                            color = if (isSelected) TextInverse else TextMuted,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }

        // 5. Ranking list
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            players.forEach { player ->
                val isSelf = player.isCurrentUser
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelf) CharcoalCardElevated else CharcoalCard)
                        .border(1.dp, if (isSelf) BrandAccent.copy(alpha = 0.5f) else BorderSubtle, RoundedCornerShape(12.dp))
                        .clickable { onPlayerClick(player) }
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                        .testTag("player_row_${player.rank}")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "#${player.rank}",
                                color = if (player.rank <= 3) AmberAlert else TextMuted,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(42.dp)
                            )
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(CharcoalCard, CircleShape)
                                    .border(1.dp, if (isSelf) BrandAccent else BorderSubtle, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = player.avatarInitial,
                                    color = if (isSelf) BrandAccent else TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = player.name,
                                        color = if (isSelf) BrandAccent else TextPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = player.countryFlag, fontSize = 12.sp)
                                    if (player.isVerified) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Verified", tint = SportGreen, modifier = Modifier.size(13.dp))
                                    }
                                }
                                Text(text = player.handle, color = TextSubtle, fontSize = 11.sp)
                            }
                        }

                        Text(
                            text = player.scoreText,
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }

        // 6. Personal best strip
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(CharcoalCard)
                .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Bolt, contentDescription = null, tint = CoolBlue, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Classic", color = TextMuted, fontSize = 13.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "214 ms", color = BrandAccent, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
                IconButton(
                    onClick = onSeeRecords,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("see_records_button")
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "See records", tint = CoolBlue, modifier = Modifier.size(16.dp))
                }
            }
        }

        // 7. Coming soon: Duels with waitlist action
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(CharcoalCard.copy(alpha = 0.5f))
                .border(1.dp, BorderSubtle.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.FlashOn, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(text = "Duels", color = TextMuted, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text(text = "1v1 sync match", color = TextSubtle, fontSize = 11.sp)
                    }
                }
                OutlinedButton(
                    onClick = { joinedWaitlist = true },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = if (joinedWaitlist) SportGreen else TextPrimary),
                    border = BorderStroke(1.dp, BorderSubtle),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    if (joinedWaitlist) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = SportGreen, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Joined", fontSize = 11.sp, color = SportGreen)
                    } else {
                        Text(text = "Waitlist", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

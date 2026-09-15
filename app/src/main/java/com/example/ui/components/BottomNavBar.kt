package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.automirrored.outlined.ShowChart
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.AppTab
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.TextMuted
import com.example.ui.theme.VisionTeal

data class NavItem(
    val tab: AppTab,
    val label: String,
    val activeIcon: ImageVector,
    val inactiveIcon: ImageVector
)

@Composable
fun BottomNavBar(
    currentTab: AppTab,
    onTabSelected: (AppTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        NavItem(AppTab.HOME, "Home", Icons.Filled.Visibility, Icons.Outlined.Visibility),
        NavItem(AppTab.TRAIN, "Train", Icons.Filled.FitnessCenter, Icons.Outlined.FitnessCenter),
        NavItem(AppTab.COMPETE, "Compete", Icons.Filled.EmojiEvents, Icons.Outlined.EmojiEvents),
        NavItem(AppTab.PROGRESS, "Progress", Icons.AutoMirrored.Filled.ShowChart, Icons.AutoMirrored.Outlined.ShowChart),
        NavItem(AppTab.PROFILE, "Profile", Icons.Filled.Person, Icons.Outlined.Person)
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(DarkBackground)
            .navigationBarsPadding()
    ) {
        HorizontalDivider(color = BorderSubtle, thickness = 1.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val isSelected = currentTab == item.tab
                val color = if (isSelected) VisionTeal else TextMuted
                val icon = if (isSelected) item.activeIcon else item.inactiveIcon

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 8.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(bounded = true, color = VisionTeal)
                        ) { onTabSelected(item.tab) }
                        .testTag("nav_tab_${item.tab.name.lowercase()}")
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) VisionTeal.copy(alpha = 0.22f) else androidx.compose.ui.graphics.Color.Transparent)
                            .padding(horizontal = 18.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = item.label,
                            tint = color,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

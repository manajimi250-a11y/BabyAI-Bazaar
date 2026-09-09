package com.newlifetech.babyhey.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.newlifetech.babyhey.billing.PurchaseManager
import com.newlifetech.babyhey.data.UiStrings
import com.newlifetech.babyhey.data.UserPreferences
import com.newlifetech.babyhey.ui.components.SettingsIconButton
import com.newlifetech.babyhey.ui.components.UnlockDialog
import com.newlifetech.babyhey.ui.theme.BabyBlue
import com.newlifetech.babyhey.ui.theme.BabyGreen
import com.newlifetech.babyhey.ui.theme.BabyOrange
import com.newlifetech.babyhey.ui.theme.BabyPink
import com.newlifetech.babyhey.ui.theme.BabyPurple
import com.newlifetech.babyhey.ui.theme.BabyYellow
import kotlinx.coroutines.flow.first

@Composable
fun GamesMenuScreen(
    onBack: () -> Unit,
    onMemoryGameClick: () -> Unit,
    onOddOneOutClick: () -> Unit,
    onSortingGameClick: () -> Unit,
    onCountingGameClick: () -> Unit,
    onListenAndTapClick: () -> Unit,
    onSpeedTapClick: () -> Unit,
    onPuzzleClick: () -> Unit,
    onBalloonPopClick: () -> Unit,
    onSettingsClick: () -> Unit,
    purchaseManager: PurchaseManager
) {
    val context = LocalContext.current
    val prefs = remember { UserPreferences(context) }
    var language by remember { mutableStateOf("en") }
    var showUnlockDialog by remember { mutableStateOf(false) }
    val isUnlocked by purchaseManager.isUnlocked

    LaunchedEffect(Unit) {
        language = prefs.language.first()
    }

    fun handleClick(free: Boolean, action: () -> Unit) {
        if (free || isUnlocked) action() else showUnlockDialog = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = UiStrings.t("games_title", language),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            SettingsIconButton(language = language, onSettingsClick = onSettingsClick)
        }

        Spacer(Modifier.height(20.dp))

        // بازی «حافظه» همیشه رایگانه
        GameCard(
            emoji = "🧠",
            title = UiStrings.t("game_memory", language),
            color = BabyGreen,
            locked = false,
            onClick = { handleClick(free = true, action = onMemoryGameClick) }
        )
        Spacer(Modifier.height(14.dp))
        GameCard(
            emoji = "🔍",
            title = UiStrings.t("game_odd_one_out", language),
            color = BabyOrange,
            locked = !isUnlocked,
            onClick = { handleClick(free = false, action = onOddOneOutClick) }
        )
        Spacer(Modifier.height(14.dp))
        GameCard(
            emoji = "🗂️",
            title = UiStrings.t("game_sorting", language),
            color = BabyBlue,
            locked = !isUnlocked,
            onClick = { handleClick(free = false, action = onSortingGameClick) }
        )
        Spacer(Modifier.height(14.dp))
        GameCard(
            emoji = "🔢",
            title = UiStrings.t("game_counting", language),
            color = BabyPurple,
            locked = !isUnlocked,
            onClick = { handleClick(free = false, action = onCountingGameClick) }
        )
        Spacer(Modifier.height(14.dp))
        GameCard(
            emoji = "🎧",
            title = UiStrings.t("game_listen_tap", language),
            color = BabyPink,
            locked = !isUnlocked,
            onClick = { handleClick(free = false, action = onListenAndTapClick) }
        )
        Spacer(Modifier.height(14.dp))
        GameCard(
            emoji = "⚡",
            title = UiStrings.t("game_speed_tap", language),
            color = BabyYellow,
            locked = !isUnlocked,
            onClick = { handleClick(free = false, action = onSpeedTapClick) }
        )
        Spacer(Modifier.height(14.dp))
        GameCard(
            emoji = "🧩",
            title = UiStrings.t("game_puzzle", language),
            color = BabyGreen,
            locked = !isUnlocked,
            onClick = { handleClick(free = false, action = onPuzzleClick) }
        )
        Spacer(Modifier.height(14.dp))
        // بازی «بادکنک‌ها» همیشه رایگانه
        GameCard(
            emoji = "🎈",
            title = UiStrings.t("game_balloons", language),
            color = BabyOrange,
            locked = false,
            onClick = { handleClick(free = true, action = onBalloonPopClick) }
        )
    }

    if (showUnlockDialog) {
        UnlockDialog(
            language = language,
            onConfirm = {
                showUnlockDialog = false
                purchaseManager.purchaseFullUnlock { _, _ -> }
            },
            onDismiss = { showUnlockDialog = false }
        )
    }
}

@Composable
private fun GameCard(emoji: String, title: String, color: Color, locked: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = if (locked) Color.Gray else color),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(emoji, fontSize = 38.sp)
            Spacer(Modifier.width(16.dp))
            Text(title, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            if (locked) {
                Icon(Icons.Filled.Lock, contentDescription = "Locked", tint = Color.White)
            }
        }
    }
}

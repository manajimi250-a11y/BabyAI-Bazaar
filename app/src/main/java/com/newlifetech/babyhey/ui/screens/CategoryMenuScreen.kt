package com.newlifetech.babyhey.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
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
import com.newlifetech.babyhey.data.Category
import com.newlifetech.babyhey.data.UserPreferences
import com.newlifetech.babyhey.data.WordRepository
import com.newlifetech.babyhey.ui.components.MascotCompanion
import com.newlifetech.babyhey.ui.components.ParentalGateDialog
import com.newlifetech.babyhey.ui.components.UnlockDialog
import com.newlifetech.babyhey.ui.theme.*
import kotlinx.coroutines.flow.first

private fun colorForCategory(categoryId: String): Color = when (categoryId) {
    "animals" -> BabyOrange
    "colors" -> BabyBlue
    "shapes" -> BabyPurple
    "people" -> BabyPink
    else -> BabyGreen
}

/** فقط دسته‌ی «حیوانات» همیشه رایگانه؛ بقیه نیاز به خرید «باز کردن کامل محتوا» دارن. */
private val FREE_CATEGORY_ID = "animals"

@Composable
fun CategoryMenuScreen(
    onCategoryChosen: (String) -> Unit,
    onSettingsClick: () -> Unit,
    purchaseManager: PurchaseManager
) {
    val context = LocalContext.current
    val prefs = remember { UserPreferences(context) }
    var showGate by remember { mutableStateOf(false) }
    var language by remember { mutableStateOf("en") }
    var childName by remember { mutableStateOf("") }
    var totalStars by remember { mutableStateOf(0) }
    var showUnlockDialog by remember { mutableStateOf(false) }

    val isUnlocked by purchaseManager.isUnlocked

    LaunchedEffect(Unit) {
        language = prefs.language.first()
        childName = prefs.childName.first()
        totalStars = prefs.totalStars.first()
    }

    Box(modifier = Modifier.fillMaxSize()) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = buildString {
                    if (childName.isNotBlank()) {
                        append(if (language == "fa") "سلام $childName! " else "Hi $childName! ")
                    }
                    append(if (language == "fa") "چی یاد بگیریم؟" else "What shall we learn?")
                },
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(BabyYellow, RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text("⭐", fontSize = 18.sp)
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "$totalStars",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.width(4.dp))
            IconButton(onClick = {
                showGate = true
            }) {
                Icon(Icons.Filled.Settings, contentDescription = "Settings")
            }
        }

        Spacer(Modifier.height(36.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(WordRepository.allCategories) { category ->
                val locked = !isUnlocked && category.id != FREE_CATEGORY_ID
                CategoryCard(
                    category = category,
                    language = language,
                    locked = locked,
                    onClick = {
                        if (locked) showUnlockDialog = true else onCategoryChosen(category.id)
                    }
                )
            }
        }
    }

    MascotCompanion(
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(20.dp)
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

    if (showGate) {
        var gateEnabled by remember { mutableStateOf(true) }
        LaunchedEffect(Unit) {
            gateEnabled = prefs.parentalGateEnabled.first()
            if (!gateEnabled) {
                // اگه قفل خاموشه، مستقیم برو تنظیمات
                showGate = false
                onSettingsClick()
            }
        }
        if (gateEnabled) {
            ParentalGateDialog(
                language = language,
                onSuccess = {
                    showGate = false
                    onSettingsClick()
                },
                onDismiss = { showGate = false }
            )
        }
    }
}

@Composable
private fun CategoryCard(category: Category, language: String, locked: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable { onClick() },
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (locked) Color.Gray else colorForCategory(category.id)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = category.name(language),
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            if (locked) {
                Icon(
                    Icons.Filled.Lock,
                    contentDescription = "Locked",
                    tint = Color.White,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                )
            }
        }
    }
}

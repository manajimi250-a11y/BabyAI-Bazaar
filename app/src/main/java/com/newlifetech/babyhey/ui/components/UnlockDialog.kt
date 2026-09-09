package com.newlifetech.babyhey.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

/**
 * دیالوگی که وقتی کاربر رو یه محتوای قفل‌شده می‌زنه نشون داده می‌شه.
 * فقط فارسی/انگلیسی (چون این نسخه‌ی کافه‌بازار فقط همین دو زبون رو داره).
 */
@Composable
fun UnlockDialog(
    language: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val isFa = language == "fa"
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isFa) "این بخش قفل است 🔒" else "This is Locked 🔒") },
        text = {
            Text(
                if (isFa)
                    "با خرید یک‌باره‌ی «باز کردن کامل محتوا»، همه‌ی دسته‌ها، بازی‌ها، داستان‌ها و لالایی‌ها برای همیشه باز می‌شن."
                else
                    "With a one-time purchase to unlock full content, all categories, games, stories, and lullabies open forever."
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(if (isFa) "باز کردن" else "Unlock")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isFa) "بعداً" else "Later")
            }
        }
    )
}

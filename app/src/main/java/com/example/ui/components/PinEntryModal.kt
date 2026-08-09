package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.util.LocalAppStrings

@Composable
fun PinEntryModal(
    pinInput: String,
    pinError: Boolean,
    onDigitClick: (String) -> Unit,
    onDeleteClick: () -> Unit,
    onDismiss: () -> Unit,
    isBiometricAvailable: Boolean = false,
    onBiometricClick: (() -> Unit)? = null,
    title: String? = null,
    subtitle: String? = null
) {
    val strings = LocalAppStrings.current
    val actualTitle = title ?: strings.pinModalDefaultTitle
    val actualSubtitle = subtitle ?: strings.pinModalDefaultSubtitle

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("pin_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top close button & icon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("pin_dialog_close")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = strings.closeBtn
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = actualTitle,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = actualSubtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // PIN dots indicator
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 0 until 4) {
                        val isFilled = i < pinInput.length
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        pinError -> Color.Red
                                        isFilled -> MaterialTheme.colorScheme.primary
                                        else -> MaterialTheme.colorScheme.outlineVariant
                                    }
                                )
                        )
                    }
                }

                if (pinError) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = strings.pinIncorrectError,
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Biometric shortcut button if available
                if (isBiometricAvailable && onBiometricClick != null) {
                    OutlinedButton(
                        onClick = onBiometricClick,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("pin_biometric_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = strings.biometricButtonLabel,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = strings.biometricButtonLabel,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Numeric Keypad
                val keypad = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf(if (isBiometricAvailable) "BIO" else "", "0", "DEL")
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    for (row in keypad) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            for (item in row) {
                                if (item.isEmpty()) {
                                    Spacer(modifier = Modifier.size(64.dp))
                                } else if (item == "BIO") {
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
                                        modifier = Modifier
                                            .size(64.dp)
                                            .testTag("pin_key_bio")
                                            .clickable { onBiometricClick?.invoke() }
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Fingerprint,
                                                contentDescription = "Biométrie",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(32.dp)
                                            )
                                        }
                                    }
                                } else if (item == "DEL") {
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier
                                            .size(64.dp)
                                            .testTag("pin_key_del")
                                            .clickable { onDeleteClick() }
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.Backspace,
                                                contentDescription = "Effacer",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                } else {
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                        modifier = Modifier
                                            .size(64.dp)
                                            .testTag("pin_key_$item")
                                            .clickable { onDigitClick(item) }
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = item,
                                                style = MaterialTheme.typography.titleLarge.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 22.sp
                                                ),
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

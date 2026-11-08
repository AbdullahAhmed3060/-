package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.BorderDark
import com.example.ui.theme.CardSurfaceDark
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DangerRed
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.SecurityAmber

@Composable
fun PinInputDialog(
    title: String,
    subtitle: String = "أدخل رمز الحماية للدخول إلى الخزنة",
    isSetupMode: Boolean = false,
    errorMessage: String? = null,
    onPinSubmit: (String) -> Boolean,
    onDismiss: () -> Unit
) {
    var enteredPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var isConfirmStep by remember { mutableStateOf(false) }
    var localError by remember { mutableStateOf<String?>(errorMessage) }

    val currentInput = if (isConfirmStep) confirmPin else enteredPin
    val maxPinLength = 4

    fun handleDigit(d: String) {
        localError = null
        if (currentInput.length < maxPinLength) {
            val updated = currentInput + d
            if (isConfirmStep) {
                confirmPin = updated
                if (confirmPin.length == maxPinLength) {
                    if (confirmPin == enteredPin) {
                        onPinSubmit(confirmPin)
                    } else {
                        localError = "الرمزان غير متطابقين! أعد المحاولة"
                        confirmPin = ""
                    }
                }
            } else {
                enteredPin = updated
                if (enteredPin.length == maxPinLength) {
                    if (isSetupMode) {
                        isConfirmStep = true
                    } else {
                        val success = onPinSubmit(enteredPin)
                        if (!success) {
                            localError = "رمز PIN غير صحيح"
                            enteredPin = ""
                        }
                    }
                }
            }
        }
    }

    fun handleBackspace() {
        localError = null
        if (isConfirmStep) {
            if (confirmPin.isNotEmpty()) {
                confirmPin = confirmPin.dropLast(1)
            } else {
                isConfirmStep = false
            }
        } else {
            if (enteredPin.isNotEmpty()) {
                enteredPin = enteredPin.dropLast(1)
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("pin_dialog_card"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Close button & Shield Icon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(CyberCyan.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = CyberCyan,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "إلغاء",
                            tint = Color(0xFF94A3B8)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = if (isConfirmStep) "تأكيد الرمز السري" else title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ),
                    color = Color(0xFFF8FAFC),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (isConfirmStep) "أعد إدخال الرمز المكون من 4 أرقام للتأكيد" else subtitle,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                    color = Color(0xFF94A3B8),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                // PIN Dots Indicator
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 0 until maxPinLength) {
                        val isFilled = i < currentInput.length
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isFilled) CyberCyan else Color(0xFF334155)
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isFilled) CyberCyan else Color(0xFF475569),
                                    shape = CircleShape
                                )
                        )
                    }
                }

                // Error Message if any
                AnimatedVisibility(visible = localError != null) {
                    Text(
                        text = localError ?: "",
                        color = DangerRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Numpad (1-9, 0, Backspace)
                val padRows = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("C", "0", "DEL")
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    for (row in padRows) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            for (item in row) {
                                when (item) {
                                    "DEL" -> {
                                        Surface(
                                            modifier = Modifier
                                                .weight(1f)
                                                .aspectRatio(1.6f)
                                                .clip(RoundedCornerShape(14.dp))
                                                .clickable { handleBackspace() }
                                                .testTag("pin_btn_del"),
                                            shape = RoundedCornerShape(14.dp),
                                            color = Color(0xFF0F172A)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                                                    contentDescription = "حذف",
                                                    tint = Color(0xFF94A3B8),
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }
                                    "C" -> {
                                        Surface(
                                            modifier = Modifier
                                                .weight(1f)
                                                .aspectRatio(1.6f)
                                                .clip(RoundedCornerShape(14.dp))
                                                .clickable {
                                                    localError = null
                                                    if (isConfirmStep) confirmPin = "" else enteredPin = ""
                                                }
                                                .testTag("pin_btn_clear"),
                                            shape = RoundedCornerShape(14.dp),
                                            color = Color(0xFF0F172A)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = "مسح",
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = SecurityAmber
                                                )
                                            }
                                        }
                                    }
                                    else -> {
                                        Surface(
                                            modifier = Modifier
                                                .weight(1f)
                                                .aspectRatio(1.6f)
                                                .clip(RoundedCornerShape(14.dp))
                                                .clickable { handleDigit(item) }
                                                .testTag("pin_btn_$item"),
                                            shape = RoundedCornerShape(14.dp),
                                            color = Color(0xFF0F172A)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = item,
                                                    fontSize = 20.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFFF8FAFC)
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
}

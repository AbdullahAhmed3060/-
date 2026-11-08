package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.SecurityAmber

@Composable
fun CalculatorDisguiseView(
    expression: String,
    result: String,
    onKeyClick: (String) -> Unit,
    onDirectUnlockClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBlack)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Disguised Top Bar (looks like a normal utility calculator)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "آلة حاسبة",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFF64748B)
                    )
                )

                // Discreet vault trigger icon (small subtle lock button)
                IconButton(
                    onClick = onDirectUnlockClick,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .testTag("discreet_vault_trigger")
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "فتح الخزنة برمز PIN",
                        tint = Color(0xFF475569),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Display Screen (Expression & Result)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = if (expression.isEmpty()) "0" else expression,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = if (expression.length > 10) 36.sp else 48.sp,
                        fontWeight = FontWeight.Light,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFFF8FAFC)
                    ),
                    maxLines = 2,
                    textAlign = TextAlign.End,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("calc_expression_display")
                )

                if (result.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "= $result",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = NeonEmerald
                        ),
                        textAlign = TextAlign.End,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("calc_result_display")
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Calculator Keypad
            val buttons = listOf(
                listOf("C", "DEL", "%", "÷"),
                listOf("7", "8", "9", "×"),
                listOf("4", "5", "6", "-"),
                listOf("1", "2", "3", "+"),
                listOf("0", ".", "=", "")
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                for (row in buttons) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        for (key in row) {
                            if (key.isEmpty()) {
                                Spacer(modifier = Modifier.weight(1f))
                            } else {
                                val isOperator = key in listOf("÷", "×", "-", "+", "=")
                                val isAction = key in listOf("C", "DEL", "%")
                                val isEquals = key == "="

                                val bgColor = when {
                                    isEquals -> CyberCyan
                                    isOperator -> Color(0xFF1E293B)
                                    isAction -> Color(0xFF334155)
                                    else -> Color(0xFF0F172A)
                                }

                                val textColor = when {
                                    isEquals -> Color(0xFF00363F)
                                    isOperator -> CyberCyan
                                    isAction -> SecurityAmber
                                    else -> Color(0xFFF8FAFC)
                                }

                                val weight = if (key == "0" && row[3].isEmpty()) 1f else 1f

                                Surface(
                                    modifier = Modifier
                                        .weight(weight)
                                        .aspectRatio(1.15f)
                                        .clip(RoundedCornerShape(20.dp))
                                        .clickable { onKeyClick(key) }
                                        .testTag("calc_key_$key"),
                                    shape = RoundedCornerShape(20.dp),
                                    color = bgColor
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = key,
                                            fontSize = if (key == "DEL") 18.sp else 24.sp,
                                            fontWeight = if (isOperator || isEquals) FontWeight.Bold else FontWeight.Medium,
                                            color = textColor
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

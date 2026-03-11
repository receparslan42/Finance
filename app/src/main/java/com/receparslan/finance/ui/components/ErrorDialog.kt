package com.receparslan.finance.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.receparslan.finance.R

// This composable function displays an error dialog with a customizable message, a dismiss button, and an optional retry button.
@Composable
fun ErrorDialog(
    message: String = "An error occurred while fetching data. Please try again.",
    onDismiss: () -> Unit,
    onRetry: (() -> Unit)? = null
) {
    Dialog(
        onDismissRequest = onDismiss,
    ) {
        Box(
            modifier = Modifier
                .shadow(
                    elevation = 3.dp,
                    spotColor = Color.White,
                    ambientColor = Color.White,
                    shape = RoundedCornerShape(size = 16.dp)
                )
                .windowInsetsPadding(WindowInsets(1.dp, 1.dp, 1.dp, 1.dp))
                .background(MaterialTheme.colorScheme.background, shape = RoundedCornerShape(16.dp))
                .windowInsetsPadding(WindowInsets(8.dp, 8.dp, 8.dp, 8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.error_icon),
                        contentDescription = "Error Icon",
                        tint = MaterialTheme.colorScheme.surfaceTint,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "ERROR",
                        style = TextStyle(
                            fontFamily = FontFamily(Font(R.font.poppins)),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        ),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.error_icon),
                        contentDescription = "Error Icon",
                        tint = MaterialTheme.colorScheme.surfaceTint,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Text(
                    text = message,
                    style = TextStyle(
                        fontFamily = FontFamily(Font(R.font.poppins)),
                        fontSize = 18.sp,
                        color = Color.White
                    ),
                    textAlign = TextAlign.Center
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Dismiss",
                        modifier = Modifier
                            .clickable { onDismiss() }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .background(
                                MaterialTheme.colorScheme.surfaceTint,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .windowInsetsPadding(WindowInsets(16.dp, 8.dp, 16.dp, 8.dp)),
                        style = TextStyle(
                            fontFamily = FontFamily(Font(R.font.poppins)),
                            fontSize = 18.sp,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    )

                    if (onRetry != null)
                        Text(
                            text = "Retry",
                            modifier = Modifier
                                .clickable { onRetry() }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .background(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .windowInsetsPadding(WindowInsets(29.dp, 8.dp, 29.dp, 8.dp)),
                            style = TextStyle(
                                fontFamily = FontFamily(Font(R.font.poppins)),
                                fontSize = 18.sp,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                }
            }
        }
    }
}
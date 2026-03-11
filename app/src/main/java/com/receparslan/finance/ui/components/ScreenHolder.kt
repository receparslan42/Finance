package com.receparslan.finance.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.receparslan.finance.R

// This function is used to display a centered message, typically for loading or error states, with a default message of "Loading..."
@Composable
fun ScreenHolder(
    modifier: Modifier = Modifier,
    message: String = "Loading..."
) {
    Box(
        modifier = modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = TextStyle(
                fontFamily = FontFamily(Font(R.font.poppins)),
                fontSize = 20.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
            ),
            textAlign = TextAlign.Center
        )
    }
}
package com.receparslan.finance.feature.home.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.receparslan.finance.R

// This function is used to display the header text for the Home Screen
@Composable
fun LeftHeaderText(
    modifier: Modifier = Modifier
) {
    Text(
        text = "Trending Coins",
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 20.dp, top = 7.dp),
        style = TextStyle(
            shadow = Shadow(
                color = Color.White,
                offset = Offset(0f, 2f),
                blurRadius = 3f
            ),
            fontFamily = FontFamily(Font(R.font.poppins)),
            fontSize = 20.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
        )
    )
}
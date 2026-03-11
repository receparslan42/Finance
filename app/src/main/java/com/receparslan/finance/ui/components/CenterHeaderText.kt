package com.receparslan.finance.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.receparslan.finance.R

// This composable function displays a centered header text with optional icons on both sides based on the provided text.
@Composable
fun CenterHeaderText(
    text: String,
    modifier: Modifier = Modifier
) {
    // Determine the icon based on whether it's for "Top Gainers" or "Top Losers"
    val icon: Int? =
        if (text == "Top Gainers") R.drawable.up_icon else if (text == "Top Losers") R.drawable.down_icon else null

    // Display the header text with the optional icons on both sides
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null)
            Icon(
                modifier = Modifier.size(28.dp),
                imageVector = ImageVector.vectorResource(icon),
                contentDescription = "Price Change",
                tint = if (text == "Top Gainers") Color.Green else Color.Red
            )

        Text(
            text = text,
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
            ),
            textAlign = TextAlign.Center
        )

        if (icon != null)
            Icon(
                modifier = Modifier.size(28.dp),
                imageVector = ImageVector.vectorResource(icon),
                contentDescription = "Price Change",
                tint = if (text == "Top Gainers") Color.Green else Color.Red
            )
    }
}
package com.receparslan.finance.feature.detail.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.receparslan.finance.R

// This function creates a text field for the user to input the amount of cryptocurrency they want to calculate with
@Composable
fun AmountTextField(
    onAmountChange: (Double) -> Unit
) {
    var input by remember { mutableStateOf("") }

    val focusManager = LocalFocusManager.current

    BasicTextField(
        value = input,
        textStyle = TextStyle(
            fontSize = 16.sp,
            fontFamily = FontFamily(Font(R.font.poppins)),
            fontWeight = FontWeight(450),
            color = Color.Gray
        ),
        onValueChange = { newValue ->
            input = newValue

            val parsed = input.toDoubleOrNull() ?: 0.0
            onAmountChange(parsed)
        },
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = KeyboardType.Number
        ),
        keyboardActions = KeyboardActions(
            onDone = { focusManager.clearFocus() }
        ),
        modifier = Modifier
            .widthIn(75.dp, 75.dp)
            .border(
                1.dp,
                Color.Gray,
                RoundedCornerShape(15.dp)
            )
            .background(
                color = MaterialTheme.colorScheme.background,
                shape = RoundedCornerShape(15.dp)
            )
            .windowInsetsPadding(
                WindowInsets(
                    8.dp,
                    4.dp,
                    4.dp,
                    4.dp
                )
            ),
        cursorBrush = SolidColor(Color.Gray),
        singleLine = true,
    ) {
        // Placeholder
        if (input.isEmpty()) {
            Text(
                text = "00.00",
                style = TextStyle(
                    fontSize = 16.sp,
                    fontFamily = FontFamily(Font(R.font.poppins)),
                    fontWeight = FontWeight(450),
                    color = Color.Gray
                )
            )
            it()
        } else
            it()
    }
}
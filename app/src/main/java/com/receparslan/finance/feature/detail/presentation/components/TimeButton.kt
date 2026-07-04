package com.receparslan.finance.feature.detail.presentation.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.receparslan.finance.R
import com.receparslan.finance.feature.detail.presentation.state.DetailUIState
import com.receparslan.finance.feature.detail.presentation.viewmodel.DetailViewModel

// This function creates a button for selecting the time period
@Composable
fun TimeButton(
    time: String,
    modifier: Modifier,
    viewModel: DetailViewModel,
    state: DetailUIState
) {
    val isSelected = time == state.timePeriod

    Button(
        enabled = !isSelected,
        onClick = { viewModel.updateTimePeriod(time) },
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
            disabledContainerColor = MaterialTheme.colorScheme.secondary,
            containerColor = MaterialTheme.colorScheme.background
        ),
        contentPadding = PaddingValues(0.dp),
        modifier = modifier
            .border(width = 0.5.dp, color = Color.Gray, shape = RoundedCornerShape(size = 20.dp))
            .size(width = 60.dp, height = 40.dp)
    ) {
        Text(
            text = time,
            fontSize = 12.sp,
            fontFamily = FontFamily(Font(R.font.poppins)),
            fontWeight = FontWeight(450),
            color = Color.White
        )
    }
}
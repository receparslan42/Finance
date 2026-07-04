package com.receparslan.finance.feature.detail.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.receparslan.finance.R
import com.receparslan.finance.core.domain.model.Cryptocurrency
import com.receparslan.finance.feature.detail.presentation.viewmodel.DetailViewModel
import kotlin.math.absoluteValue

// This function creates a row for cryptocurrency calculations based on user input
@Composable
fun CryptocurrencyCalculationRow(
    cryptocurrency: Cryptocurrency,
    modifier: Modifier = Modifier,
    viewModel: DetailViewModel
) {
    var amount by remember { mutableDoubleStateOf(0.0) }

    val prevPrice =
        cryptocurrency.currentPrice / (1 + (cryptocurrency.priceChangePercentage24h / 100))

    val priceChange = cryptocurrency.currentPrice - prevPrice

    val totalValue by remember(amount, cryptocurrency.currentPrice) {
        derivedStateOf {
            "$${viewModel.decimalFormatter("#,###.#####", cryptocurrency.currentPrice * amount)}"
        }
    }

    val totalPriceChange by remember(amount, priceChange) {
        derivedStateOf {
            (if (priceChange * amount > 0) "+" else if (priceChange * amount < 0) "-" else "") +
                    viewModel.decimalFormatter("#,###.###", priceChange.absoluteValue * amount)
        }
    }

    Row(
        modifier = modifier
            .padding(16.dp)
            .background(
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(size = 15.dp)
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Display the cryptocurrency logo
        Image(
            painter = rememberAsyncImagePainter(cryptocurrency.image),
            contentDescription = cryptocurrency.name,
            modifier = Modifier
                .padding(start = 8.dp)
                .size(48.dp)
                .clip(CircleShape)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Column for the name and row for amount input and symbol
            Column {
                Text(
                    text = cryptocurrency.name,
                    fontSize = 20.sp,
                    fontFamily = FontFamily(Font(R.font.poppins)),
                    fontWeight = FontWeight(450),
                    color = Color.White
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    // Input field for the amount
                    AmountTextField(onAmountChange = { amount = it })

                    // Display the cryptocurrency symbol
                    Text(
                        text = cryptocurrency.symbol.uppercase(),
                        fontSize = 16.sp,
                        fontFamily = FontFamily(Font(R.font.poppins)),
                        fontWeight = FontWeight(450),
                        color = MaterialTheme.colorScheme.tertiary,
                        maxLines = 1,
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {
                // Display the total value based on the input amount
                Text(
                    text = totalValue,
                    fontSize = 16.sp,
                    fontFamily = FontFamily(Font(R.font.poppins)),
                    fontWeight = FontWeight(450),
                    color = Color.White
                )

                // Display the total price change based on the input amount
                Text(
                    text = totalPriceChange,
                    fontSize = 16.sp,
                    fontFamily = FontFamily(Font(R.font.poppins)),
                    fontWeight = FontWeight(450),
                    color = if (priceChange * amount > 0) Color(android.graphics.Color.GREEN) else if (priceChange * amount < 0) Color(
                        android.graphics.Color.RED
                    ) else Color.Gray,
                )
            }
        }
    }
}
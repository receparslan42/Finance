package com.receparslan.finance.feature.detail.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.receparslan.finance.R
import com.receparslan.finance.core.domain.model.Cryptocurrency
import com.receparslan.finance.feature.detail.presentation.viewmodel.DetailViewModel
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.absoluteValue

// This function creates a row displaying cryptocurrency information
@Composable
fun CryptocurrencyInfoRow(
    cryptocurrency: Cryptocurrency,
    modifier: Modifier = Modifier,
    viewModel: DetailViewModel
) {
    val prevPrice =
        cryptocurrency.currentPrice / (1 + (cryptocurrency.priceChangePercentage24h / 100))
    val priceChange = cryptocurrency.currentPrice - prevPrice

    val currentPriceFormatted = "$${
        viewModel.decimalFormatter(
            "#,###.################",
            cryptocurrency.currentPrice
        )
    }"

    val priceChangeFormatted = (if (priceChange >= 0) "+" else "-") +
            viewModel.decimalFormatter("#,###.###", priceChange.absoluteValue)

    val priceChangePercentageFormatted = ("( " + if (priceChange > 0) "+" else "-") + "${
        viewModel.decimalFormatter(
            "#,###.###",
            cryptocurrency.priceChangePercentage24h.absoluteValue
        )
    }% )"

    val lastUpdatedTimeFormatted =
        DateTimeFormatter.ofPattern("dd.MM.yyyy\n      HH:mm", LocalLocale.current.platformLocale)
            .format(
                ZonedDateTime.parse(cryptocurrency.lastUpdated)
                    .withZoneSameInstant(ZoneId.systemDefault())
            )

    val priceChangeColor = if (priceChange > 0) Color.Green else Color.Red

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            // Display the current price
            Text(
                text = currentPriceFormatted,
                fontSize = 28.sp,
                fontFamily = FontFamily(Font(R.font.poppins)),
                fontWeight = FontWeight(500),
                color = Color.White
            )

            // Display the price change
            Text(
                text = priceChangeFormatted,
                fontSize = 24.sp,
                fontFamily = FontFamily(Font(R.font.poppins)),
                fontWeight = FontWeight(450),
                color = priceChangeColor,
                modifier = Modifier.padding(top = 8.dp)
            )

            // Display the price change percentage
            Text(
                text = priceChangePercentageFormatted,
                fontSize = 24.sp,
                fontFamily = FontFamily(Font(R.font.poppins)),
                fontWeight = FontWeight(450),
                color = priceChangeColor,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // Display the last updated time of the cryptocurrency
        Text(
            text = lastUpdatedTimeFormatted,
            fontSize = 20.sp,
            fontFamily = FontFamily(Font(R.font.poppins)),
            fontWeight = FontWeight(500),
            color = Color.Gray,
        )
    }
}
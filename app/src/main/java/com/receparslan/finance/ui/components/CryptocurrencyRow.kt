package com.receparslan.finance.ui.components

import android.icu.text.DecimalFormat
import android.icu.text.DecimalFormatSymbols
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.receparslan.finance.R
import com.receparslan.finance.model.Cryptocurrency
import com.receparslan.finance.ui.Screen
import java.util.Locale

// This function is used to display each cryptocurrency (name, symbol, price, and price change percentage) in a row format.
@Composable
fun CryptocurrencyRow(
    cryptocurrency: Cryptocurrency,
    navController: NavController
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .shadow(
                elevation = 3.dp,
                spotColor = Color.White,
                ambientColor = Color.White,
                shape = RoundedCornerShape(size = 15.dp)
            )
            .background(
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(size = 15.dp)
            )
            .clickable {
                val encodedId = Uri.encode(cryptocurrency.id)
                navController.navigate(Screen.Detail.createRoute(encodedId))
            },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Display the cryptocurrency icon
            Image(
                painter = rememberAsyncImagePainter(cryptocurrency.image),
                contentDescription = cryptocurrency.name,
                modifier = Modifier
                    .padding(16.dp, 16.dp, 12.dp, 16.dp)
                    .size(40.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.FillBounds
            )

            // Display the cryptocurrency name and symbol
            Column {
                Text(
                    text = cryptocurrency.name,
                    style = TextStyle(
                        fontSize = if (cryptocurrency.name.length > 40) 10.sp else if (cryptocurrency.name.length > 30) 12.sp else if (cryptocurrency.name.length > 20) 14.sp else 16.sp,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily(Font(R.font.poppins)),
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.size(3.dp))

                Text(
                    text = cryptocurrency.symbol.uppercase(),
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.tertiary,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily(Font(R.font.poppins)),
                    )
                )
            }
        }

        // Display the cryptocurrency price and price change percentage
        Column {
            Text(
                text = "$" +
                        DecimalFormat(
                            "#,###.####", DecimalFormatSymbols(Locale.US)
                        ).format(
                            cryptocurrency.currentPrice
                        ),
                style = TextStyle(
                    fontSize = 16.sp,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily(Font(R.font.poppins)),
                ),
                modifier = Modifier
                    .padding(end = 18.dp)
                    .align(Alignment.End)
            )

            Spacer(Modifier.size(5.dp))

            Text(
                text = cryptocurrency.priceChangePercentage24h.toString() + "%",
                style = TextStyle(
                    fontSize = 12.sp,
                    color = if ((cryptocurrency.priceChangePercentage24h) > 0) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceTint,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily(Font(R.font.poppins)),
                ),
                modifier = Modifier
                    .padding(end = 16.dp)
                    .align(Alignment.End)
            )
        }
    }
}
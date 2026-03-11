package com.receparslan.finance.ui.components

import android.icu.text.DecimalFormat
import android.icu.text.DecimalFormatSymbols
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
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
import kotlin.math.absoluteValue

// This function is used to display a grid item for a cryptocurrency for the Gainer and Loser screens
@Composable
fun GridItem(
    cryptocurrency: Cryptocurrency,
    navController: NavController,
    arrow: @Composable () -> Unit = {}
) {
    val cryptocurrencyPrice by remember {
        derivedStateOf {
            "$" + DecimalFormat("#,###.####", DecimalFormatSymbols(Locale.US)).format(
                cryptocurrency.currentPrice
            )
        }
    }

    val priceChangePercentage by remember {
        derivedStateOf {
            cryptocurrency.priceChangePercentage24h.absoluteValue.toString() + "%"
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
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
                val encodedCryptoID = Uri.encode(cryptocurrency.id)

                navController.navigate(Screen.Detail.createRoute(encodedCryptoID))
            },
    ) {
        Column {
            // Display the cryptocurrency icon
            Image(
                painter = rememberAsyncImagePainter(cryptocurrency.image),
                contentDescription = cryptocurrency.name,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier
                    .size(75.dp)
                    .clip(CircleShape)
                    .align(Alignment.CenterHorizontally)
            )

            // Display the cryptocurrency name
            Text(
                text = cryptocurrency.name,
                modifier = Modifier.align(Alignment.CenterHorizontally),
                style = TextStyle(
                    color = MaterialTheme.colorScheme.tertiary,
                    fontSize = 14.sp,
                    fontFamily = FontFamily(Font(R.font.poppins)),
                    fontWeight = FontWeight(500),
                    shadow = Shadow(
                        color = Color.Black,
                        offset = Offset(0f, 1f),
                        blurRadius = 2f
                    )
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Display the cryptocurrency price
            Text(
                text = cryptocurrencyPrice,
                modifier = Modifier.align(Alignment.CenterHorizontally),
                style = TextStyle(
                    color = Color.White,
                    fontSize = 14.sp,
                    fontFamily = FontFamily(Font(R.font.poppins)),
                    fontWeight = FontWeight(600),
                    shadow = Shadow(
                        color = Color.Black,
                        offset = Offset(0f, 1f),
                        blurRadius = 2f
                    )
                ),
                maxLines = 1,
            )

            // Display the price change percentage
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .border(0.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.background, RoundedCornerShape(16.dp))
                    .wrapContentSize()
                    .align(Alignment.CenterHorizontally)
                    .padding(4.dp)
            ) {
                arrow()

                Text(
                    text = priceChangePercentage,
                    style = TextStyle(
                        color = MaterialTheme.colorScheme.tertiary,
                        fontSize = 14.sp,
                        fontFamily = FontFamily(Font(R.font.poppins)),
                        fontWeight = FontWeight(500),
                        shadow = Shadow(
                            color = Color.Black,
                            offset = Offset(0f, 1f),
                            blurRadius = 2f
                        )
                    )
                )
            }
        }
    }
}
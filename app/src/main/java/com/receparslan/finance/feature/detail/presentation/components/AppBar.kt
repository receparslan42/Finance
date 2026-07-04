package com.receparslan.finance.feature.detail.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.receparslan.finance.R
import com.receparslan.finance.feature.detail.presentation.state.DetailUIState
import com.receparslan.finance.feature.detail.presentation.viewmodel.DetailViewModel

// This function creates the app bar for the DetailScreen
@Composable
fun AppBar(
    state: DetailUIState,
    navController: NavController,
    viewModel: DetailViewModel
) {
    if (state.cryptocurrency == null) return

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(Color.Transparent),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Display the back button
        Icon(
            imageVector = ImageVector.vectorResource(id = R.drawable.back_icon),
            contentDescription = "Go back",
            modifier = Modifier
                .padding(start = 18.dp)
                .size(28.dp)
                .clickable {
                    navController.popBackStack()
                },
            tint = Color.White
        )

        // Display the cryptocurrency logo
        Image(
            painter = rememberAsyncImagePainter(state.cryptocurrency.image),
            contentDescription = state.cryptocurrency.name,
            modifier = Modifier
                .padding(start = 8.dp)
                .size(48.dp)
                .clip(CircleShape)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(start = 8.dp, end = 16.dp)
                .weight(1f)
        ) {
            // Row for the name and symbol
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                Text(
                    text = state.cryptocurrency.name,
                    modifier = if (state.cryptocurrency.name.length > 10) Modifier.weight(1f) else Modifier,
                    fontSize = 24.sp,
                    fontFamily = FontFamily(Font(R.font.poppins)),
                    fontWeight = FontWeight(450),
                    color = Color.White,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "(${state.cryptocurrency.symbol.uppercase()})",
                    modifier = Modifier
                        .padding(start = 4.dp),
                    fontSize = 16.sp,
                    fontFamily = FontFamily(Font(R.font.poppins)),
                    fontWeight = FontWeight(450),
                    color = MaterialTheme.colorScheme.tertiary,
                    maxLines = 1,
                )
            }

            // Display the save icon, which changes based on whether the cryptocurrency is saved or not
            Icon(
                imageVector = ImageVector.vectorResource(id = if (state.isSaved) R.drawable.star_filled_icon else R.drawable.star_icon),
                contentDescription = "Add to favorites",
                modifier = Modifier
                    .size(32.dp)
                    .clickable {
                        if (state.isSaved) viewModel.deleteCryptocurrency() else viewModel.saveCryptocurrency()
                    },
                tint = Color.White
            )
        }
    }
}
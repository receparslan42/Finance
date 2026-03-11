package com.receparslan.finance.ui.charts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.compose.cartesian.CartesianMeasuringContext
import com.patrykandpatrick.vico.compose.cartesian.Scroll
import com.patrykandpatrick.vico.compose.cartesian.Zoom
import com.patrykandpatrick.vico.compose.cartesian.axis.Axis
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.component.TextComponent
import com.patrykandpatrick.vico.compose.common.data.ExtraStore
import com.receparslan.finance.ui.markers.rememberMarker
import com.receparslan.finance.util.Constants.ExtraKeys
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.roundToLong

// This variable defines the date format for the X-axis labels in the chart.
private val BottomAxisValueFormatter =
    object : CartesianValueFormatter {
        private val dateFormat = SimpleDateFormat("      HH:mm\n   dd.MM.yyyy", Locale.US)

        override fun format(
            context: CartesianMeasuringContext,
            value: Double,
            verticalAxisPosition: Axis.Position.Vertical?,
        ) = dateFormat.format(Date(value.toLong()))
    }

// This variable defines the range provider for the Y-axis in the chart.
// Uses a dynamic step based on the actual data range to handle all price levels correctly.
private val RangeProvider =
    object : CartesianLayerRangeProvider {
        private fun calculateStep(minY: Double, maxY: Double): Double {
            val range = maxY - minY
            if (range <= 0.0) return 1.0
            // Calculate a step that divides the range into ~10 segments
            val rawStep = range / 10.0
            val magnitude = 10.0.pow(floor(log10(rawStep)))
            return magnitude * ceil(rawStep / magnitude)
        }

        override fun getMinY(minY: Double, maxY: Double, extraStore: ExtraStore): Double {
            val step = calculateStep(minY, maxY)
            return step * floor(minY / step)
        }

        override fun getMaxY(minY: Double, maxY: Double, extraStore: ExtraStore): Double {
            val step = calculateStep(minY, maxY)
            return step * ceil(maxY / step)
        }
    }

val MarkerValueFormatter = object : DefaultCartesianMarker.ValueFormatter {
    override fun format(
        context: CartesianDrawingContext,
        targets: List<CartesianMarker.Target>
    ): CharSequence {
        val target = targets.firstOrNull() ?: return ""
        val targetTime = target.x.roundToLong()
        val dataMap = context.model.extraStore.getOrNull(ExtraKeys.klineDataMap) ?: emptyMap()
        val closestTime = dataMap.keys.minByOrNull { abs(it - targetTime) } ?: return ""
        val previousTime = dataMap.keys.filter { it < closestTime }.maxOrNull()
        val prevItem = dataMap[previousTime] ?: return ""
        val item = dataMap[closestTime] ?: return ""

        val color = if (prevItem.close < item.close) Color.Green
        else if (prevItem.close > item.close) Color.Red
        else Color.White

        val formatter = DecimalFormat(
            "#,###.###",
            DecimalFormatSymbols(Locale.US)
        )

        return buildAnnotatedString {
            withStyle(
                SpanStyle(
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            ) { append("Open :") }
            withStyle(SpanStyle(color = color)) { append(" $${formatter.format(item.open.toDouble())}\n") }

            withStyle(
                SpanStyle(
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            ) { append("High :") }
            withStyle(SpanStyle(color = color)) { append(" $${formatter.format(item.high.toDouble())}\n") }

            withStyle(
                SpanStyle(
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            ) { append("Low  :") }
            withStyle(SpanStyle(color = color)) { append(" $${formatter.format(item.low.toDouble())}\n") }

            withStyle(
                SpanStyle(
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            ) { append("Close:") }
            withStyle(SpanStyle(color = color)) { append(" $${formatter.format(item.close.toDouble())}") }
        }
    }
}

@Composable
fun LineChart(
    modelProducer: CartesianChartModelProducer,
    modifier: Modifier,
    lineColor: Brush,
) {
    CartesianChartHost(
        rememberCartesianChart(
            rememberLineCartesianLayer(
                lineProvider =
                    LineCartesianLayer.LineProvider.series(
                        LineCartesianLayer.rememberLine(
                            fill = LineCartesianLayer.LineFill.single(
                                Fill(lineColor)
                            ),
                            areaFill =
                                LineCartesianLayer.AreaFill.single(
                                    Fill(
                                        Brush.verticalGradient(
                                            listOf(
                                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f),
                                                Color.Transparent
                                            )
                                        )
                                    )
                                ),
                        )
                    ),
                rangeProvider = RangeProvider,
            ),
            bottomAxis = HorizontalAxis.rememberBottom(
                valueFormatter = BottomAxisValueFormatter,
                label = TextComponent(
                    textStyle = TextStyle.Default.copy(color = Color.Gray),
                    lineCount = 2
                ),
                guideline = null
            ),
            marker = rememberMarker(MarkerValueFormatter),
        ),
        zoomState = rememberVicoZoomState(
            initialZoom = Zoom.fixed(0.095f)
        ),
        placeholder = {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(64.dp)
                    .align(Alignment.Center)
                    .background(MaterialTheme.colorScheme.background),
                color = MaterialTheme.colorScheme.secondary
            )
        },
        modelProducer = modelProducer,
        modifier = modifier.height(250.dp),
        scrollState = rememberVicoScrollState(initialScroll = Scroll.Absolute.End)
    )
}
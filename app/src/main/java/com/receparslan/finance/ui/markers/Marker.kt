package com.receparslan.finance.ui.markers

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisGuidelineComponent
import com.patrykandpatrick.vico.compose.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.marker.rememberDefaultCartesianMarker
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.Insets
import com.patrykandpatrick.vico.compose.common.LayeredComponent
import com.patrykandpatrick.vico.compose.common.MarkerCornerBasedShape
import com.patrykandpatrick.vico.compose.common.component.ShapeComponent
import com.patrykandpatrick.vico.compose.common.component.TextComponent
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent

@Composable
internal fun rememberMarker(
    valueFormatter: DefaultCartesianMarker.ValueFormatter = DefaultCartesianMarker.ValueFormatter.default(),
    showIndicator: Boolean = true,
): CartesianMarker {
    val labelBackground =
        rememberShapeComponent(
            fill = Fill(MaterialTheme.colorScheme.background),
            shape = MarkerCornerBasedShape(RoundedCornerShape(50.dp), 10.dp),
            strokeThickness = 1.dp,
            strokeFill = Fill(MaterialTheme.colorScheme.outline),
        )
    val label =
        rememberTextComponent(
            style = TextStyle.Default.copy(
                textAlign = TextAlign.Start
            ),
            padding = Insets(24.dp, 4.dp),
            background = labelBackground,
            minWidth = TextComponent.MinWidth.fixed(40.dp),
            lineCount = 4
        )
    val indicatorFrontComponent =
        rememberShapeComponent(
            Fill(MaterialTheme.colorScheme.surface),
            MarkerCornerBasedShape(RoundedCornerShape(percent = 50))
        )
    val guideline = rememberAxisGuidelineComponent()
    return rememberDefaultCartesianMarker(
        label = label,
        valueFormatter = valueFormatter,
        indicator =
            if (showIndicator) {
                { color ->
                    LayeredComponent(
                        back = ShapeComponent(
                            Fill(color.copy(alpha = 0.15f)),
                            MarkerCornerBasedShape(RoundedCornerShape(percent = 50))
                        ),
                        front =
                            LayeredComponent(
                                back = ShapeComponent(
                                    fill = Fill(color),
                                    shape = MarkerCornerBasedShape(RoundedCornerShape(percent = 50))
                                ),
                                front = indicatorFrontComponent,
                                padding = Insets(5.dp),
                            ),
                        padding = Insets(10.dp),
                    )
                }
            } else {
                null
            },
        labelPosition = DefaultCartesianMarker.LabelPosition.AroundPoint,
        indicatorSize = 36.dp,
        guideline = guideline,
    )
}
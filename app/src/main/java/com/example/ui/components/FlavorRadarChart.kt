package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FlavorAxes
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun FlavorRadarChart(
    primaryAxes: FlavorAxes,
    primaryLabel: String,
    modifier: Modifier = Modifier,
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    secondaryAxes: FlavorAxes? = null,
    secondaryLabel: String? = null,
    secondaryColor: Color = MaterialTheme.colorScheme.tertiary,
) {
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val spokeColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = TextStyle(
        color = labelColor,
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (secondaryAxes != null && secondaryLabel != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadarLegendPill(label = primaryLabel, color = primaryColor)
                RadarLegendPill(label = secondaryLabel, color = secondaryColor)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.12f)
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val axes = primaryAxes.asLabeledList()
                val count = axes.size
                val center = Offset(size.width / 2f, size.height / 2f)
                val maxRadius = minOf(size.width, size.height) * 0.34f
                val angleStep = (2.0 * PI) / count

                // Draw 4 concentric octagonal guide rings
                for (ring in 1..4) {
                    val r = maxRadius * (ring / 4f)
                    val ringPath = Path()
                    for (i in 0 until count) {
                        val angle = -PI / 2.0 + i * angleStep
                        val pt = Offset(
                            x = center.x + (r * cos(angle)).toFloat(),
                            y = center.y + (r * sin(angle)).toFloat(),
                        )
                        if (i == 0) ringPath.moveTo(pt.x, pt.y) else ringPath.lineTo(pt.x, pt.y)
                    }
                    ringPath.close()
                    drawPath(
                        path = ringPath,
                        color = gridColor.copy(alpha = if (ring == 4) 0.9f else 0.55f),
                        style = Stroke(width = if (ring == 4) 1.5.dp.toPx() else 1.dp.toPx()),
                    )
                }

                // Draw axis spokes & labels
                for (i in 0 until count) {
                    val angle = -PI / 2.0 + i * angleStep
                    val outer = Offset(
                        x = center.x + (maxRadius * cos(angle)).toFloat(),
                        y = center.y + (maxRadius * sin(angle)).toFloat(),
                    )
                    drawLine(
                        color = spokeColor,
                        start = center,
                        end = outer,
                        strokeWidth = 1.dp.toPx(),
                    )

                    val labelResult = textMeasurer.measure(axes[i].first, style = labelStyle)
                    val labelRadius = maxRadius + 20.dp.toPx()
                    val lx = center.x + (labelRadius * cos(angle)).toFloat() - labelResult.size.width / 2f
                    val ly = center.y + (labelRadius * sin(angle)).toFloat() - labelResult.size.height / 2f
                    drawText(
                        textLayoutResult = labelResult,
                        topLeft = Offset(
                            x = lx.coerceIn(0f, (size.width - labelResult.size.width).coerceAtLeast(0f)),
                            y = ly.coerceIn(0f, (size.height - labelResult.size.height).coerceAtLeast(0f)),
                        ),
                    )
                }

                // Helper to draw filled + stroked polygon
                fun drawPolygon(values: List<Float>, color: Color, fillAlpha: Float) {
                    val poly = Path()
                    val points = mutableListOf<Offset>()
                    for (i in 0 until count) {
                        val angle = -PI / 2.0 + i * angleStep
                        val value = values[i].coerceIn(0.08f, 1f)
                        val r = maxRadius * value
                        val pt = Offset(
                            x = center.x + (r * cos(angle)).toFloat(),
                            y = center.y + (r * sin(angle)).toFloat(),
                        )
                        points.add(pt)
                        if (i == 0) poly.moveTo(pt.x, pt.y) else poly.lineTo(pt.x, pt.y)
                    }
                    poly.close()

                    drawPath(
                        path = poly,
                        color = color.copy(alpha = fillAlpha),
                    )
                    drawPath(
                        path = poly,
                        color = color,
                        style = Stroke(
                            width = 2.4.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round,
                        ),
                    )
                    points.forEach { pt ->
                        drawCircle(
                            color = color,
                            radius = 3.5.dp.toPx(),
                            center = pt,
                        )
                    }
                }

                if (secondaryAxes != null) {
                    drawPolygon(
                        values = secondaryAxes.asLabeledList().map { it.second },
                        color = secondaryColor,
                        fillAlpha = 0.22f,
                    )
                }

                drawPolygon(
                    values = axes.map { it.second },
                    color = primaryColor,
                    fillAlpha = 0.28f,
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
    }
}

@Composable
private fun RadarLegendPill(label: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Surface(
            modifier = Modifier.size(10.dp),
            shape = CircleShape,
            color = color,
            content = {},
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

package com.mac.starflow

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Typeface
import android.util.TypedValue
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.toBitmap
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.circle
import androidx.graphics.shapes.star
import androidx.graphics.shapes.toPath
import com.mac.starflow.model.ArrowData
import com.mac.starflow.model.IconData
import com.mac.starflow.model.StarShapeData
import java.io.ByteArrayOutputStream
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

internal data class IconAndArrowPositionalData(val iconPosition: Offset, val arrowPosition: Offset)

/** Shape drawn behind the icons and used to anchor the flow arrows. */
public enum class BackgroundShape {
    Circle, Star
}

/**
 * Arranges [icons] evenly around a [BackgroundShape], and draws gradient, curved
 * [arrows] connecting any two of them, e.g. to visualize energy/data/resource flow
 * between a fixed set of nodes (solar panel -> battery -> home, etc).
 *
 * @param starShape background fill color behind the icons.
 * @param icons the nodes of the graph, one per vertex, drawn in order around the shape.
 * @param arrows optional flow arrows connecting two icons by their index in [icons].
 * @param backgroundShapeType whether to draw a [BackgroundShape.Circle] or [BackgroundShape.Star].
 * @param sizeDp the overall (square) size the graph is laid out in.
 * @param typefaceText typeface used for each icon's label.
 * @param iconTextColor ARGB color used for each icon's label.
 * @param modifier standard Compose modifier.
 */
@Composable
public fun StarFlowGraph(
    starShape: StarShapeData,
    icons: List<IconData>,
    arrows: List<ArrowData> = emptyList(),
    backgroundShapeType: BackgroundShape = BackgroundShape.Circle,
    sizeDp: Dp = 250.dp,
    typefaceText: Typeface = Typeface.DEFAULT,
    iconTextColor: Int = Color.DarkGray.toArgb(),
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val sizePx = with(density) { sizeDp.toPx() }

    val offsetDistance = with(density) { 8.dp.toPx() }

    val outerRadius = sizePx / 2.5f
    val innerRadius = outerRadius * 0.5f
    val circleRadius = outerRadius * 0.65f

    val verticesCount = icons.size

    val backgroundShape = when (backgroundShapeType) {
        BackgroundShape.Circle -> RoundedPolygon.circle(radius = circleRadius)
        BackgroundShape.Star -> RoundedPolygon.star(
            numVerticesPerRadius = verticesCount,
            radius = outerRadius,
            innerRadius = innerRadius,
            rounding = CornerRounding(outerRadius * 0.3f, smoothing = 0.8f),
            innerRounding = CornerRounding(innerRadius * 0.5f)
        )
    }
    val primaryStarShape = remember(sizePx, backgroundShapeType, verticesCount) { backgroundShape }

    Canvas(modifier = modifier.size(sizeDp)) {

        val centerX = size.width / 2
        val centerY = size.height / 2
        val center = Offset(centerX, centerY)

        val originalIconAndArrowPoints = generatePointsForIconAndArrowPlacement(
            numVerticesPerRadius = verticesCount,
            outerRadius = outerRadius,
            centerX = centerX,
            centerY = centerY,
            offsetDistance = offsetDistance
        )

        val requiredRotation = -90f

        val path = primaryStarShape.toPath().asComposePath()
        val bounds = path.getBounds()
        val offsetX = centerX - bounds.center.x
        val offsetY = centerY - bounds.center.y

        withTransform({
            translate(offsetX, offsetY)
            if (backgroundShapeType == BackgroundShape.Star) {
                rotate(degrees = requiredRotation, pivot = bounds.center)
            }
        }) {
            drawPath(
                path = path,
                color = Color(starShape.primaryColor),
                style = Fill
            )
        }

        val rotatedIconAndArrowPoints = originalIconAndArrowPoints.map { iconVertex ->
            iconVertex.copy(
                iconPosition = rotatePoint(iconVertex.iconPosition, centerX, centerY, requiredRotation),
                arrowPosition = rotatePoint(iconVertex.arrowPosition, centerX, centerY, requiredRotation)
            )
        }

        val scaledTextSize = outerRadius * 0.115f
        val scaledTextOffset = outerRadius * 0.15f

        icons.zip(rotatedIconAndArrowPoints).forEach { (iconData, vertex) ->

            val imageBitmap = byteArrayToImageBitmap(iconData.icon)
            val iconWidth = imageBitmap.width.toFloat()
            val iconHeight = imageBitmap.height.toFloat()

            drawIntoCanvas { canvas ->
                canvas.drawImage(
                    image = imageBitmap,
                    topLeftOffset = vertex.iconPosition - Offset(iconWidth / 2, iconHeight / 2),
                    paint = Paint()
                )
            }

            val combinedText = if (iconData.secondaryText.isNotEmpty()) {
                "${iconData.primaryText} • ${iconData.secondaryText}"
            } else {
                iconData.primaryText
            }

            drawContext.canvas.nativeCanvas.drawText(
                combinedText,
                vertex.iconPosition.x,
                vertex.iconPosition.y + iconHeight / 2 + scaledTextOffset,
                android.graphics.Paint().apply {
                    color = iconTextColor
                    textSize = scaledTextSize
                    textAlign = android.graphics.Paint.Align.CENTER
                    typeface = Typeface.create(typefaceText, Typeface.BOLD)
                }
            )
        }

        arrows.forEach { arrow ->
            val start = rotatedIconAndArrowPoints.getOrNull(arrow.fromTo.first)
            val end = rotatedIconAndArrowPoints.getOrNull(arrow.fromTo.second)

            if (start != null && end != null) {
                drawCurvedArrow(
                    start.arrowPosition,
                    end.arrowPosition,
                    arrow.thickness,
                    center,
                    listOf(Color(arrow.colorPair.first), Color(arrow.colorPair.second))
                )
            }
        }
    }
}

private fun DrawScope.drawCurvedArrow(
    start: Offset,
    end: Offset,
    lineThickness: Float,
    controlPoint: Offset,
    colors: List<Color>,
) {
    val arrowHeadLength = lineThickness * 0.8f

    val softness = 0.6f

    val softControl = Offset(
        (start.x + end.x) / 2 + (controlPoint.x - (start.x + end.x) / 2) * softness,
        (start.y + end.y) / 2 + (controlPoint.y - (start.y + end.y) / 2) * softness
    )

    val angle = atan2(end.y - softControl.y, end.x - softControl.x)
    val shortenedEnd = Offset(
        end.x - arrowHeadLength * cos(angle),
        end.y - arrowHeadLength * sin(angle)
    )

    val arrowPath = Path().apply {
        moveTo(start.x, start.y)
        quadraticTo(softControl.x, softControl.y, shortenedEnd.x, shortenedEnd.y)
    }

    val gradientBrush = Brush.linearGradient(colors, start, shortenedEnd)

    drawPath(
        path = arrowPath,
        brush = gradientBrush,
        style = Stroke(width = lineThickness, cap = StrokeCap.Round)
    )

    drawClosedArrowhead(end, start, softControl, lineThickness, colors.last())
}

private fun DrawScope.drawClosedArrowhead(
    end: Offset,
    start: Offset,
    control: Offset,
    lineThickness: Float,
    color: Color,
) {
    val t = 1f
    val dx = (1 - t) * (control.x - start.x) + t * (end.x - control.x)
    val dy = (1 - t) * (control.y - start.y) + t * (end.y - control.y)

    val angle = atan2(dy, dx)
    val angleOffset = Math.toRadians(40.0).toFloat()
    val arrowLength = max(lineThickness * 1.4f, 12.5f)

    val arrowPoint1 = Offset(
        x = end.x - arrowLength * cos(angle - angleOffset),
        y = end.y - arrowLength * sin(angle - angleOffset)
    )

    val arrowPoint2 = Offset(
        x = end.x - arrowLength * cos(angle + angleOffset),
        y = end.y - arrowLength * sin(angle + angleOffset)
    )

    val arrowHeadPath = Path().apply {
        moveTo(end.x, end.y)
        lineTo(arrowPoint1.x, arrowPoint1.y)
        lineTo(arrowPoint2.x, arrowPoint2.y)
        close()
    }

    drawPath(
        path = arrowHeadPath,
        color = color
    )
}

private fun generatePointsForIconAndArrowPlacement(
    numVerticesPerRadius: Int,
    outerRadius: Float,
    centerX: Float,
    centerY: Float,
    offsetDistance: Float = 0f,
): List<IconAndArrowPositionalData> {
    val angleStep = (Math.PI * 2) / (numVerticesPerRadius * 2)
    val placements = mutableListOf<IconAndArrowPositionalData>()

    val arrowEndRadius = (outerRadius * 0.68f) + offsetDistance - 20f

    for (i in 0 until numVerticesPerRadius * 2) {
        val angle = i * angleStep
        if (i % 2 == 0) {
            val iconX = (centerX + cos(angle) * (outerRadius + offsetDistance)).toFloat()
            val iconY = (centerY + sin(angle) * (outerRadius + offsetDistance)).toFloat()
            val arrowX = (centerX + cos(angle) * arrowEndRadius).toFloat()
            val arrowY = (centerY + sin(angle) * arrowEndRadius).toFloat()
            placements.add(IconAndArrowPositionalData(Offset(iconX, iconY), Offset(arrowX, arrowY)))
        }
    }

    return placements
}

private fun rotatePoint(point: Offset, centerX: Float, centerY: Float, angle: Float = -90f): Offset {
    val radians = Math.toRadians(angle.toDouble())
    val cosTheta = cos(radians).toFloat()
    val sinTheta = sin(radians).toFloat()

    val translatedX = point.x - centerX
    val translatedY = point.y - centerY

    val rotatedX = translatedX * cosTheta - translatedY * sinTheta
    val rotatedY = translatedX * sinTheta + translatedY * cosTheta

    return Offset(rotatedX + centerX, rotatedY + centerY)
}

/** Scales a drawable resource down to a square bitmap of [targetDp] on each side. */
public fun getScaledIcon(context: Context, drawableRes: Int, targetDp: Float = 24f): Bitmap {
    val drawable = ContextCompat.getDrawable(context, drawableRes)
        ?: return createBitmap(1, 1)

    val targetPx = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, targetDp, context.resources.displayMetrics
    ).toInt()

    return drawable.toBitmap(targetPx, targetPx, Bitmap.Config.ARGB_8888)
}

public fun byteArrayToImageBitmap(byteArray: ByteArray): ImageBitmap =
    BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size).asImageBitmap()

public fun bitmapToByteArray(
    bitmap: Bitmap,
    format: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG,
    quality: Int = 100
): ByteArray {
    val outputStream = ByteArrayOutputStream()
    bitmap.compress(format, quality, outputStream)
    return outputStream.toByteArray()
}

// ---------------------------------------------------------------------------
// Previews. These build their own tiny placeholder icons at runtime so the
// library module stays self-contained (no drawable resources required just
// to preview it). See the `sample` module for a richer, real-icon demo.
// ---------------------------------------------------------------------------

private fun previewDot(color: Int): ByteArray {
    val bitmap = createBitmap(48, 48)
    val canvas = android.graphics.Canvas(bitmap)
    canvas.drawCircle(
        24f, 24f, 20f,
        android.graphics.Paint().apply {
            isAntiAlias = true
            this.color = color
        }
    )
    return bitmapToByteArray(bitmap)
}

@Preview(showBackground = true)
@Composable
private fun StarFlowGraphCirclePreview() {
    StarFlowGraph(
        starShape = StarShapeData(0xFFDBDFE3.toInt()),
        icons = listOf(
            IconData(previewDot(0xFFEFA139.toInt()), "14,2 units"),
            IconData(previewDot(0xFF315AE7.toInt()), "10,2 units"),
            IconData(previewDot(0xFF34A853.toInt()), "2,0 units"),
        ),
        arrows = listOf(
            ArrowData(fromTo = 0 to 1, thickness = 30f, colorPair = 0xFFFBE8A5.toInt() to 0xFFEFA139.toInt()),
            ArrowData(fromTo = 0 to 2, thickness = 10f, colorPair = 0xFFFBE8A5.toInt() to 0xFFEFA139.toInt()),
        ),
    )
}

@Preview(showBackground = true)
@Composable
private fun StarFlowGraphStarPreview() {
    StarFlowGraph(
        starShape = StarShapeData(0xFFDBDFE3.toInt()),
        backgroundShapeType = BackgroundShape.Star,
        icons = listOf(
            IconData(previewDot(0xFFEFA139.toInt()), "4,5 units"),
            IconData(previewDot(0xFF9747FF.toInt()), "1,1 units", "56%"),
            IconData(previewDot(0xFF315AE7.toInt()), "3,1 units"),
            IconData(previewDot(0xFF34A853.toInt()), "0,2 units"),
            IconData(previewDot(0xFF888888.toInt()), "0 units"),
        ),
        arrows = listOf(
            ArrowData(fromTo = 0 to 1, colorPair = 0xFFFBE8A5.toInt() to 0xFFEFA139.toInt()),
            ArrowData(fromTo = 0 to 2, thickness = 20f, colorPair = 0xFFFBE8A5.toInt() to 0xFFEFA139.toInt()),
            ArrowData(fromTo = 0 to 3, thickness = 5f, colorPair = 0xFFFBE8A5.toInt() to 0xFFEFA139.toInt()),
        ),
    )
}

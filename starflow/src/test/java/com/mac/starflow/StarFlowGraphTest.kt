package com.mac.starflow

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.mac.starflow.model.ArrowData
import com.mac.starflow.model.IconData
import com.mac.starflow.model.StarShapeData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import androidx.compose.ui.graphics.Canvas as ComposeCanvas

private const val TAG = "starflow"
private const val SHAPE_COLOR = 0xFFDBDFE3.toInt()
private const val ARROW_LIGHT = 0xFFFBE8A5.toInt()
private const val ARROW_DARK = 0xFFEFA139.toInt()

private fun dot(color: Int): ByteArray {
    val bitmap = Bitmap.createBitmap(16, 16, Bitmap.Config.ARGB_8888)
    Canvas(bitmap).drawColor(color)
    return bitmapToByteArray(bitmap)
}

private fun threeIcons() = listOf(
    IconData(dot(0xFFEFA139.toInt()), "14,2 units"),
    IconData(dot(0xFF315AE7.toInt()), "10,2 units", "56%"),
    IconData(dot(0xFF34A853.toInt()), "2,0 units"),
)

private fun fiveIcons() = listOf(
    IconData(dot(0xFFEFA139.toInt()), "4,5 units"),
    IconData(dot(0xFF9747FF.toInt()), "1,1 units", "56%"),
    IconData(dot(0xFF315AE7.toInt()), "3,1 units"),
    IconData(dot(0xFF34A853.toInt()), "0,2 units"),
    IconData(dot(0xFF888888.toInt()), "0 units"),
)

/** Layout of the composable, via the Compose test rule. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w480dp-h900dp-mdpi")
class StarFlowGraphLayoutTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `occupies the requested size`() {
        composeRule.setContent {
            StarFlowGraph(
                starShape = StarShapeData(SHAPE_COLOR),
                icons = threeIcons(),
                sizeDp = 200.dp,
                modifier = Modifier.testTag(TAG),
            )
        }

        composeRule.onNodeWithTag(TAG)
            .assertIsDisplayed()
            .assertWidthIsEqualTo(200.dp)
            .assertHeightIsEqualTo(200.dp)
    }

    @Test
    fun `defaults to 250dp`() {
        composeRule.setContent {
            StarFlowGraph(
                starShape = StarShapeData(SHAPE_COLOR),
                icons = threeIcons(),
                modifier = Modifier.testTag(TAG),
            )
        }

        composeRule.onNodeWithTag(TAG)
            .assertWidthIsEqualTo(250.dp)
            .assertHeightIsEqualTo(250.dp)
    }
}

/**
 * What the graph actually draws. Rendered straight into an off-screen [ImageBitmap] with
 * [CanvasDrawScope] instead of capturing a window: `captureToImage()` waits for a real draw
 * frame, which Robolectric's paused main looper never delivers.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34])
class StarFlowGraphDrawTest {

    private fun render(
        icons: List<IconData>,
        arrows: List<ArrowData> = emptyList(),
        shape: BackgroundShape = BackgroundShape.Circle,
        sizePx: Int = 200,
    ): Bitmap {
        val image = ImageBitmap(sizePx, sizePx)
        val outerRadius = sizePx / 2.5f
        CanvasDrawScope().draw(
            Density(1f),
            LayoutDirection.Ltr,
            ComposeCanvas(image),
            Size(sizePx.toFloat(), sizePx.toFloat()),
        ) {
            drawStarFlow(
                starShape = StarShapeData(SHAPE_COLOR),
                icons = icons,
                arrows = arrows,
                backgroundShapeType = shape,
                primaryStarShape = backgroundShapeFor(shape, icons.size, outerRadius),
                outerRadius = outerRadius,
                offsetDistance = 8f,
            )
        }
        return image.asAndroidBitmap()
    }

    private fun Bitmap.centerPixel(): Int = getPixel(width / 2, height / 2)

    @Test
    fun `circle is filled with the requested color at its center`() {
        assertEquals(SHAPE_COLOR, render(threeIcons()).centerPixel())
    }

    @Test
    fun `circle does not paint the corners of the canvas`() {
        assertNotEquals(SHAPE_COLOR, render(threeIcons()).getPixel(2, 2))
    }

    @Test
    fun `star is filled with the requested color at its center`() {
        assertEquals(SHAPE_COLOR, render(fiveIcons(), shape = BackgroundShape.Star).centerPixel())
    }

    @Test
    fun `draws icons, labels and arrows for both shapes without throwing`() {
        val arrows = listOf(
            ArrowData(fromTo = 0 to 1, colorPair = ARROW_LIGHT to ARROW_DARK),
            ArrowData(fromTo = 0 to 2, thickness = 20f, colorPair = ARROW_LIGHT to ARROW_DARK),
            ArrowData(fromTo = 0 to 3, thickness = 5f, colorPair = ARROW_LIGHT to ARROW_DARK),
        )

        BackgroundShape.entries.forEach { shape ->
            val bitmap = render(fiveIcons(), arrows, shape, sizePx = 250)
            assertEquals(250, bitmap.width)
        }
    }

    @Test
    fun `arrows pointing at missing icons are ignored`() {
        val arrows = listOf(
            ArrowData(fromTo = 0 to 99),
            ArrowData(fromTo = -1 to 1),
            ArrowData(fromTo = 5 to 6),
        )

        assertEquals(SHAPE_COLOR, render(threeIcons(), arrows).centerPixel())
    }

    @Test
    fun `circle with no icons still draws the background`() {
        assertEquals(SHAPE_COLOR, render(emptyList()).centerPixel())
    }
}
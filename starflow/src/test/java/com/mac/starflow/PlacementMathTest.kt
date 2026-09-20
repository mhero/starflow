package com.mac.starflow

import androidx.compose.ui.geometry.Offset
import com.mac.starflow.model.IconData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.atan2
import kotlin.math.hypot

private const val EPS = 1e-3f

private fun Offset.distanceTo(other: Offset): Float = hypot(x - other.x, y - other.y)

private fun assertOffsetEquals(expected: Offset, actual: Offset, eps: Float = EPS) {
    assertEquals("x", expected.x, actual.x, eps)
    assertEquals("y", expected.y, actual.y, eps)
}

class GeneratePointsForIconAndArrowPlacementTest {

    private val cx = 200f
    private val cy = 200f
    private val outerRadius = 100f
    private val offset = 8f
    private val center = Offset(cx, cy)

    private fun place(n: Int, offsetDistance: Float = offset) =
        generatePointsForIconAndArrowPlacement(
            numVerticesPerRadius = n,
            outerRadius = outerRadius,
            centerX = cx,
            centerY = cy,
            offsetDistance = offsetDistance,
        )

    @Test
    fun `produces exactly one placement per icon`() {
        for (n in 1..8) {
            assertEquals("n=$n", n, place(n).size)
        }
    }

    @Test
    fun `no icons means no placements`() {
        assertTrue(place(0).isEmpty())
    }

    @Test
    fun `icons sit on a ring of outerRadius plus offset`() {
        place(6).forEach {
            assertEquals(outerRadius + offset, it.iconPosition.distanceTo(center), EPS)
        }
    }

    @Test
    fun `arrow anchors sit on a smaller inner ring`() {
        // arrowEndRadius = outerRadius * 0.68 + offset - 20 = 56 for these inputs
        place(6).forEach {
            assertEquals(56f, it.arrowPosition.distanceTo(center), EPS)
        }
    }

    @Test
    fun `offset defaults to zero`() {
        val placements = generatePointsForIconAndArrowPlacement(
            numVerticesPerRadius = 3,
            outerRadius = outerRadius,
            centerX = cx,
            centerY = cy,
        )
        placements.forEach {
            assertEquals(outerRadius, it.iconPosition.distanceTo(center), EPS)
        }
    }

    @Test
    fun `first icon is placed at angle zero, to the right of center`() {
        val first = place(4).first()

        assertOffsetEquals(Offset(cx + outerRadius + offset, cy), first.iconPosition)
        assertOffsetEquals(Offset(cx + 56f, cy), first.arrowPosition)
    }

    @Test
    fun `four icons are spaced 90 degrees apart, clockwise on screen`() {
        val r = outerRadius + offset
        val icons = place(4).map { it.iconPosition }

        assertOffsetEquals(Offset(cx + r, cy), icons[0])
        assertOffsetEquals(Offset(cx, cy + r), icons[1]) // +y is down on screen
        assertOffsetEquals(Offset(cx - r, cy), icons[2])
        assertOffsetEquals(Offset(cx, cy - r), icons[3])
    }

    @Test
    fun `each icon and its arrow anchor lie on the same ray from the center`() {
        place(5).forEach {
            val iconAngle = atan2(it.iconPosition.y - cy, it.iconPosition.x - cx)
            val arrowAngle = atan2(it.arrowPosition.y - cy, it.arrowPosition.x - cx)
            assertEquals(iconAngle, arrowAngle, EPS)
        }
    }

    @Test
    fun `neighbouring icons are equally far apart`() {
        val icons = place(5).map { it.iconPosition }
        val gaps = icons.indices.map { icons[it].distanceTo(icons[(it + 1) % icons.size]) }

        gaps.forEach { assertEquals(gaps.first(), it, EPS) }
    }

    @Test
    fun `placement is centered on the requested center`() {
        val shifted = generatePointsForIconAndArrowPlacement(
            numVerticesPerRadius = 4,
            outerRadius = outerRadius,
            centerX = 0f,
            centerY = 0f,
            offsetDistance = offset,
        )
        val moved = place(4)

        shifted.zip(moved).forEach { (a, b) ->
            assertOffsetEquals(Offset(a.iconPosition.x + cx, a.iconPosition.y + cy), b.iconPosition)
        }
    }
}

class RotatePointTest {

    private val cx = 200f
    private val cy = 200f
    private val point = Offset(cx + 10f, cy) // 10px to the right of the pivot

    @Test
    fun `zero degrees is the identity`() {
        assertOffsetEquals(point, rotatePoint(point, cx, cy, 0f))
    }

    @Test
    fun `positive 90 degrees turns right into down (clockwise on screen)`() {
        assertOffsetEquals(Offset(cx, cy + 10f), rotatePoint(point, cx, cy, 90f))
    }

    @Test
    fun `negative 90 degrees turns right into up`() {
        assertOffsetEquals(Offset(cx, cy - 10f), rotatePoint(point, cx, cy, -90f))
    }

    @Test
    fun `default angle is -90, which puts the first icon at the top`() {
        assertOffsetEquals(rotatePoint(point, cx, cy, -90f), rotatePoint(point, cx, cy))
    }

    @Test
    fun `180 degrees mirrors through the pivot`() {
        assertOffsetEquals(Offset(cx - 10f, cy), rotatePoint(point, cx, cy, 180f))
    }

    @Test
    fun `a full turn returns to the start`() {
        assertOffsetEquals(point, rotatePoint(point, cx, cy, 360f))
    }

    @Test
    fun `the pivot itself never moves`() {
        assertOffsetEquals(Offset(cx, cy), rotatePoint(Offset(cx, cy), cx, cy, 123f))
    }

    @Test
    fun `rotation preserves distance from the pivot`() {
        val pivot = Offset(cx, cy)
        val start = Offset(150f, 260f)

        listOf(-90f, -33f, 17f, 45f, 200f).forEach { angle ->
            val rotated = rotatePoint(start, cx, cy, angle)
            assertEquals("angle=$angle", start.distanceTo(pivot), rotated.distanceTo(pivot), EPS)
        }
    }

    @Test
    fun `rotating by an angle then its opposite restores the point`() {
        val start = Offset(150f, 260f)
        val roundTrip = rotatePoint(rotatePoint(start, cx, cy, 37f), cx, cy, -37f)

        assertOffsetEquals(start, roundTrip)
    }
}

class IconLabelTextTest {

    private val bytes = byteArrayOf(0)

    @Test
    fun `primary text only when there is no secondary text`() {
        assertEquals("14,2 units", IconData(bytes, "14,2 units").labelText())
    }

    @Test
    fun `primary and secondary are joined with a bullet`() {
        assertEquals("1,1 units • 56%", IconData(bytes, "1,1 units", "56%").labelText())
    }
}

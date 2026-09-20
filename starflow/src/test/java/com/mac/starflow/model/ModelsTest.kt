package com.mac.starflow.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StarShapeDataTest {

    @Test
    fun `default color is light gray`() {
        assertEquals(0xFFD3D3D3.toInt(), StarShapeData().primaryColor)
    }

    @Test
    fun `custom color is kept`() {
        assertEquals(0xFF112233.toInt(), StarShapeData(primaryColor = 0xFF112233.toInt()).primaryColor)
    }
}

class ArrowDataTest {

    @Test
    fun `defaults`() {
        val arrow = ArrowData(fromTo = 0 to 1)

        assertEquals(0 to 1, arrow.fromTo)
        assertEquals(7f, arrow.thickness, 0f)
        assertEquals(0xFF000000.toInt(), arrow.primaryColor)
        assertEquals(0xFF888888.toInt(), arrow.secondaryColor)
        assertEquals(0xFF888888.toInt() to 0xFF000000.toInt(), arrow.colorPair)
    }

    @Test
    fun `copy changes only the requested field`() {
        val original = ArrowData(fromTo = 2 to 3, thickness = 12f)
        val copy = original.copy(thickness = 30f)

        assertEquals(2 to 3, copy.fromTo)
        assertEquals(30f, copy.thickness, 0f)
        assertEquals(original.colorPair, copy.colorPair)
    }

    @Test
    fun `arrows with the same values are equal`() {
        assertEquals(ArrowData(fromTo = 0 to 1), ArrowData(fromTo = 0 to 1))
        assertNotEquals(ArrowData(fromTo = 0 to 1), ArrowData(fromTo = 1 to 0))
    }
}

class IconDataTest {

    private val bytes = byteArrayOf(1, 2, 3)

    @Test
    fun `secondary text defaults to empty`() {
        assertEquals("", IconData(bytes, "1 unit").secondaryText)
    }

    @Test
    fun `equality compares icon bytes by content, not by reference`() {
        val a = IconData(byteArrayOf(1, 2, 3), "label", "56%")
        val b = IconData(byteArrayOf(1, 2, 3), "label", "56%")

        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun `equals is reflexive and rejects null and other types`() {
        val a = IconData(bytes, "label")

        assertTrue(a == a)
        assertFalse(a.equals(null))
        assertFalse(a.equals("label"))
    }

    @Test
    fun `different icon bytes are not equal`() {
        assertNotEquals(IconData(byteArrayOf(1, 2, 3), "x"), IconData(byteArrayOf(1, 2, 4), "x"))
    }

    @Test
    fun `different primary or secondary text is not equal`() {
        assertNotEquals(IconData(bytes, "a"), IconData(bytes, "b"))
        assertNotEquals(IconData(bytes, "a", "1"), IconData(bytes, "a", "2"))
    }

    @Test
    fun `equal icons collapse in a hash set`() {
        val set = hashSetOf(
            IconData(byteArrayOf(9, 9), "x"),
            IconData(byteArrayOf(9, 9), "x"),
        )
        assertEquals(1, set.size)
    }

    @Test
    fun `copy keeps unchanged fields`() {
        val original = IconData(bytes, "primary", "secondary")
        val copy = original.copy(primaryText = "changed")

        assertEquals("changed", copy.primaryText)
        assertEquals("secondary", copy.secondaryText)
        assertTrue(bytes.contentEquals(copy.icon))
    }
}

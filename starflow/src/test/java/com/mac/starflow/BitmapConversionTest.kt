package com.mac.starflow

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * NATIVE graphics mode makes Robolectric use the real Skia bitmap codecs, so PNG/JPEG
 * encoding and decoding behave like they do on a device.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "mdpi")
class BitmapConversionTest {

    private fun solidBitmap(width: Int, height: Int, color: Int): Bitmap =
        Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).also { Canvas(it).drawColor(color) }

    @Test
    fun `bitmapToByteArray encodes PNG by default`() {
        val bytes = bitmapToByteArray(solidBitmap(8, 6, Color.RED))

        // PNG signature: 0x89 'P' 'N' 'G'
        assertEquals(0x89.toByte(), bytes[0])
        assertEquals('P'.code.toByte(), bytes[1])
        assertEquals('N'.code.toByte(), bytes[2])
        assertEquals('G'.code.toByte(), bytes[3])
    }

    @Test
    fun `bitmapToByteArray honours the requested format`() {
        val bytes = bitmapToByteArray(solidBitmap(8, 6, Color.RED), Bitmap.CompressFormat.JPEG, quality = 80)

        // JPEG SOI marker: 0xFF 0xD8
        assertEquals(0xFF.toByte(), bytes[0])
        assertEquals(0xD8.toByte(), bytes[1])
    }

    @Test
    fun `PNG round trip keeps size and pixels`() {
        val color = Color.rgb(200, 30, 60)

        val decoded = byteArrayToImageBitmap(bitmapToByteArray(solidBitmap(8, 6, color)))

        assertEquals(8, decoded.width)
        assertEquals(6, decoded.height)
        assertEquals(color, decoded.asAndroidBitmap().getPixel(3, 2))
    }

    @Test
    fun `getScaledIcon returns a square bitmap of targetDp at mdpi`() {
        val bitmap = getScaledIcon(RuntimeEnvironment.getApplication(), android.R.drawable.ic_menu_add, 24f)

        assertEquals(24, bitmap.width)
        assertEquals(24, bitmap.height)
    }

    @Test
    @Config(sdk = [34], qualifiers = "xhdpi")
    fun `getScaledIcon scales dp to pixels using screen density`() {
        val context = RuntimeEnvironment.getApplication()

        val defaultSize = getScaledIcon(context, android.R.drawable.ic_menu_add)
        val custom = getScaledIcon(context, android.R.drawable.ic_menu_add, targetDp = 40f)

        // xhdpi is 2x: the default targetDp is 24dp -> 48px, and 40dp -> 80px
        assertEquals(48, defaultSize.width)
        assertEquals(48, defaultSize.height)
        assertEquals(80, custom.width)
        assertEquals(80, custom.height)
        assertTrue(custom.config == Bitmap.Config.ARGB_8888)
    }
}

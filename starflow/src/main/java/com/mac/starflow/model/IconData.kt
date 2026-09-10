package com.mac.starflow.model

/**
 * One vertex of a StarFlow graph: an icon plus the text drawn underneath it.
 *
 * The icon is stored as a raw [ByteArray] (e.g. a PNG) rather than a drawable
 * resource ID so that the graph itself stays decoupled from any particular
 * resource set. Use [com.mac.starflow.bitmapToByteArray] to build one from a
 * [android.graphics.Bitmap] or drawable resource.
 */
public data class IconData(
    val icon: ByteArray,
    val primaryText: String,
    val secondaryText: String = "",
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IconData

        if (!icon.contentEquals(other.icon)) return false
        if (primaryText != other.primaryText) return false
        if (secondaryText != other.secondaryText) return false

        return true
    }

    override fun hashCode(): Int {
        var result = icon.contentHashCode()
        result = 31 * result + primaryText.hashCode()
        result = 31 * result + secondaryText.hashCode()
        return result
    }
}

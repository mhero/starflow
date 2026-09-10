package com.mac.starflow.model

/**
 * Describes one flow arrow drawn between two vertices of a StarFlow graph. The
 * indices refer to positions in the `icons` list passed to the graph.
 *
 * @param fromTo the (startIconIndex, endIconIndex) pair the arrow connects.
 * @param thickness stroke width of the arrow, in px.
 * @param primaryColor reserved for future single-color styling.
 * @param secondaryColor reserved for future single-color styling.
 * @param colorPair the (start, end) ARGB colors used for the arrow's gradient.
 */
public data class ArrowData(
    val fromTo: Pair<Int, Int>,
    val thickness: Float = 7f,
    val primaryColor: Int = 0xFF000000.toInt(),
    val secondaryColor: Int = 0xFF888888.toInt(),
    val colorPair: Pair<Int, Int> = 0xFF888888.toInt() to 0xFF000000.toInt(),
)

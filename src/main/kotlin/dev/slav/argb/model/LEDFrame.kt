package dev.slav.argb.model

import java.awt.Color

/**
 * LED animation frame.
 */
data class LEDFrame(
    /**
     * LED colors in this frame.
     */
    val colors: List<Color>
) {

    /**
     * 565 RGB representations of the LED colors in this frame.
     */
    val colors565: List<UShort> by lazy {
        colors.map(Color::to565RGB)
    }
}

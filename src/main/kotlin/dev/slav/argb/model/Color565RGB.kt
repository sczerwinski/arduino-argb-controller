@file:JvmName(name = "Color565RGB")

package dev.slav.argb.model

import java.awt.Color

private const val FACTOR_R = 8
private const val FACTOR_G = 4
private const val FACTOR_B = 8

private const val BIT_SHIFT_R = 11
private const val BIT_SHIFT_G = 5

private const val BIT_MASK_R = 0x1f
private const val BIT_MASK_G = 0x3f
private const val BIT_MASK_B = 0x1f

/**
 * Converts this color to 565 RGB value.
 *
 * @return 565 RGB value of this color.
 */
fun Color.to565RGB(): UShort {
    val r = red / FACTOR_R
    val g = green / FACTOR_G
    val b = blue / FACTOR_B
    return ((r shl BIT_SHIFT_R) + (g shl BIT_SHIFT_G) + b).toUShort()
}

/**
 * Converts given 565 RGB [value] to color.
 *
 * @param value 565 RGB value of a color.
 *
 * @return A new color.
 */
fun colorFrom565RGB(value: UShort): Color {
    val intValue = value.toInt()
    val r = (intValue shr BIT_SHIFT_R) and BIT_MASK_R
    val g = (intValue shr BIT_SHIFT_G) and BIT_MASK_G
    val b = intValue and BIT_MASK_B
    return Color(r * FACTOR_R, g * FACTOR_G, b * FACTOR_B)
}

package com.example.chromaalbum.domain.palette

import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToInt

/** Converts a gamma-encoded sRGB channel value (0–1) to linear light for luminance math. */
fun linearise(c: Double) = if (c <= 0.04045) c / 12.92 else ((c + 0.055) / 1.055).pow(2.4)

/** Parses a `#RRGGBB` hex string into a packed 24-bit `0xRRGGBB` integer. */
fun hexToRgb(hex: String): Int = hex.removePrefix("#").toInt(16)

/**
 * Returns the WCAG relative luminance of [hex] in the range [0.0, 1.0].
 *
 * Channels are linearised before applying the standard perceptual weights
 * (R 21.26 %, G 71.52 %, B 7.22 %).
 */
fun relativeLuminance(hex: String): Double {
    val rgb = hexToRgb(hex)
    val r = linearise(((rgb shr 16) and 0xFF) / 255.0)
    val g = linearise(((rgb shr 8) and 0xFF) / 255.0)
    val b = linearise((rgb and 0xFF) / 255.0)
    return 0.2126 * r + 0.7152 * g + 0.0722 * b
}

/**
 * Returns the WCAG contrast ratio between two colors in the range [1.0, 21.0].
 *
 * Argument order does not affect the result. Common thresholds:
 * - 4.5 : 1 — WCAG AA (normal text)
 * - 7.0 : 1 — WCAG AAA (enhanced)
 */
fun contrastRatio(
    colorHex1: String,
    colorHex2: String,
): Double {
    val l1 = relativeLuminance(colorHex1)
    val l2 = relativeLuminance(colorHex2)
    val lighter = maxOf(l1, l2)
    val darker = minOf(l1, l2)
    return (lighter + 0.05) / (darker + 0.05)
}

/**
 * Alpha-composites a semi-transparent foreground over an opaque background.
 *
 * Each channel is blended as `fg * alpha + bg * (1 - alpha)`. [opacityFraction] must be in
 * [0.0, 1.0]; 1.0 returns the foreground unchanged, 0.0 returns the background unchanged.
 */
fun compositeColorOverBackground(
    foregroundHex: String,
    backgroundHex: String,
    opacityFraction: Double,
): String {
    val fg = hexToRgb(foregroundHex)
    val bg = hexToRgb(backgroundHex)
    val blend = { fgC: Int, bgC: Int ->
        (fgC * opacityFraction + bgC * (1.0 - opacityFraction)).roundToInt().coerceIn(0, 255)
    }
    val r = blend((fg shr 16) and 0xFF, (bg shr 16) and 0xFF)
    val g = blend((fg shr 8) and 0xFF, (bg shr 8) and 0xFF)
    val b = blend(fg and 0xFF, bg and 0xFF)
    return "#%02X%02X%02X".format(r, g, b)
}

/**
 * Adjusts [textColorHex] until its contrast ratio against [backgroundColorHex] meets [minRatio].
 *
 * Hue and saturation are preserved; only HSL lightness is shifted. The function binary-searches
 * in both directions (toward white and toward black) and picks whichever requires the smaller
 * lightness change. On a tie it prefers the lighter direction. Defaults to WCAG AA (4.5 : 1).
 */
fun adjustForContrast(
    textColorHex: String,
    backgroundColorHex: String,
    minRatio: Double = 4.5,
): String {
    if (contrastRatio(textColorHex, backgroundColorHex) >= minRatio) return textColorHex

    val (h, s, l) = hexToHsl(textColorHex)

    // Binary-search toward white (increase L)
    var loLight = l
    var hiLight = 1f
    repeat(20) {
        val mid = (loLight + hiLight) / 2f
        if (contrastRatio(hslToHex(h, s, mid), backgroundColorHex) >= minRatio) hiLight = mid else loLight = mid
    }
    val lightL = hiLight
    val lightHex = hslToHex(h, s, lightL)
    val lightMeetsRatio = contrastRatio(lightHex, backgroundColorHex) >= minRatio

    // Binary-search toward black (decrease L)
    var loDark = 0f
    var hiDark = l
    repeat(20) {
        val mid = (loDark + hiDark) / 2f
        if (contrastRatio(hslToHex(h, s, mid), backgroundColorHex) >= minRatio) loDark = mid else hiDark = mid
    }
    val darkL = loDark
    val darkHex = hslToHex(h, s, darkL)
    val darkMeetsRatio = contrastRatio(darkHex, backgroundColorHex) >= minRatio

    return when {
        lightMeetsRatio && darkMeetsRatio ->
            if (abs(lightL - l) <= abs(darkL - l)) lightHex else darkHex
        lightMeetsRatio -> lightHex
        darkMeetsRatio -> darkHex
        else -> if (relativeLuminance(backgroundColorHex) > 0.5) "#000000" else "#FFFFFF"
    }
}

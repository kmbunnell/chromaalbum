package com.example.chromaalbum.domain.palette

import kotlin.math.pow

fun linearise(c: Double) = if (c <= 0.04045) c / 12.92 else ((c + 0.055) / 1.055).pow(2.4)

fun hexToRgb(hex: String): Int = hex.removePrefix("#").toInt(16)

package com.example.data

data class SlabDimension(
    val length: Double, // in inches
    val width: Double   // in inches
) {
    val areaSqFt: Double
        get() = (length * width) / 144.0

    val areaSqM: Double
        get() = areaSqFt * 0.092903
}

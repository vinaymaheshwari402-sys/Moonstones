package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.Gold
import com.example.ui.theme.LuxuryBlack
import kotlin.random.Random

@Composable
fun QrCodeDrawer(
    sku: String,
    modifier: Modifier = Modifier,
    qrSize: Dp = 140.dp
) {
    // Generate deterministic pixel map based on SKU to draw unique QR codes
    val seed = remember(sku) { sku.hashCode().toLong() }
    val random = remember(seed) { Random(seed) }

    // Represent a 17x17 grid (standard compact QR code)
    val gridSize = 17
    val qrGrid = remember(seed) {
        Array(gridSize) { r ->
            BooleanArray(gridSize) { c ->
                // QR code anchors (corners) must remain fixed
                val isAnchor = (r < 5 && c < 5) || (r < 5 && c >= gridSize - 5) || (r >= gridSize - 5 && c < 5)
                if (isAnchor) {
                    // Handled separately below
                    false
                } else {
                    random.nextBoolean()
                }
            }
        }
    }

    Box(
        modifier = modifier
            .size(qrSize)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
            .border(2.dp, Gold, RoundedCornerShape(8.dp))
            .padding(10.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(qrSize - 20.dp)) {
            val cellWidth = size.width / gridSize
            val cellHeight = size.height / gridSize

            // Draw QR code background
            drawRect(color = Color.White)

            // Draw deterministic matrix pixels
            for (r in 0 until gridSize) {
                for (c in 0 until gridSize) {
                    val x = c * cellWidth
                    val y = r * cellHeight

                    // 1. Draw top-left, top-right, bottom-left anchors
                    val isTopLeftAnchor = r < 5 && c < 5
                    val isTopRightAnchor = r < 5 && c >= gridSize - 5
                    val isBottomLeftAnchor = r >= gridSize - 5 && c < 5

                    if (isTopLeftAnchor || isTopRightAnchor || isBottomLeftAnchor) {
                        // We draw outer ring, middle ring, inner dot
                        val localR = if (isTopLeftAnchor) r else if (isTopRightAnchor) r else r - (gridSize - 5)
                        val localC = if (isTopLeftAnchor) c else if (isTopRightAnchor) c - (gridSize - 5) else c

                        val drawPixel = (localR == 0 || localR == 4 || localC == 0 || localC == 4) || (localR == 2 && localC == 2)
                        if (drawPixel) {
                            drawRect(
                                color = LuxuryBlack,
                                topLeft = Offset(x, y),
                                size = Size(cellWidth, cellHeight)
                            )
                        }
                    } else if (qrGrid[r][c]) {
                        // 2. Draw standard random data pixel
                        drawRect(
                            color = LuxuryBlack,
                            topLeft = Offset(x, y),
                            size = Size(cellWidth, cellHeight)
                        )
                    }
                }
            }
        }
    }
}

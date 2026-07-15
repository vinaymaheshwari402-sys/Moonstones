package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.ui.theme.Gold
import com.example.ui.theme.GoldLight
import com.example.ui.theme.LuxuryBlack
import com.example.ui.theme.LuxuryDarkGray
import com.example.ui.theme.MarbleWhite
import kotlin.random.Random

@Composable
fun MarbleTexture(
    marbleColor: String,
    category: String,
    modifier: Modifier = Modifier
) {
    // Generate deterministic seed based on product name to keep veins consistent
    val seed = remember(marbleColor, category) {
        (marbleColor.hashCode() + category.hashCode()).toLong()
    }

    val random = remember(seed) { Random(seed) }

    // Pick luxury base gradient colors
    val baseColors = remember(marbleColor) {
        when (marbleColor.lowercase()) {
            "black" -> listOf(LuxuryBlack, LuxuryDarkGray, Color(0xFF1E1E1E))
            "white" -> listOf(MarbleWhite, Color(0xFFEAEAEA), Color(0xFFDCDCDC))
            "golden" -> listOf(Color(0xFF2E2203), Color(0xFF4A3B12), Color(0xFF1A1300))
            "beige" -> listOf(Color(0xFFF5ECCB), Color(0xFFE2D4A8), Color(0xFFECE0BC))
            "grey", "gray" -> listOf(Color(0xFF2C3E50), Color(0xFF1A252F), Color(0xFF0F171E))
            else -> listOf(LuxuryDarkGray, Color(0xFF252525), LuxuryBlack)
        }
    }

    // Pick vein colors
    val veinColors = remember(marbleColor) {
        when (marbleColor.lowercase()) {
            "black" -> listOf(Gold, GoldLight, Color(0xFFE5C158))
            "white" -> listOf(Color(0xFF5A5A5A), Color(0xFF8C8C8C), Gold)
            "golden" -> listOf(GoldLight, MarbleWhite, Color(0xFFFFE082))
            "beige" -> listOf(Color(0xFF8D6E63), Gold, Color(0xFF795548))
            "grey", "gray" -> listOf(MarbleWhite, Gold, Color(0xFFB0BEC5))
            else -> listOf(Gold, MarbleWhite, Color(0xFF888888))
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, Brush.horizontalGradient(listOf(Gold, Color.Transparent)), RoundedCornerShape(12.dp))
            .background(Brush.radialGradient(baseColors))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            if (width == 0f || height == 0f) return@Canvas

            // Draw procedural veins
            val numVeins = random.nextInt(3, 7)
            for (i in 0 until numVeins) {
                val path = Path()
                
                // Pick random start and end positions
                val startX = random.nextFloat() * width
                val startY = if (random.nextBoolean()) 0f else random.nextFloat() * height
                val endX = random.nextFloat() * width
                val endY = if (random.nextBoolean()) height else random.nextFloat() * height

                path.moveTo(startX, startY)

                // Draw wavy spline line across the canvas to simulate natural marble cracking/layering
                val segments = 8
                var currentX = startX
                var currentY = startY
                val dx = (endX - startX) / segments
                val dy = (endY - startY) / segments

                for (j in 1..segments) {
                    val nextX = startX + dx * j + (random.nextFloat() - 0.5f) * (width * 0.15f)
                    val nextY = startY + dy * j + (random.nextFloat() - 0.5f) * (height * 0.15f)
                    
                    // Control point for curve
                    val cx = (currentX + nextX) / 2 + (random.nextFloat() - 0.5f) * (width * 0.08f)
                    val cy = (currentY + nextY) / 2 + (random.nextFloat() - 0.5f) * (height * 0.08f)

                    path.quadraticTo(cx, cy, nextX, nextY)
                    currentX = nextX
                    currentY = nextY
                }

                val veinColor = veinColors[random.nextInt(veinColors.size)]
                val veinStrokeWidth = random.nextFloat() * 3.5f + 0.8f
                
                // Draw main vein
                drawPath(
                    path = path,
                    color = veinColor.copy(alpha = random.nextFloat() * 0.5f + 0.4f),
                    style = Stroke(width = veinStrokeWidth)
                )

                // Draw smaller feathering veins for realistic bookmatch details
                if (random.nextBoolean()) {
                    val featherPath = Path()
                    featherPath.moveTo(currentX, currentY)
                    featherPath.quadraticTo(
                        currentX + (random.nextFloat() - 0.5f) * 60f,
                        currentY + (random.nextFloat() - 0.5f) * 60f,
                        currentX + (random.nextFloat() - 0.5f) * 120f,
                        currentY + (random.nextFloat() - 0.5f) * 120f
                    )
                    drawPath(
                        path = featherPath,
                        color = veinColor.copy(alpha = random.nextFloat() * 0.3f + 0.1f),
                        style = Stroke(width = veinStrokeWidth * 0.5f)
                    )
                }
            }
        }
    }
}

package org.krishna.ninja

import android.hardware.Sensor
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

import ninja.composeapp.generated.resources.Res
import ninja.composeapp.generated.resources.compose_multiplatform
import kotlin.math.abs
import kotlin.math.abs as abs1

@Composable
@Preview
fun App() {
    MaterialTheme {

    }
}

@Composable
fun LiquidBox(
    modifier: Modifier = Modifier,
    initialHeightPercentage: Float = 0.5f // Initial height of the liquid (50% of box height)
) {
    // State for liquid height (between 0 and 1)
    var liquidHeightPercentage by remember { mutableStateOf(initialHeightPercentage) }

    // Animation to simulate movement
    val animatedHeight by animateFloatAsState(
        targetValue = liquidHeightPercentage,
        animationSpec = tween(durationMillis = 1000, easing = LinearEasing)
    )

    // Offset for the wave movement
    var waveOffset by remember { mutableStateOf(0f) }

    // Animate the wave offset
    LaunchedEffect(Unit) {
        while (true) {
            waveOffset += 5f // Adjust the speed of wave movement
            delay(16L) // Roughly 60 FPS
        }
    }

    Box(
        modifier = modifier
            .border(2.dp, Color.Black) // Box outline
            .background(Color.Transparent)
            .fillMaxSize()
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            // Calculate liquid height
            val liquidHeight = canvasHeight * (1 - animatedHeight)

            // Create path for the liquid
            val path = Path().apply {
                moveTo(0f, liquidHeight)

                // Create a wavy pattern
                val waveAmplitude = 20f
                val waveLength = 100f
                for (x in 0..canvasWidth.toInt() step waveLength.toInt()) {
                    quadraticTo(
                        x + waveLength / 2f,
                        liquidHeight + waveAmplitude * Math.sin((x + waveOffset) * Math.PI / waveLength).toFloat(),
                        x + waveLength,
                        liquidHeight
                    )
                }

                // Close the path to form the liquid
                lineTo(canvasWidth, canvasHeight) // Bottom-right corner
                lineTo(0f, canvasHeight) // Bottom-left corner
                close()
            }

            // Draw the liquid
            drawPath(
                path = path,
                color = Color.Cyan
            )
        }
    }
}


@Composable
fun LiquidSimulation(xAcceleration: Float, yAcceleration: Float) {
    var liquidX by remember { mutableFloatStateOf(0f) }
    var liquidY by remember { mutableFloatStateOf(0f) }

    // Screen size tracking
    val canvasSize = remember { mutableStateOf(Size(0f, 0f)) }

//    update liquid position based on acceleration
    LaunchedEffect(xAcceleration,yAcceleration) {
        liquidX = (liquidX + xAcceleration * 10).coerceIn(0f, canvasSize.value.width)
        liquidY = (liquidY - yAcceleration * 10).coerceIn(0f, canvasSize.value.height)
        liquidX += xAcceleration * 10
        liquidY += yAcceleration * 10
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        // Track canvas size
        canvasSize.value = Size(size.width, size.height)



        // Draw liquid effect
        drawLiquid(
            offsetX = liquidX,
            offsetY = liquidY,
            canvasSize = canvasSize.value
        )
    }

}


fun DrawScope.drawLiquid(offsetX: Float, offsetY: Float, canvasSize: Size) {
    val path = Path().apply {
        moveTo(0f, canvasSize.height / 3)

        // Generate wave-like pattern
        for (i in 0..canvasSize.width.toInt() step 100) {
            val waveHeight = abs(offsetX - i) / 5
            quadraticTo(
                i.toFloat(),
                (canvasSize.height / 2) - waveHeight,
                (i + 50).toFloat(),
                (canvasSize.height / 2)
            )
        }

        lineTo(canvasSize.width, canvasSize.height)
        lineTo(0f, canvasSize.height)
        close()
    }

    // Draw the liquid
    drawPath(
        path = path,
        color = Color.Blue,
        style = androidx.compose.ui.graphics.drawscope.Fill
    )
}


/*@Composable
fun MetaballsLiquidBox(modifier: Modifier = Modifier) {
    val canvasWidth = remember { mutableStateOf(0f) }
    val canvasHeight = remember { mutableStateOf(0f) }

    // Metaballs state: List of positions and radii
    val metaballs = remember {
        mutableStateListOf(
            Metaball(200f, 300f, 80f), // x, y, radius
            Metaball(400f, 500f, 100f),
            Metaball(600f, 400f, 90f),
        )
    }

    // Animate metaballs movement
    LaunchedEffect(Unit) {
        while (true) {
            metaballs.forEach { metaball ->
                metaball.x += (-1..1).random() * 5f
                metaball.y += (-1..1).random() * 5f

                // Keep metaballs within bounds
                metaball.x = metaball.x.coerceIn(0f, canvasWidth.value)
                metaball.y = metaball.y.coerceIn(0f, canvasHeight.value)
            }
            delay(16L) // Approx 60 FPS
        }
    }

    Canvas(modifier = modifier.onSizeChanged {
        canvasWidth.value = it.width.toFloat()
        canvasHeight.value = it.height.toFloat()
    }) {
        val path = Path()
        val threshold = 0.5f

        // Iterate over all points in the canvas to compute the metaballs field
        val resolution = 10f // Lower resolution for performance
        for (x in 0 until size.width.toInt() step resolution.toInt()) {
            for (y in 0 until size.height.toInt() step resolution.toInt()) {
                var fieldValue = 0f
                metaballs.forEach { metaball ->
                    // Field function: Contribution from each metaball
                    val dx = x - metaball.x
                    val dy = y - metaball.y
                    val distanceSquared = dx * dx + dy * dy
                    fieldValue += metaball.radius * metaball.radius / distanceSquared
                }

                // If field value exceeds the threshold, it's part of the metaball shape
                if (fieldValue > threshold) {
                    val radius = resolution / 2
                    path.addOval(
                        Rect(
                            left = x - radius,
                            top = y - radius,
                            right = x + radius,
                            bottom = y + radius
                        )
                    )
                }
            }
        }

        // Draw the metaballs as a smooth liquid shape
        drawPath(
            path = path,
            color = Color.Cyan,
            style = Fill
        )
    }
}

data class Metaball(var x: Float, var y: Float, var radius: Float)*/


@Composable
fun MetaballsLiquidBox(
    modifier: Modifier = Modifier,
    xAcceleration: Float,
    yAcceleration: Float
) {
    val liquidOffsetX = remember { mutableStateOf(0f) }
    val liquidOffsetY = remember { mutableStateOf(0f) }

    // Update the offset based on the accelerometer data
    liquidOffsetX.value = xAcceleration * 20f // Adjust sensitivity
    liquidOffsetY.value = yAcceleration * 20f // Adjust sensitivity

    Canvas(modifier = modifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        val path = Path()
        val metaballs = listOf(
            // Example metaballs; you can modify this list dynamically
            Metaball(canvasWidth / 2 + liquidOffsetX.value, canvasHeight / 2 + liquidOffsetY.value, 100f)
        )

        // Draw metaballs using the offsets
        metaballs.forEach { metaball ->
            path.addOval(
                Rect(
                    left = metaball.x - metaball.radius,
                    top = metaball.y - metaball.radius,
                    right = metaball.x + metaball.radius,
                    bottom = metaball.y + metaball.radius
                )
            )
        }

        drawPath(
            path = path,
            color = Color.Cyan,
            style = Fill
        )
    }
}

data class Metaball(val x: Float, val y: Float, val radius: Float)



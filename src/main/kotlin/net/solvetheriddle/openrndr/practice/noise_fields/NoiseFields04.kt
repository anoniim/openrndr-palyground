package net.solvetheriddle.openrndr.practice.noise_fields

import net.solvetheriddle.openrndr.Colors
import net.solvetheriddle.openrndr.Display
import net.solvetheriddle.openrndr.sketchSize
import org.openrndr.Program
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.DrawQuality
import org.openrndr.extra.color.presets.MEDIUM_VIOLET_RED
import org.openrndr.extra.noise.Random
import org.openrndr.extra.shapes.grid
import org.openrndr.math.Polar
import org.openrndr.math.Vector2
import org.openrndr.math.Vector3
import org.openrndr.shape.Rectangle

fun main() = NoiseFields04().main()
private class NoiseFields04 {

    val lengthNoiseOffset = Vector3(1000.0, 1000.0, 0.0)

    fun main() = application {
        configure {
            sketchSize(Display.LG_ULTRAWIDE)
        }
        program {
            backgroundColor = Colors.GREY1
            val cellSize = 30.0
            val maxLength = 80.0
            val maxWeight = 5.0
            val speed = .1
            val structure = 0.0005
            val positions = drawer.bounds.grid(cellSize, cellSize).flatten()
            val weights = MutableList(positions.size * 2) { 6.0 }
            val lineColors = List(100) { ColorRGBa.MEDIUM_VIOLET_RED.opacify(0.5) }

            extend {

                drawer.drawStyle.quality = DrawQuality.QUALITY

                val lines = positions
                    .flatMapIndexed { index: Int, it: Rectangle ->
                        val start = it.center
                        val noiseVector = (start * structure).vector3(z = seconds * speed)
                        weights[index * 2] = getWeight(noiseVector, maxWeight)
                        val length = getLength(noiseVector, maxLength)
                        val theta = Random.simplex(noiseVector) * 180.0
                        val end = start + Polar(theta).cartesian * length
                        listOf(start.vector3(z = 0.0), end.vector3(z = 0.0))
                    }
                drawer.lineSegments(lines, weights, lineColors)
            }
        }
    }

    private fun getLength(noiseVector: Vector3, maxLength: Double): Double {
        val lengthNoise = Random.simplex(noiseVector + lengthNoiseOffset) + 0.5
        return if (lengthNoise < 0.0) 0.0 else lengthNoise * maxLength
    }

    private fun getWeight(noiseVector: Vector3, maxWeight: Double): Double {
        val lengthNoise = Random.simplex(noiseVector + lengthNoiseOffset) + 0.5
        return if (lengthNoise < 0.0) 0.0 else lengthNoise * maxWeight
    }

    private fun Program.mouseControl(
        it: Rectangle,
        length: Double
    ): Vector2 {
        val mouseEnd = (mouse.position - drawer.bounds.center).normalized * length
        return it.center + mouseEnd
    }
}
package net.solvetheriddle.openrndr.experiments.noise_fields

import net.solvetheriddle.openrndr.Colors
import net.solvetheriddle.openrndr.Display
import net.solvetheriddle.openrndr.sketchSize
import org.openrndr.Program
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extra.color.presets.MEDIUM_VIOLET_RED
import org.openrndr.extra.noise.Random
import org.openrndr.extra.shapes.grid
import org.openrndr.math.Polar
import org.openrndr.math.Vector2
import org.openrndr.math.Vector3
import org.openrndr.shape.LineSegment
import org.openrndr.shape.Rectangle

fun main() = NoiseFields04().main()
private class NoiseFields04 {
    fun main() = application {
        configure {
            sketchSize(Display.LG_ULTRAWIDE)
        }
        program {
            backgroundColor = Colors.GREY1
            extend {

                drawer.stroke = ColorRGBa.MEDIUM_VIOLET_RED
                drawer.strokeWeight = 1.0
//                drawer.fill = ColorRGBa.PINK
                val cellSize = 40.0
                val maxLength = 100.0

                val lines = drawer.bounds.grid(cellSize, cellSize).flatten().map {
                    val noiseVector = (it.center * 0.001).vector3(z = seconds / 4)
                    val direction = Random.simplex(noiseVector)
                    val noiseOffset = Vector3(1000.0, 1000.0, 0.0)
                    val length = Random.simplex(noiseVector + noiseOffset) * maxLength
                    val end = it.center + Polar(direction * 180.0).cartesian * length
//                    val end = mouseControl(it, length)
                    LineSegment(it.center, end)
                }
                drawer.lineSegments(lines)
            }
        }
    }

    private fun Program.mouseControl(
        it: Rectangle,
        length: Double
    ): Vector2 {
        val mouseEnd = (mouse.position - drawer.bounds.center).normalized * length
        return it.center + mouseEnd
    }
}
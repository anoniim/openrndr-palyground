package net.solvetheriddle.openrndr.experiments.noise_fields

import net.solvetheriddle.openrndr.Colors
import net.solvetheriddle.openrndr.Display
import net.solvetheriddle.openrndr.sketchSize
import org.openrndr.Program
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extra.noclear.NoClear
import org.openrndr.extra.noise.Random
import org.openrndr.math.Polar
import org.openrndr.math.Vector2
import org.openrndr.shape.Rectangle

fun main() = NoiseFields02().main()
class NoiseFields02 {

    // config
    private val bgColor = Colors.BG_GREY
    private val colorChangePeriod = 5.0
    private val zoom = 0.05 // 0.05 .. 0.0005
    private val speed = 1 // 1..10..100
    private val changeColors = false

    fun main() = application {
        configure {
            sketchSize(Display.LG_ULTRAWIDE)
        }
        program {
            backgroundColor = bgColor
            extend(NoClear())

            val colors = LineColors(colorChangePeriod)

            extend {
                drawer.stroke = null

                val currentColor = getCurrentColor(colors)

                // config [helper] draw next color
//                drawNextRandomColor(colors.next)

                // draw lines
                drawer.fill = currentColor.opacify(0.01)
                drawer.points(generateNoisyLines(100, drawer.bounds, seconds))
                // erase old lines
                drawer.fill = bgColor.opacify(0.05)
                drawer.points(generateNoisyLines(100, drawer.bounds, seconds - 3))
            }
        }
    }

    class LineColors(private val colorChangePeriod: Double) {
        var current: ColorRGBa = Colors.random
        var next: ColorRGBa = Colors.random
        fun change(seconds: Double) {
            if (seconds % colorChangePeriod < 0.01) {
                current = next
                next = Colors.random
            }
        }
    }

    private fun Program.getCurrentColor(colors: LineColors): ColorRGBa {
        return if (changeColors) {
            colors.change(seconds)
            colors.current.mix(colors.next, seconds % colorChangePeriod / colorChangePeriod)
        } else {
            colors.current
        }
    }

    private fun Program.drawNextRandomColor(nextColor: ColorRGBa) {
        if (seconds % colorChangePeriod < 0.01) {
            drawer.fill = nextColor
            drawer.circle(Vector2(50.0 * (seconds / colorChangePeriod), 50.0), 30.0)
        }
    }

    private fun generateNoisyLines(count: Int, bounds: Rectangle, seconds: Double): List<Vector2> {
        return List(count) {
            NoisyLine(Random.point(bounds), seconds)
        }.flatten()
    }

    private inner class NoisyLine(startingPoint: Vector2, seconds: Double) : ArrayList<Vector2>() {
        init {
            addAll(generateSequence(startingPoint) {
                it + Polar(360 * Random.simplex(it.vector3(z = seconds * speed) * zoom)).cartesian
            }.take(500).toList())
        }
    }
}

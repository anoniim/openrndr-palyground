package net.solvetheriddle.openrndr.experiments.noise_fields

import net.solvetheriddle.openrndr.Colors
import net.solvetheriddle.openrndr.Display
import net.solvetheriddle.openrndr.sketchSize
import org.openrndr.Program
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extra.color.presets.OLIVE
import org.openrndr.extra.noclear.NoClear
import org.openrndr.extra.noise.Random
import org.openrndr.math.Polar
import org.openrndr.math.Vector2

fun main() = NoiseFields01().main()
private class NoiseFields01 {
    fun main() = application {
        configure {
            sketchSize(Display.LG_ULTRAWIDE)
        }
        program {
            backgroundColor = Colors.BG_GREY
            extend(NoClear())
            extend {
                drawer.fill = ColorRGBa.OLIVE.opacify(0.01)
                drawer.points(generateNoisyLines(100))
            }
        }
    }

    private val ZOOM = 0.03

    inner class NoisyLine(startingPoint: Vector2, seconds: Double) : ArrayList<Vector2>() {
        init {
            addAll(generateSequence(startingPoint) {
                it + Polar(270 * Random.simplex(it.vector3(z = seconds) * ZOOM)).cartesian
            }.take(500).toList())
        }
    }

    fun Program.generateNoisyLines(count: Int): List<Vector2> {
        return List(count) {
            NoisyLine(Random.point(drawer.bounds), seconds)
        }.flatten()
    }
}
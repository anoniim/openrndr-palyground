package net.solvetheriddle.openrndr.practice.noise_fields

import net.solvetheriddle.openrndr.Colors
import net.solvetheriddle.openrndr.Display
import net.solvetheriddle.openrndr.sketchSize
import org.openrndr.Program
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extra.noise.Random
import org.openrndr.math.Polar
import org.openrndr.math.Vector2
import org.openrndr.shape.*

fun main() = NoiseFields03().main()
class NoiseFields03 {

    // config
    private val bgColor = Colors.BG_GREY
    private val colorChangePeriod = 5.0
    private val zoom = 0.001 // 0.003/0.001
    private val speed = 30 // 10/30
    private val changeColors = true
    private val lineLength = 3500

    fun main() {
        application {
            configure {
                sketchSize(Display.LG_ULTRAWIDE)
            }
            program {
                backgroundColor = bgColor

                val colors = ChangingColors(colorChangePeriod)
                val lines = mutableListOf<NoisyLine>()

                mouse.buttonDown.listen {
                    window.requestDraw()
                }

                extend {
                    val currentColor = colors.getCurrentColor(seconds, colors)

                    // config [helper] draw next color
    //                colors.drawNextRandomColor()

//                    lines.add(NoisyLine(generateContour(Vector2(0.0, 3 * drawer.height / 4.0), lineLength, seconds * speed, zoom, 180)))
                    lines.add(NoisyLine(generateContour(drawer.bounds.center, lineLength, seconds * speed, zoom, 360)))
                    lines.forEach {
                        it.update()

                        // draw lines
                        drawer.fill = null
                        drawer.stroke = currentColor.opacify(it.alpha)
                        drawer.strokeWeight = 1.0
                        drawer.contour(it.shape)
                    }
                    lines.removeIf { it.isNotVisible() }
                }
            }
        }
    }

    private fun generateContour(origin: Vector2, lineLength: Int = 4000, z: Double, zoom: Double, curviness: Int): ShapeContour {
        return contour {
            val sequence = generateSequence(origin) {
                it + (Polar(curviness * Random.simplex(it.vector3(z = z) * zoom))).cartesian
            }
            moveTo(sequence.first())
            sequence.take(lineLength).toList().forEach {
                lineTo(it)
            }
        }
    }

    private inner class NoisyLine(val shape: ShapeContour) {
        var alpha = 0.0
        var showingUp = true

        fun update() {
            if (showingUp) {
                alpha += 0.1
            } else {
                alpha -= 0.008
            }
            if (showingUp && alpha > 1.0) showingUp = false
        }

        fun isNotVisible(): Boolean {
            return alpha < 0.0
        }
    }

    private inner class ChangingColors(private val colorChangePeriod: Double) {
        var first: ColorRGBa = Colors.random
        var second: ColorRGBa = Colors.random
        var recentlyChanged = false
        private fun change(seconds: Double) {
            if (!recentlyChanged && seconds % colorChangePeriod < 0.2) {
                first = second
                second = Colors.random
                recentlyChanged = true
            } else if (seconds % colorChangePeriod > 0.5) {
                recentlyChanged = false
            }
        }

        fun getCurrentColor(seconds: Double, colors: ChangingColors): ColorRGBa {
            return if (changeColors) {
                colors.change(seconds)
                colors.first.mix(colors.second, seconds % colorChangePeriod / colorChangePeriod)
            } else {
                colors.first
            }
        }

        context(Program)
        fun drawNextRandomColor() {
            drawer.fill = second
            drawer.circle(Vector2(50.0 * (seconds / colorChangePeriod), 50.0), 30.0)
        }
    }
}

package net.solvetheriddle.openrndr.experiments.noise_fields

import net.solvetheriddle.openrndr.Colors
import net.solvetheriddle.openrndr.Display
import net.solvetheriddle.openrndr.sketchSize
import org.openrndr.Program
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.Drawer
import org.openrndr.extra.noise.Random
import org.openrndr.math.Polar
import org.openrndr.math.Vector2
import org.openrndr.shape.*

fun main() = NoiseFields03().main()
class NoiseFields03 {

    // config
    private val bgColor = Colors.BG_GREY
    private val colorChangePeriod = 5.0
    private val zoom = 0.003 // 0.05 .. 0.0005
    private val speed = 10 // 1..10..100
    private val changeColors = true

    fun main() = application {
        configure {
            sketchSize(Display.LG_ULTRAWIDE)
        }
        program {
            backgroundColor = bgColor

            val colors = ChangingColors(colorChangePeriod)
            val lines = mutableListOf<NoisyLine>()

//            extend(NoClear())
//            window.presentationMode = PresentationMode.MANUAL
            mouse.buttonDown.listen {
                window.requestDraw()
            }

            extend {
                val currentColor = colors.getCurrentColor(seconds, colors)

                // config [helper] draw next color
//                colors.drawNextRandomColor()

                lines.add(NoisyLine(generateContour(Vector2(0.0, drawer.height / 2.0), 4000, seconds * speed, zoom)))
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

    private fun generateContour(origin: Vector2, lineLength: Int = 4000, z: Double, zoom: Double): ShapeContour {
        return contour {
            val sequence = generateSequence(origin) {
                it + (Polar(180 * Random.simplex(it.vector3(z = z) * zoom))).cartesian
            }
            moveTo(sequence.first())
            sequence.take(lineLength).toList().forEach {
                lineTo(it)
            }
        }
    }

    private inner class NoisyLine(val shape: ShapeContour) {
        var alpha = 1.0

        fun update() {
            alpha -= 0.008
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
            } else if (seconds % colorChangePeriod > 0.5){
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

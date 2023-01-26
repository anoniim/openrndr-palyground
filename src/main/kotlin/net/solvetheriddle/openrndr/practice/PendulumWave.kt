package net.solvetheriddle.openrndr.practice

import net.solvetheriddle.openrndr.Colors
import net.solvetheriddle.openrndr.Display
import net.solvetheriddle.openrndr.sketchSize
import org.openrndr.Program
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.math.Vector2
import kotlin.math.sin

fun main() = PendulumWave().main()
class PendulumWave {
    fun main() = application {
        configure {
            sketchSize(Display.LG_ULTRAWIDE_LEFT)
        }
        program {
            val circleSize = 30.0
            val numOfCircles = 50
            val direction = Direction.VERTICAL
            val margin = 0.7 // .3 .. .7
            val circles = generateCircles(numOfCircles, direction)
            val maxOffset = getMaxOffset(direction, margin)

            extend {
                translateForDirection(direction)
                drawer.clear(Colors.GREY1)
                drawer.circles {
                    circles.forEach {
                        it.update(seconds / 4.0, maxOffset)
                        fill = it.color
                        circle(it.position, circleSize)
                    }
                }
            }
        }
    }

    private fun Program.generateCircles(numOfCircles: Int, direction: Direction): List<PendulumCircle> {
        val segment = getSegment(numOfCircles, direction)
        val color1 = Colors.random
        val color2 = Colors.random
        return List(numOfCircles) {
            val staticPosition = (it + 1) * segment
            val circleColor = color1.mix(color2, it * (1.0/numOfCircles))
            PendulumCircle(numOfCircles - it, staticPosition, direction, color = circleColor)
        }
    }

    private fun Program.getSegment(numOfCircles: Int, direction: Direction) = if (direction == Direction.VERTICAL) {
        drawer.width / (numOfCircles + 1.0)
    } else {
        drawer.height / (numOfCircles + 1.0)
    }

    private fun Program.getMaxOffset(direction: Direction, margin: Double) =
        if (direction == Direction.VERTICAL) {
            drawer.bounds.height / 2 * margin
        } else {
            drawer.bounds.width / 2 * margin
        }

    private fun Program.translateForDirection(direction: Direction) {
        if (direction == Direction.VERTICAL) {
            drawer.translate(0.0, drawer.height / 2.0)
        } else {
            drawer.translate(drawer.width / 2.0, 0.0)
        }
    }

    class PendulumCircle(
        private val speedCoef: Int,
        private val staticPosition: Double,
        private val direction: Direction = Direction.VERTICAL,
        val color: ColorRGBa,
    ) {

        var position = Vector2(0.0, staticPosition)

        fun update(seconds: Double, maxOffset: Double) {
            position = if (direction == Direction.VERTICAL) {
                Vector2(staticPosition, sin(seconds * speedCoef) * maxOffset)
            } else {
                Vector2(sin(seconds * speedCoef) * maxOffset, staticPosition)
            }
        }
    }

    enum class Direction {
        VERTICAL,
        HORIZONTAL
    }
}
package net.solvetheriddle.openrndr.opticalillusions

import net.solvetheriddle.openrndr.Colors
import net.solvetheriddle.openrndr.Display
import net.solvetheriddle.openrndr.sketchSize
import org.openrndr.Program
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.isolated
import org.openrndr.extra.color.presets.CHARTREUSE
import org.openrndr.extra.color.presets.LINEN
import org.openrndr.math.Polar
import org.openrndr.math.Vector2
import org.openrndr.shape.LineSegment
import kotlin.math.sin

fun main() = application {
    configure {
        sketchSize(Display.MACBOOK_AIR)
    }

    program {

        val numOfLines = 24
        val angleSegment = 360.0 / numOfLines
        val length = 300
        val centerMargin = 150.0

        val lines = List(numOfLines) {
            val angle = angleSegment * it
            val start = Vector2.fromPolar(Polar(angle, centerMargin))
            val end = Vector2.fromPolar(Polar(angle, centerMargin + length))
            val arrowDirection = if (it % 2 == 0) Arrow.ArrowDirection.IN else Arrow.ArrowDirection.OUT
            Arrow(start, end, angle, arrowDirection)
        }

        extend {
            drawer.clear(Colors.BG_GREY)
            drawer.translate(drawer.bounds.center)

            lines.forEach {
                it.updateDistance(seconds)
                it.draw()
            }
        }
    }
}

private class Arrow(
    val start: Vector2,
    val end: Vector2,
    val angle: Double,
    val arrowDirection: ArrowDirection,
) {

    private var arrowLength = 25.0
    private var arrowAngleProgress = 0.0

    fun updateDistance(seconds: Double) {
        arrowAngleProgress = (sin(seconds) + 1.0) / 2.0
    }

    context(Program)
    fun draw() {
        drawer.strokeWeight = 3.0
        drawLine()
        drawArrows()
    }

    context(Program)
    private fun drawLine() {
        drawer.stroke = ColorRGBa.CHARTREUSE
        drawer.lineSegment(LineSegment(start, end))
    }

    context(Program)
    private fun drawArrows() {
        drawer.stroke = ColorRGBa.LINEN
        drawArrow(start, arrowDirection, angle)
        drawArrow(end, arrowDirection, angle + 180.0)
    }

    context(Program)
    private fun drawArrow(origin: Vector2, arrowDirection: ArrowDirection, lineEndAngle: Double) {
        drawer.isolated {
            translate(origin)
            rotate(lineEndAngle)
            val directionFactor = if (arrowDirection == ArrowDirection.IN) 1.0 else -1.0
            val firstAngle = arrowDirection.firstAngle + arrowAngleProgress * 90 * directionFactor
            val secondAngle = arrowDirection.secondAngle - arrowAngleProgress * 90 * directionFactor
            lineSegment(LineSegment(Vector2.ZERO, Vector2.fromPolar(Polar(firstAngle, arrowLength))))
            lineSegment(LineSegment(Vector2.ZERO, Vector2.fromPolar(Polar(secondAngle, arrowLength))))
        }
    }

    enum class ArrowDirection(
        val firstAngle: Double,
        val secondAngle: Double
    ) {
        IN(45.0, 315.0),
        OUT(135.0, 225.0),
    }
}

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
import org.openrndr.panel.ControlManager
import org.openrndr.panel.elements.Range
import org.openrndr.panel.elements.slider
import org.openrndr.panel.layout
import org.openrndr.shape.LineSegment

fun main() = application {
    configure {
        sketchSize(Display.MACBOOK_AIR)
    }

    program {

        var halfDistance = 100.0
        val maxHalfDistance = 300.0

        extend(ControlManager()) {
            layout {
                slider {
                    label = "Distance"
                    range = Range(0.0, maxHalfDistance)
                    value = halfDistance
                    precision = 0
                    events.valueChanged.listen {
                        halfDistance = it.newValue
                    }
                }
            }
        }

        val length = 600
        val halfLength = length / 2.0
        val lines = listOf(
            Line(halfLength, -1.0, Line.ArrowDirection.IN),
            Line(halfLength, 1.0, Line.ArrowDirection.OUT),
        )

        extend {
            drawer.clear(Colors.BG_GREY)
            drawer.translate(drawer.bounds.center)

            lines.forEach {
                it.updateDistance(halfDistance)
                it.draw()
            }
        }
    }
}

private class Line(
    val halfLength: Double,
    val side: Double,
    val arrowDirection: ArrowDirection,
) {
    lateinit var start: Vector2
    lateinit var end: Vector2

    private var arrowLength = 50.0

    fun updateDistance(halfDistance: Double) {
        start = Vector2(-halfLength, side * halfDistance)
        end = Vector2(halfLength, side * halfDistance)
        arrowLength = 50.0 // - halfDistance / 10.0
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
        drawArrow(start, arrowDirection, 0.0)
        drawArrow(end, arrowDirection, 180.0)
    }

    context(Program)
    private fun drawArrow(origin: Vector2, arrowDirection: ArrowDirection, lineEndAngle: Double) {
        drawer.isolated {
            translate(origin)
            rotate(lineEndAngle)
            lineSegment(LineSegment(Vector2.ZERO, Vector2.fromPolar(Polar(arrowDirection.firstAngle, arrowLength))))
            lineSegment(LineSegment(Vector2.ZERO, Vector2.fromPolar(Polar(arrowDirection.secondAngle, arrowLength))))
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

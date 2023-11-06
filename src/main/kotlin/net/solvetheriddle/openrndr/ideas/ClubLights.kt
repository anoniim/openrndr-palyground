package net.solvetheriddle.openrndr.ideas

import net.solvetheriddle.openrndr.Colors
import net.solvetheriddle.openrndr.Display
import net.solvetheriddle.openrndr.sketchSize
import org.openrndr.Program
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extra.color.presets.DARK_GREEN
import org.openrndr.extra.noise.uniform
import org.openrndr.math.Vector2
import org.openrndr.shape.Rectangle
import org.openrndr.shape.contour
import kotlin.random.Random

fun main() = application {
    configure {
        sketchSize(Display.MACBOOK_AIR)
    }
    program {

        val margin = 20.0
        val sourcePoints = sourcePoints(drawer.bounds, margin)

        extend {
            drawer.clear(Colors.BG_GREY)
            drawer.translate(drawer.width / 2.0, 0.0)

            drawer.fill = ColorRGBa.DARK_GREEN
            drawer.stroke = ColorRGBa.DARK_GREEN
            sourcePoints.forEach {
                it.draw()
            }
        }
    }
}

private class LightBeam(
    val source: Vector2,
    private val drawerBounds: Rectangle
) {
    val end = generateEnd()

    private fun generateEnd(): Vector2 {
        val randomPoint = drawerBounds.uniform() - source
        return randomPoint.copy(x = randomPoint.x - drawerBounds.width / 2.0)
    }

    context(Program)
    fun draw() {
        drawer.lineSegment(source, end)
//                drawer.shape(contour {
//
//                })
    }

}

private fun sourcePoints(bounds: Rectangle, margin: Double): List<LightBeam> {
    val contour = contour {
        moveTo(0.0, 0.0 - margin)
        lineTo(bounds.width / 2.0 + margin, 0.0 - margin)
        lineTo(bounds.width / 2.0 + margin, bounds.height + 2 * margin)
        lineTo(0.0, bounds.height + 2 * margin)
    }
    return (contour.equidistantPositions(20) + contour.equidistantPositions(20)
        .map { it.copy(x = -it.x) })
        .map { LightBeam(it, bounds) }
}

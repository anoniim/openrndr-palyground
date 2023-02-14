package net.solvetheriddle.openrndr.ideas

import net.solvetheriddle.openrndr.Display
import net.solvetheriddle.openrndr.sketchSize
import net.solvetheriddle.openrndr.tools.Move
import net.solvetheriddle.openrndr.tools.Movie
import org.openrndr.Program
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extra.color.presets.DARK_GOLDEN_ROD
import org.openrndr.math.Polar
import org.openrndr.math.Vector2
import org.openrndr.shape.LineSegment

fun main() = application {
    configure {
        sketchSize(Display.LG_SQUARE_LEFT)
    }
    program {
        val armLength = 400.0
        val numOfArms = 20
        val revolutionLength = 60 * 5
        var tickPointer = 0
        val tickAlpha = 3.0
        val decay = 0.015

        val arms = List(numOfArms) {
            val lineStart = Vector2(width/(numOfArms + 2.0) * (it + 1), height/4.0)
            val lineEnd = Vector2(width/(numOfArms + 2.0) * (it + 1), 3 * height/4.0)
            Arm(lineStart, lineEnd, tickAlpha, decay)
        }

        val movie = Movie().apply {
            append(Move(revolutionLength) { frameCount ->
                val ticksPerFrame = if (numOfArms < revolutionLength) ((revolutionLength - (1/decay)) / numOfArms).toInt() else 1
                if (frameCount % ticksPerFrame == 0 && tickPointer < numOfArms - 1) arms[tickPointer++].show()
                arms.forEach {
                    it.update(frameCount)
                    it.draw()
                }
            })
        }

        extend {
//            drawer.translate(drawer.bounds.center)
            movie.play()
        }
    }

}

private class Arm(
    start: Vector2,
    end: Vector2,
    private val tickAlpha: Double = 2.0,
    private val decay: Double = 0.01,
) {

    private val lineSegment = LineSegment(start, end)
    private var alpha = 0.0

    fun show() {
        alpha = tickAlpha
    }

    fun update(frameCount: Int) {
        if (alpha > 0) alpha -= decay
    }

    context(Program)
    fun draw() {
        drawer.strokeWeight = 4.0
        drawer.stroke = ColorRGBa.DARK_GOLDEN_ROD.opacify(alpha)
        drawer.lineSegment(lineSegment)
    }
}

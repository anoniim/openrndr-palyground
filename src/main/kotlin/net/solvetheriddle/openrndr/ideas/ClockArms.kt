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
        val numOfArms = 50
        val revolutionLength = 60 * 4
        var tickPointer = 3 * numOfArms / 4
        val tickAlpha = 2.0

        val arms = List(numOfArms) {
            val lineEnd = Polar(360.0 / numOfArms * it, armLength).cartesian
            Arm(Vector2.ZERO, lineEnd, tickAlpha, 0.01)
        }

        val movie = Movie().apply {
            append(Move(revolutionLength) { frameCount ->
                println(frameCount)
                val ticksPerFrame = if (numOfArms < revolutionLength) revolutionLength / numOfArms else 1
                if (frameCount % ticksPerFrame == 0) arms[tickPointer++ % numOfArms].show()
                arms.forEach {
                    it.update(frameCount)
                    it.draw()
                }
            })
        }

        extend {
            drawer.translate(drawer.bounds.center)
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
        drawer.strokeWeight = 3.0
        drawer.stroke = ColorRGBa.DARK_GOLDEN_ROD.opacify(alpha)
        drawer.lineSegment(lineSegment)
    }
}

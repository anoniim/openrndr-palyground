package net.solvetheriddle.openrndr.ideas.shulze

import net.solvetheriddle.openrndr.Display
import net.solvetheriddle.openrndr.sketchSize
import net.solvetheriddle.openrndr.tools.Movie
import net.solvetheriddle.openrndr.tools.Scene
import org.openrndr.Program
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.isolated
import org.openrndr.extra.color.presets.ORANGE
import org.openrndr.extra.color.presets.SILVER
import org.openrndr.extra.videoprofiles.ProresProfile
import org.openrndr.ffmpeg.ScreenRecorder
import org.openrndr.math.Polar
import org.openrndr.math.Vector2
import org.openrndr.math.Vector3
import org.openrndr.shape.LineSegment
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

fun main() = application {
    configure {
        sketchSize(Display.LG_SQUARE_LEFT)
    }
    program {

//        extend(ScreenRecorder().apply {
//            profile = ProresProfile()
//        })

        val numOfLines = 500

        val phase1Length = numOfLines / 3
        val movie = Movie(loop = false).apply {
            append(Scene1(phase1Length, numOfLines, 150.0, 400.0))
            append(Scene2(30000, numOfLines, 150.0, 400.0))
        }

        extend {
            drawer.clear(ColorRGBa.BLACK)
            drawer.stroke = ColorRGBa.SILVER
            drawer.isolated {
                translate(drawer.bounds.center)
                rotate(-90.0)
                translate(-300.0, 0.0)
                strokeWeight = 2.0

//                lineSegments(lines)

                movie.play()
            }
        }
    }
}

private class Scene1(
    lengthFrames: Int,
    val numOfLines: Int,
    val innerRadius: Double,
    val outerRadius: Double
) : Scene(lengthFrames) {

    override fun Program.sceneFunction(frameCount: Int) {
        val (lines, colors) = generateLines(numOfLines, innerRadius, outerRadius, frameCount)
        val weights = listOf(2.0)
        drawer.lineSegments(lines, weights, colors)
    }

    private fun generateLines(numOfLines: Int, innerRadius: Double, outerRadius: Double, frames: Int): Pair<List<Vector3>, List<ColorRGBa>> {
        val angleSegment = 360.0 / numOfLines
        val lineSegments = List(numOfLines) {
            val f = (it.toDouble() / numOfLines - 0.5) * 2 * PI
            val time = 1000.0
            val offset = sin(f * time) * 100.0
            val start = Vector2.fromPolar(Polar(it * angleSegment, innerRadius + offset * f)).vector3()
            val end = Vector2.fromPolar(Polar(it * angleSegment, outerRadius + offset * f)).vector3()
            if (frames > it / 3.1) {
                listOf(start, end)
            } else {
                listOf(start, start)
            }
        }.flatten()
        val colors = lineSegments.map {
            val factor = it.y / -550.0
            ColorRGBa.RED.mix(ColorRGBa.ORANGE, factor)
        }
        return Pair(lineSegments, colors)
    }
}

private class Scene2(
    lengthFrames: Int,
    val numOfLines: Int,
    val innerRadius: Double,
    val outerRadius: Double
) : Scene(lengthFrames) {

    override fun Program.sceneFunction(frameCount: Int) {
        val (lines, colors) = generateLines(numOfLines, innerRadius, outerRadius, frameCount)
        val weights = listOf(2.0)
        drawer.lineSegments(lines, weights, colors)
    }

    private fun generateLines(numOfLines: Int, innerRadius: Double, outerRadius: Double, frames: Int): Pair<List<Vector3>, List<ColorRGBa>> {
        val angleSegment = 360.0 / numOfLines
        val speed = 1 * (cos(PI + frames / 460.0) / 2.0 + 0.5)
        val speedReduction = 20 - frames / 30.0
        val lineSegments = List(numOfLines) {
            val f = (it.toDouble() / numOfLines - 0.5) * 2 * PI
            val time = 1000 + (frames / 50.0 + speed) / speedReduction
            val offset = sin(f * time) * 100.0
            val start = Vector2.fromPolar(Polar(it * angleSegment, innerRadius + offset * f)).vector3()
            val end = Vector2.fromPolar(Polar(it * angleSegment, outerRadius + offset * f)).vector3()
            listOf(start, end)
        }.flatten()
        val colors = lineSegments.map {
            val factor = it.x / 350.0
            ColorRGBa.RED.mix(ColorRGBa.ORANGE, factor)
        }
        return Pair(lineSegments, colors)
    }
}

context(Program)
private fun generateBaseLines(numOfLines: Int, innerRadius: Double, outerRadius: Double): List<LineSegment> {
    val angleSegment = 360.0 / numOfLines
    val speed = 1 + (sin(seconds / 36.0) / 2.0 + 0.5)
    return List(numOfLines) {
        val f = (it.toDouble() / numOfLines - 0.5) * 2 * PI
        val time = if (seconds < numOfLines / 80.0) {
            1000.0
        } else {
            1000 + seconds / 2.0 * speed
        }
        val offset = sin(f * time) * 100.0
        val start = Vector2.fromPolar(Polar(it * angleSegment, innerRadius + offset * f))
        val end = Vector2.fromPolar(Polar(it * angleSegment, outerRadius + offset * f))
        if (seconds > it / 80.0) {
            LineSegment(start, end)
        } else {
            LineSegment(start, start)
        }
    }
}



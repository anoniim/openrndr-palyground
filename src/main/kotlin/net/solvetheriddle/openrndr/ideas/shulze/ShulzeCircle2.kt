package net.solvetheriddle.openrndr.ideas.shulze

import net.solvetheriddle.openrndr.Display
import net.solvetheriddle.openrndr.sketchSize
import net.solvetheriddle.openrndr.tools.Movie
import net.solvetheriddle.openrndr.tools.Scene
import org.openrndr.Program
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.isolated
import org.openrndr.extra.color.presets.DARK_GOLDEN_ROD
import org.openrndr.extra.color.presets.SILVER
import org.openrndr.extra.videoprofiles.ProresProfile
import org.openrndr.ffmpeg.ScreenRecorder
import org.openrndr.math.Polar
import org.openrndr.math.Vector2
import org.openrndr.shape.LineSegment
import org.openrndr.shape.contour

// config
const val RECORDING = false
fun main() = application {
    configure {
        sketchSize(Display.MACBOOK_AIR)
    }
    program {

        if (RECORDING) {
            extend(ScreenRecorder().apply {
                profile = ProresProfile()
            })
        }

        val numOfLines = 100

        val phase1Length = 60 * 4
        val lineLength = 250.0
        val innerRadius = 200.0
        val secondaryRadius = 250.0
        val secondaryCenter = Vector2(0.0, 200.0)
        val movie = Movie(loop = true).apply {
            append(EntryScene(phase1Length, numOfLines, innerRadius, lineLength))
            append(InnerEntryScene(phase1Length, numOfLines, innerRadius, lineLength, secondaryCenter, secondaryRadius))
//            append(StaticScene(phase1Length * 10, numOfLines, innerRadius, lineLength, secondaryCenter, secondaryRadius))
        }

        extend {
            drawer.clear(ColorRGBa.BLACK)
            drawer.stroke = ColorRGBa.SILVER
            drawer.isolated {
                translate(drawer.bounds.center)
                rotate(-90.0)
//                translate(-300.0, 0.0)
                strokeWeight = 2.0

                movie.play()
            }
        }
    }
}

private class EntryScene(
    lengthFrames: Int,
    val numOfLines: Int,
    val innerRadius: Double,
    val lineLength: Double
) : Scene(lengthFrames) {

    override fun Program.sceneFunction(frameCount: Int) {
        val progress = frameCount.toDouble() / lengthFrames
        val lines = generateBaseLines(numOfLines, innerRadius, lineLength, progress)
        drawer.lineSegments(lines)
    }
}

private class InnerEntryScene(
    lengthFrames: Int,
    val numOfLines: Int,
    val innerRadius: Double,
    val lineLength: Double,
    val secondaryCenter: Vector2,
    val secondaryRadius: Double,
) : Scene(lengthFrames) {

    override fun Program.sceneFunction(frameCount: Int) {
        val baseLines = generateBaseLines(numOfLines, innerRadius, lineLength, 1.0)
        drawer.lineSegments(baseLines)

        drawer.stroke = ColorRGBa.DARK_GOLDEN_ROD
        drawer.strokeWeight = 4.0
        val progress = frameCount.toDouble() / lengthFrames
        val secondaryLines = generateSecondaryLines(numOfLines, innerRadius, lineLength, secondaryCenter, secondaryRadius, progress)
        drawer.lineSegments(secondaryLines)
    }
}

private class StaticScene(
    lengthFrames: Int,
    val numOfLines: Int,
    val innerRadius: Double,
    val lineLength: Double,
    val secondaryCenter: Vector2,
    val secondaryRadius: Double,
) : Scene(lengthFrames) {

    override fun Program.sceneFunction(frameCount: Int) {
        val baseLines = generateBaseLines(numOfLines, innerRadius, lineLength, 1.0)
        drawer.lineSegments(baseLines)

        drawer.stroke = ColorRGBa.DARK_GOLDEN_ROD
        drawer.strokeWeight = 4.0
        val secondaryLines = generateSecondaryLines(numOfLines, innerRadius, lineLength, secondaryCenter, secondaryRadius, 1.0)
        drawer.lineSegments(secondaryLines)

        val c = contour {
            moveTo(secondaryLines.first().end)
            secondaryLines.map { it.end }
                .forEach { lineTo(it) }
        }
        drawer.contour(c)

        drawer.circle(secondaryCenter, 5.0)
    }
}

private fun generateBaseLines(numOfLines: Int, innerRadius: Double, lineLength: Double, progress: Double): List<LineSegment> {
    val angleSegment = 360.0 / numOfLines
    return List(numOfLines) {
        val start = Polar(it * angleSegment, innerRadius).cartesian
        val end = Polar(it * angleSegment, innerRadius + lineLength).cartesian
        val currentEnd = start + (end - start) * progress
        LineSegment(start, currentEnd)
    }
}
private fun generateSecondaryLines(numOfLines: Int, innerRadius: Double, lineLength: Double, centerPoint: Vector2, secondaryRadius: Double, progress: Double): List<LineSegment> {
    val angleSegment = 360.0 / numOfLines
    return List(numOfLines) {
        val start = Polar(it * angleSegment, innerRadius).cartesian
        val end = Polar(it * angleSegment, innerRadius + lineLength).cartesian
        fun calculateEnd(secondaryRadius: Double): Vector2 {
            var i = 0.0
            var distance = centerPoint.distanceTo(end * i)
            while(distance > 0.0 && distance < secondaryRadius && i < 1.0) {
                i += 0.001
                distance = centerPoint.distanceTo(end * i)
            }
            return (end - start) * i
        }
        val currentEnd = start + calculateEnd(secondaryRadius) * progress
        LineSegment(start, currentEnd)
    }
}



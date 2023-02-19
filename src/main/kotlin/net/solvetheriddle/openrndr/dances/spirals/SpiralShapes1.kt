package net.solvetheriddle.openrndr.dances.spirals

import net.solvetheriddle.openrndr.Colors
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extra.color.presets.*
import org.openrndr.extra.videoprofiles.ProresProfile
import org.openrndr.ffmpeg.ScreenRecorder
import org.openrndr.math.Polar
import org.openrndr.math.Vector2
import org.openrndr.shape.contour
import kotlin.math.*

fun main() = application {
    configure {
//        sketchSize(Display.LG_ULTRAWIDE)
        width = 1080
        height = 1080
    }
    program {

        // config recording
        val recording = false

        if (recording) {
            extend(ScreenRecorder().apply {
            frameRate = 60
                profile = ProresProfile()
            })
        }

        // config parameters
        val margin = 100.0
        val dimension = min(width, height) - margin
        val radiusFactor = dimension / 5.0
        val radiusSpeed = 0.02
        val minRadius = dimension / 8.0
        val centerFactor = dimension / 2.0
        val centerSpeed = 0.02
        val resolution = .05
        val speed = 5

        var time = 0.0
        val points = mutableListOf<Vector2>()
        repeat((2 * PI * 1008).toInt()) {
            time += resolution
            val radius = minRadius + ((cos(PI + time * radiusSpeed) + 1) * radiusFactor)
            val center = Vector2.ZERO + Vector2(0.0, sin(time * centerSpeed) * centerFactor)
            val newPoint = getNewPoint(center, radius, time * 100)
            points.add(newPoint)
        }
        time = 0.0
        var pointsToDrawCount = 0
        var isAppearing = true

        extend {
            time += .00089 // config tweak to slow down when looping
            drawer.clear(Colors.BG_GREY)
            drawer.clear(ColorRGBa.BLACK)
            drawer.translate(drawer.bounds.center)
            drawer.scale(1.0, 0.5)

            // Manage point count and appearing state
            val pointsToDrawChange = 1 + (abs(sin(time)) * speed).toInt()
            if(isAppearing) {
                pointsToDrawCount += pointsToDrawChange
                if(pointsToDrawCount >= points.size) {
                    pointsToDrawCount = points.size
                    isAppearing = false
                }
            } else {
                pointsToDrawCount -= pointsToDrawChange
                if(pointsToDrawCount < 1) {
                    pointsToDrawCount = 1
                    isAppearing = true
                    if (recording) program.application.exit()
                }
            }
            println(pointsToDrawChange)
            // Build shape
            val shape = contour {
                val pointsWithoutLast = if (isAppearing) {
                    moveTo(points.first())
                    points.take(pointsToDrawCount - 1)
                } else {
                    val firstPoint = points.size - pointsToDrawCount
                    moveTo(points[firstPoint])
                    points.takeLast(pointsToDrawCount - 1)
                }
                pointsWithoutLast.forEach {
                    lineTo(it)
                }
            }
            // Draw shape
            drawer.strokeWeight = 2.0
            drawer.stroke = ColorRGBa.AQUAMARINE
            drawer.contour(shape)


//            val lines = points.windowed(2)
//                .flatten()
//                .reversed()
//                .map { it.vector3(z = 0.0) }
//            val lineColors = List(lines.size) {
//                if (it < lines.size / 2) {
//                    ColorRGBa.MEDIUM_VIOLET_RED.mix(ColorRGBa.BLUE_STEEL, it / (lines.size / 2.0))
//                } else {
//                    ColorRGBa.BLUE_STEEL.mix(ColorRGBa.MEDIUM_VIOLET_RED, (it - lines.size / 2) / (lines.size / 2.0))
//                }
//            }
//            drawer.lineSegments(lines, weights = List(lines.size) { 2.0 }, colors = lineColors)
        }
    }
}

private fun getNewPoint(center: Vector2, radius: Double, theta: Double): Vector2 {
    return center + Vector2.fromPolar(Polar(theta, radius))
}

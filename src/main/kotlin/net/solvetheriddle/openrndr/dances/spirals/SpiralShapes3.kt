package net.solvetheriddle.openrndr.dances.spirals

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extra.color.presets.*
import org.openrndr.extra.videoprofiles.ProresProfile
import org.openrndr.ffmpeg.ScreenRecorder
import org.openrndr.math.Polar
import org.openrndr.math.Vector2
import org.openrndr.shape.Segment
import org.openrndr.shape.ShapeContour
import org.openrndr.shape.contour
import kotlin.math.*

fun main() = application {

    // config recording
    val recording = false

    configure {
        if (recording) {
            width = 1080
            height = 1080
        } else {
//            sketchSize(Display.LG_ULTRAWIDE)
            width = 1080
            height = 1080
        }
    }
    program {


        if (recording) {
            extend(ScreenRecorder().apply {
                frameRate = 60
                profile = ProresProfile()
            })
        }

        // config parameters
        val margin = 800.0
        val dimension = min(width, height) - margin
        val radiusFactor = dimension / 2.25
        val radiusSpeed = 1
        val minRadius = dimension / 10.0
        val centerFactor = dimension / 2.0
        val centerSpeed = 0.02
        val resolution = .01
        val speed = 15
        val centerShape = mutableListOf<Vector2>()
        val templatePointCount = (2 * PI * 1108).toInt()
        val numOfCorners = 8
        val corners = List(numOfCorners) { Vector2.fromPolar(Polar((360.0 / numOfCorners) * it, dimension)) }
        val template = ShapeContour(corners
            .windowed(2)
            .map { Segment(it[0], it[1]) }
                + Segment(corners.last(), corners.first())
                + Segment(corners.first(), corners[4]), true)
            .equidistantPositions(templatePointCount)

        var time = 0.0
        val points = mutableListOf<Vector2>()
        repeat(templatePointCount) {
            time += resolution
            val octagonPoints = templatePointCount * 0.75
            val radius = if (it < octagonPoints) {
                minRadius + (abs((sin(PI / (octagonPoints / 2) * it))) * radiusFactor)
            } else {
                minRadius + (abs((sin(PI / (templatePointCount-octagonPoints) * it))) * 3.3 * radiusFactor)
            }
            val center = template[it % templatePointCount]
            centerShape.add(center)
            val newPoint = getNewPoint(center, radius, time * 1000)
            points.add(newPoint)
        }
        time = 0.0
        var shapeProgress = 0 // points.size
        var isAppearing = true

        extend {
            time += .006 // config tweak to slow down when looping
//            drawer.clear(Colors.BG_GREY)
            drawer.clear(ColorRGBa.BLACK)
            drawer.translate(drawer.bounds.center)
//            drawer.scale(0.5, 1.0)

            // Manage point count and appearing state
            val pointsToDrawChange = 1 + (abs(sin(time)) * speed).toInt()
            if (isAppearing) {
                shapeProgress += pointsToDrawChange
                if (shapeProgress >= points.size) {
                    shapeProgress = points.size
                    isAppearing = false
                    time = -PI/22
                }
            } else {
                shapeProgress -= pointsToDrawChange
                if (shapeProgress < 1) {
                    shapeProgress = 1
                    isAppearing = true
                    if (recording) program.application.exit()
                }
            }
            println(pointsToDrawChange)
            // Build shape
            val shape = contour {
                val pointsWithoutLast = if (isAppearing) {
                    moveTo(points.first())
                    points.take(shapeProgress - 1)
                } else {
                    val firstPoint = points.size - shapeProgress
                    moveTo(points[firstPoint])
                    points.takeLast(shapeProgress - 1)
                }
                pointsWithoutLast.forEach {
                    lineTo(it)
                }
            }

//            // draw center shape
//            drawer.stroke = ColorRGBa.INDIAN_RED
//            drawer.contour(contour {
//                moveTo(centerShape[0])
//                centerShape.forEach { lineTo(it) }
//            })

            // Draw shape
            drawer.strokeWeight = 2.0
            drawer.stroke = ColorRGBa.LAWN_GREEN
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

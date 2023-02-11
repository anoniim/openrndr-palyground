package net.solvetheriddle.openrndr.practice

import net.solvetheriddle.openrndr.Colors
import net.solvetheriddle.openrndr.Display
import net.solvetheriddle.openrndr.sketchSize
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extra.color.presets.*
import org.openrndr.extra.videoprofiles.ProresProfile
import org.openrndr.ffmpeg.ScreenRecorder
import org.openrndr.math.Polar
import org.openrndr.math.Vector2
import org.openrndr.shape.Rectangle
import org.openrndr.shape.contour
import kotlin.math.*


fun main() = application {

    // config recording
    val recording = false

    configure {
        if(recording) {
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
        val margin = 600.0
        val dimension = min(width, height) - margin
        val radiusFactor = dimension / 10.0
        val radiusSpeed = 1
        val minRadius = dimension / 9.0
        val centerFactor = dimension / 2.0
        val centerSpeed = 0.02
        val resolution = .01
        val speed = 25
        val centerShape = mutableListOf<Vector2>()
        val templatePointCount = (2 * PI * 1108).toInt()
        val template = Rectangle(-dimension/2.0, -dimension/2.0, dimension, dimension)
            .contour.equidistantPositions(templatePointCount)

        var time = 0.0
        val points = mutableListOf<Vector2>()
        repeat(2 * templatePointCount) {
            time += resolution
            val radius = if(it < templatePointCount) {
                minRadius + (abs((sin(PI / (templatePointCount / 4) * it))) * 3.9 * radiusFactor)
            } else {
                minRadius + (abs((sin(PI / (templatePointCount / 4) * it))) * 2 * radiusFactor)
            }
            val center = template[it%templatePointCount]
            centerShape.add(center)
            val newPoint = getNewPoint(center, radius, time * 1000)
            points.add(newPoint)
        }
        time = 0.0
        var shapeProgress = points.size
        var isAppearing = false

        extend {
            time += .0074 // config tweak to slow down when looping
            drawer.clear(Colors.BG_GREY)
            drawer.clear(ColorRGBa.BLACK)
            drawer.translate(drawer.bounds.center)
//            drawer.scale(1.0, 0.5)

            // Manage point count and appearing state
            val pointsToDrawChange = 1 + (abs(sin(time)) * speed).toInt()
            if(isAppearing) {
                shapeProgress += pointsToDrawChange
                if(shapeProgress >= points.size) {
                    shapeProgress = points.size
                    isAppearing = false
                    if (recording) program.application.exit()
                }
            } else {
                shapeProgress -= pointsToDrawChange
                if(shapeProgress < 1) {
                    shapeProgress = 1
                    isAppearing = true
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
            drawer.stroke = ColorRGBa.MEDIUM_ORCHID
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

package net.solvetheriddle.openrndr.spiral_shapes

import net.solvetheriddle.openrndr.Display
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.Drawer
import org.openrndr.extra.color.presets.DARK_ORCHID
import org.openrndr.extra.color.presets.INDIAN_RED
import org.openrndr.extra.videoprofiles.ProresProfile
import org.openrndr.ffmpeg.ScreenRecorder
import org.openrndr.math.IntVector2
import org.openrndr.math.Vector2
import org.openrndr.shape.Segment
import org.openrndr.shape.ShapeContour

fun main() = application {

    // config recording
    val recording = false

    configure {
        width = 1080
        height = 1080
        position = IntVector2(100, (Display.LG_ULTRAWIDE.height - height) / 2)
    }

    program {

        val dimension = 500.0

        val topDownTemplate = ShapeContour(listOf(Segment(Vector2(0.0, -dimension), Vector2(0.0, 0.0))), false)
        val bottomUpTemplate = ShapeContour(listOf(Segment(Vector2(0.0, dimension), Vector2(0.0, 0.0))), false)
        val leftRightTemplate = ShapeContour(listOf(Segment(Vector2(-dimension, 0.0), Vector2(0.0, 0.0))), false)
        val rightLeftTemplate = ShapeContour(listOf(Segment(Vector2(dimension, 0.0), Vector2(0.0, 0.0))), false)
        val shapes = listOf(
            SpiralShape(
                topDownTemplate,
                startRadius = 0.0,
                endRadius = dimension,
                spiralLengthOffset = 270.0,
            ),
            SpiralShape(
                bottomUpTemplate,
                startRadius = 0.0,
                endRadius = dimension,
                spiralLengthOffset = 90.0,
            ),
//            SpiralShape(
//                leftRightTemplate,
//                startRadius = 0.0,
//                endRadius = dimension,
//                spiralLengthOffset = 180.0,
//            ),
//            SpiralShape(
//                rightLeftTemplate,
//                startRadius = 0.0,
//                endRadius = dimension,
//                spiralLengthOffset = 0.0,
//            ),
        )

        shapes.forEach { it.build() }

        extend {
            drawer.clear(ColorRGBa.BLACK)
            drawer.translate(drawer.bounds.center)
//            drawer.scale(0.5, 1.0)

            shapes.forEach {
                // config
//                drawer.drawTemplateShape(it.templateContour)

                it.update(frameCount)
                drawer.drawShape(it.contour)
            }
        }

        if (recording) {
            extend(ScreenRecorder().apply {
                frameRate = 60
                profile = ProresProfile()
            })
        }
    }
}

private fun Drawer.drawTemplateShape(centerShape: ShapeContour) {
    strokeWeight = 3.0
    stroke = ColorRGBa.INDIAN_RED
    contour(centerShape)
}

private fun Drawer.drawShape(shape: ShapeContour) {
    strokeWeight = 2.0
    stroke = ColorRGBa.DARK_ORCHID
    contour(shape)
}
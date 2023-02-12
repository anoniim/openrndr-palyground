package net.solvetheriddle.openrndr.spiral_shapes

import net.solvetheriddle.openrndr.Display
import net.solvetheriddle.openrndr.tools.Move
import net.solvetheriddle.openrndr.tools.Movie
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.Drawer
import org.openrndr.extra.color.presets.DARK_ORCHID
import org.openrndr.extra.color.presets.INDIAN_RED
import org.openrndr.extra.videoprofiles.ProresProfile
import org.openrndr.ffmpeg.ScreenRecorder
import org.openrndr.math.IntVector2
import org.openrndr.math.Vector2
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

        val topDownTemplate = listOf(Vector2(0.0, -dimension), Vector2(0.0, 0.0))
        val bottomUpTemplate = listOf(Vector2(0.0, dimension), Vector2(0.0, 0.0))
        val leftRightTemplate = listOf(Vector2(-dimension, 0.0), Vector2(0.0, 0.0))
        val rightLeftTemplate = listOf(Vector2(dimension, 0.0), Vector2(0.0, 0.0))
        val centralShapes = listOf(
            SpiralShape(
                listOf(Vector2(0.0, 0.0)),
                startRadius = 0.0,
                endRadius = dimension,
                animationFrames = (60 * 2.0).toInt(),
            )
        )
        val verticalShapes = listOf(
            SpiralShape(
                topDownTemplate,
                startRadius = 0.0,
                endRadius = dimension,
                spiralEnd = 270.0,
                animationFrames = (60 * 2.0).toInt(),
            ),
            SpiralShape(
                bottomUpTemplate,
                startRadius = 0.0,
                endRadius = dimension,
                spiralEnd = 90.0,
                animationFrames = (60 * 2.0).toInt(),
            ))
        val horizontalShapes = listOf(
            SpiralShape(
                leftRightTemplate,
                startRadius = 0.0,
                endRadius = dimension,
                spiralEnd = 180.0,
                animationFrames = (60 * 2.0).toInt(),
            ),
            SpiralShape(
                rightLeftTemplate,
                startRadius = 0.0,
                endRadius = dimension,
                spiralEnd = 0.0,
                animationFrames = (60 * 2.0).toInt(),
            ),
        )

        val movie = Movie(
            loop = true,
            moves = listOf(
                SpiralShapeMove(
                    fromFrame = 0,
                    lengthFrames = (60 * 2.0).toInt(),
                    centralShapes,
                ),
                SpiralShapeMove(
                    fromFrame = 0,
                    lengthFrames = (60 * 2.0).toInt(),
                    verticalShapes,
                ),
                SpiralShapeMove(
                    fromFrame = (60 * 2.0).toInt(),
                    lengthFrames = (60 * 2.0).toInt(),
                    horizontalShapes
                ),
            )
        )

        extend {
            drawer.clear(ColorRGBa.BLACK)
            drawer.translate(drawer.bounds.center)
//            drawer.scale(0.5, 1.0)

            movie.play(frameCount)
        }

        if (recording) {
            extend(ScreenRecorder().apply {
                frameRate = 60
                profile = ProresProfile()
            })
        }
    }
}

internal class SpiralShapeMove(
    fromFrame: Int, lengthFrames: Int,
    private val shapes: List<SpiralShape>
) : Move(fromFrame, lengthFrames,
    { frameCount ->
        shapes.forEach {
//            drawer.drawTemplateShape(it.templateContour) // config
            it.update(frameCount)
            drawer.drawShape(it.contour)
        }
    })

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

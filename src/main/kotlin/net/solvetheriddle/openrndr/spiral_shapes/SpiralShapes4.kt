package net.solvetheriddle.openrndr.spiral_shapes

import net.solvetheriddle.openrndr.Display
import net.solvetheriddle.openrndr.tools.Move
import net.solvetheriddle.openrndr.tools.Movie
import org.openrndr.animatable.easing.QuadInOut
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.Drawer
import org.openrndr.draw.isolated
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
        val centralShape1 = SpiralShape(
            listOf(Vector2(0.0, 0.0)),
            startRadius = 0.0,
            endRadius = dimension / 4,
            spiralDensity = 5,
            spiralEnd = 90.0,
            animationLength = (60 * 6.0).toInt(),
        )
        val centralShape2 = SpiralShape(
            listOf(Vector2(0.0, 0.0)),
            startRadius = 0.0,
            endRadius = dimension / 2,
            spiralDensity = 10,
            animationLength = (60 * 6.0).toInt(),
        )
        val centralShape3 = SpiralShape(
            listOf(Vector2(0.0, 0.0)),
            startRadius = 0.0,
            endRadius = dimension,
            spiralDensity = 20,
            spiralEnd = 90.0,
            animationMode = AnimationMode.REVEAL_HIDE,
            animationLength = (60 * 12.0).toInt(),
        )
        val bottomShape = SpiralShape(
            bottomUpTemplate,
            startRadius = 0.0,
            endRadius = dimension,
            spiralDensity = 20,
            spiralEnd = 90.0,
            animationMode = AnimationMode.REVEAL,
            animationLength = (60 * 8.0).toInt(),
        )
        val bottomShapeFaster = SpiralShape(
            bottomUpTemplate,
            startRadius = 0.0,
            endRadius = dimension,
            spiralDensity = 20,
            spiralEnd = 90.0,
            animationMode = AnimationMode.REVEAL_HIDE,
            animationLength = (60 * 4.0).toInt(),
        )
        val topShapeFaster = SpiralShape(
            topDownTemplate,
            startRadius = 0.0,
            endRadius = dimension,
            spiralDensity = 20,
            spiralEnd = 270.0,
            animationMode = AnimationMode.REVEAL_HIDE,
            animationLength = (60 * 4.0).toInt(),
        )
        val bottomShapeHide = SpiralShape(
            bottomUpTemplate,
            startRadius = 0.0,
            endRadius = dimension,
            spiralDensity = 20,
            spiralEnd = 90.0,
            animationMode = AnimationMode.REVERSED_REVEAL,
            animationLength = (60 * 4.0).toInt(),
        )
        val bottomShapeStatic = SpiralShape(
            bottomUpTemplate,
            startRadius = 0.0,
            endRadius = dimension,
            spiralDensity = 20,
            spiralEnd = 90.0,
            animationMode = AnimationMode.STATIC,
            animationLength = (60 * 2.0).toInt(),
        )
        val verticalShapes = listOf(
            SpiralShape(
                topDownTemplate,
                startRadius = 0.0,
                endRadius = dimension,
                spiralEnd = 270.0,
                animationLength = (60 * 2.0).toInt(),
            ),
            SpiralShape(
                bottomUpTemplate,
                startRadius = 0.0,
                endRadius = dimension,
                spiralEnd = 90.0,
                animationLength = (60 * 2.0).toInt(),
            )
        )
        val horizontalShapes = listOf(
            SpiralShape(
                leftRightTemplate,
                startRadius = 0.0,
                endRadius = dimension,
                spiralEnd = 180.0,
                animationLength = (60 * 2.0).toInt(),
            ),
            SpiralShape(
                rightLeftTemplate,
                startRadius = 0.0,
                endRadius = dimension,
                spiralEnd = 0.0,
                animationLength = (60 * 2.0).toInt(),
            ),
        )
        val allShapes = listOf(
            SpiralShape(
                topDownTemplate,
                startRadius = 0.0,
                endRadius = dimension,
                spiralEnd = 270.0,
                animationLength = (60 * 6.0).toInt(),
            ),
            SpiralShape(
                bottomUpTemplate,
                startRadius = 0.0,
                endRadius = dimension,
                spiralEnd = 90.0,
                animationLength = (60 * 6.0).toInt(),
            ),
            SpiralShape(
                leftRightTemplate,
                startRadius = 0.0,
                endRadius = dimension,
                spiralEnd = 180.0,
                animationLength = (60 * 6.0).toInt(),
            ),
            SpiralShape(
                rightLeftTemplate,
                startRadius = 0.0,
                endRadius = dimension,
                spiralEnd = 0.0,
                animationLength = (60 * 6.0).toInt(),
            ),
        )

        val movie = Movie(loop = true).apply {
            add(SpiralShapeMove(centralShape1))
//            append(SpiralShapeMove(centralShape2))
            append(SpiralShapeMove(centralShape3))
            append(SpiralShapeMove(bottomShape))
            append(rotatingMove(bottomShapeStatic, 0.0, 180.0))
            append(rotatingMove(bottomShapeStatic, 180.0, 180.0))
            append(SpiralShapeMove(bottomShapeHide))
            append(SpiralShapeMove(bottomShapeFaster))
            append(SpiralShapeMove(topShapeFaster))
            append(SpiralShapeMove(verticalShapes))
            append(SpiralShapeMove(horizontalShapes))
            append(SpiralShapeMove(allShapes))
        }

        extend {
            drawer.clear(ColorRGBa.BLACK)
            drawer.translate(drawer.bounds.center)
//            drawer.scale(0.5, 1.0)

            movie.play()
        }

        if (recording) {
            extend(ScreenRecorder().apply {
                frameRate = 60
                profile = ProresProfile()
            })
        }
    }
}

private fun rotatingMove(bottomShapeStatic: SpiralShape, from: Double, delta: Double) =
    Move(bottomShapeStatic.animationLength) { frameCount ->
        bottomShapeStatic.update(frameCount)
        drawer.isolated {
            val rotationInDegrees = QuadInOut().ease(frameCount.toDouble(), from, delta, bottomShapeStatic.animationLength.toDouble())
            drawer.rotate(rotationInDegrees)
            drawer.drawShape(bottomShapeStatic.contour)
        }
    }

internal class SpiralShapeMove(
    private val shapes: List<SpiralShape>,
    lengthFrames: Int = shapes[0].animationLength,
) : Move(lengthFrames,
    { frameCount ->
        shapes.forEach {
//            drawer.drawTemplateShape(it.templateContour) // config
            it.update(frameCount)
            drawer.drawShape(it.contour)
        }
    }) {

    constructor(shape: SpiralShape, lengthFrames: Int = shape.animationLength) : this(listOf(shape), lengthFrames)
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

package net.solvetheriddle.openrndr.dances.spirals

import net.solvetheriddle.openrndr.Display
import net.solvetheriddle.openrndr.tools.Move
import net.solvetheriddle.openrndr.tools.Movie
import org.openrndr.Program
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
        if (recording) {
            extend(ScreenRecorder().apply {
                frameRate = 60
                profile = ProresProfile()
            })
        }

        val dimension = 500.0

        val topDownTemplate = listOf(Vector2(0.0, -dimension), Vector2(0.0, 0.0))
        val bottomUpTemplate = listOf(Vector2(0.0, dimension), Vector2(0.0, 0.0))
        val leftRightTemplate = listOf(Vector2(-dimension, 0.0), Vector2(0.0, 0.0))
        val rightLeftTemplate = listOf(Vector2(dimension, 0.0), Vector2(0.0, 0.0))
        val centralSpiral1 = SpiralShape(
            listOf(Vector2(0.0, 0.0)),
            startRadius = 0.0,
            endRadius = dimension / 4,
            spiralEndOffset = 180.0,
            animationMode = AnimationMode.REVEAL_HIDE,
            animationLength = (60 * 6.0).toInt(),
        )
        val centralSpiral2 = SpiralShape(
            listOf(Vector2(0.0, 0.0)),
            startRadius = 0.0,
            endRadius = dimension / 2,
            spiralEndOffset = 278.0,
            animationMode = AnimationMode.REVEAL_HIDE,
            animationLength = (60 * 6.0).toInt(),
        )
        val ring = SpiralShape(
            listOf(Vector2(0.0, 0.0)),
            startRadius = dimension / 2,
            endRadius = dimension,
            spiralEndOffset = 95.0,
            animationMode = AnimationMode.REVEAL_HIDE,
            animationLength = (60 * 6.0).toInt(),
        )
        val centralShape4 = SpiralShape(
            listOf(Vector2(0.0, 0.0)),
            startRadius = 0.0,
            endRadius = dimension,
            spiralEndOffset = 95.0,
            animationMode = AnimationMode.REVEAL_HIDE,
            animationLength = (60 * 12.0).toInt(),
        )
        val topSpiralReveal = SpiralShape(
            topDownTemplate,
            startRadius = 0.0,
            endRadius = dimension,
            spiralEndOffset = 270.0,
            animationMode = AnimationMode.REVEAL,
            animationLength = (60 * 6.0).toInt(),
        )
        val bottomSpiralRevealHide = SpiralShape(
            bottomUpTemplate,
            startRadius = 0.0,
            endRadius = dimension,
            spiralEndOffset = 90.0,
            animationMode = AnimationMode.REVEAL_HIDE,
            animationLength = (60 * 6.0).toInt(),
        )
        val topSpiralRevealHide = SpiralShape(
            topDownTemplate,
            startRadius = 0.0,
            endRadius = dimension,
            spiralEndOffset = 270.0,
            animationMode = AnimationMode.REVEAL_HIDE,
            animationLength = (60 * 6.0).toInt(),
        )
        val topSpiralHide = SpiralShape(
            topDownTemplate,
            startRadius = 0.0,
            endRadius = dimension,
            spiralEndOffset = 270.0,
            animationMode = AnimationMode.REVERSED_REVEAL,
            animationLength = (60 * 4.0).toInt(),
        )
        val topShapeStatic = SpiralShape(
            topDownTemplate,
            startRadius = 0.0,
            endRadius = dimension,
            spiralEndOffset = 270.0,
            animationMode = AnimationMode.STATIC,
            animationLength = (60 * 2.0).toInt(),
        )
        val verticalShapes = listOf(
            SpiralShape(topDownTemplate, startRadius = 0.0, endRadius = dimension, spiralEndOffset = 270.0, animationLength = (60 * 2.0).toInt()),
            SpiralShape(bottomUpTemplate, startRadius = 0.0, endRadius = dimension, spiralEndOffset = 90.0, animationLength = (60 * 2.0).toInt()),
        )
        val horizontalShapes = listOf(
            SpiralShape(leftRightTemplate, startRadius = 0.0, endRadius = dimension, spiralEndOffset = 180.0, animationLength = (60 * 2.0).toInt()),
            SpiralShape(rightLeftTemplate, startRadius = 0.0, endRadius = dimension, spiralEndOffset = 0.0, animationLength = (60 * 2.0).toInt()),
        )
        val allShapesHalf = listOf(
            SpiralShape(topDownTemplate, startRadius = 0.0, endRadius = dimension / 2, spiralEndOffset = 270.0, animationLength = (60 * 8.0).toInt()),
            SpiralShape(bottomUpTemplate, startRadius = 0.0, endRadius = dimension / 2, spiralEndOffset = 90.0, animationLength = (60 * 8.0).toInt()),
            SpiralShape(leftRightTemplate, startRadius = 0.0, endRadius = dimension / 2, spiralEndOffset = 180.0, animationLength = (60 * 8.0).toInt()),
            SpiralShape(rightLeftTemplate, startRadius = 0.0, endRadius = dimension / 2, spiralEndOffset = 0.0, animationLength = (60 * 8.0).toInt()),
        )
        val allShapesFull = listOf(
            SpiralShape(topDownTemplate, startRadius = 0.0, endRadius = dimension, spiralEndOffset = 270.0, animationLength = (60 * 6.0).toInt()),
            SpiralShape(bottomUpTemplate, startRadius = 0.0, endRadius = dimension, spiralEndOffset = 90.0, animationLength = (60 * 6.0).toInt()),
            SpiralShape(leftRightTemplate, startRadius = 0.0, endRadius = dimension, spiralEndOffset = 180.0, animationLength = (60 * 6.0).toInt()),
            SpiralShape(rightLeftTemplate, startRadius = 0.0, endRadius = dimension, spiralEndOffset = 0.0, animationLength = (60 * 6.0).toInt()),
        )

        val movie = Movie(loop = !recording).apply {
            add(SpiralShapeMove(centralSpiral1))
            append(SpiralShapeMove(centralSpiral2))
            append(SpiralShapeMove(ring))
            append(SpiralShapeMove(topSpiralRevealHide))
            append(SpiralShapeMove(bottomSpiralRevealHide))
            append(SpiralShapeMove(topSpiralReveal))
            append(rotatingMove(topShapeStatic, 0.0, 180.0))
            append(rotatingMove(topShapeStatic, 180.0, 180.0))
            append(rotatingMove(topShapeStatic, 0.0, -180.0))
            append(rotatingMove(topShapeStatic, 180.0, -180.0))
            append(SpiralShapeMove(verticalShapes))
            append(SpiralShapeMove(horizontalShapes))
            append(SpiralShapeMove(verticalShapes))
            append(SpiralShapeMove(horizontalShapes))
            append(SpiralShapeMove(allShapesFull))
            append(SpiralShapeMove(allShapesHalf))
        }

        extend {
            drawer.clear(ColorRGBa.BLACK)
            drawer.translate(drawer.bounds.center)
//            drawer.scale(0.5, 1.0)

            movie.play {
                if (recording) program.application.exit()
            }
        }
    }
}

private fun rotatingMove(bottomShapeStatic: SpiralShape, from: Double, delta: Double) =
    object: Move(bottomShapeStatic.animationLength) {
        override fun Program.moveFunction(frameCount: Int) {
            bottomShapeStatic.update(frameCount)
            drawer.isolated {
                val rotationInDegrees = QuadInOut().ease(frameCount.toDouble(), from, delta, bottomShapeStatic.animationLength.toDouble())
                drawer.rotate(rotationInDegrees)
                drawer.drawShape(bottomShapeStatic.contour)
            }
        }
    }

internal class SpiralShapeMove(
    private val shapes: List<SpiralShape>,
    lengthFrames: Int = shapes[0].animationLength,
) : Move(lengthFrames) {

    constructor(shape: SpiralShape, lengthFrames: Int = shape.animationLength) : this(listOf(shape), lengthFrames)

    override fun Program.moveFunction(frameCount: Int) {
        shapes.forEach {
//            drawer.drawTemplateShape(it.templateContour) // config
            it.update(frameCount)
            drawer.drawShape(it.contour)
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

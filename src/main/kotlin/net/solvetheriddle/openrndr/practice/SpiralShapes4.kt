package net.solvetheriddle.openrndr.practice

import net.solvetheriddle.openrndr.Display
import org.openrndr.animatable.easing.SineInOut
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.Drawer
import org.openrndr.extra.color.presets.*
import org.openrndr.extra.videoprofiles.ProresProfile
import org.openrndr.ffmpeg.ScreenRecorder
import org.openrndr.math.IntVector2
import org.openrndr.math.Polar
import org.openrndr.math.Vector2
import org.openrndr.shape.Segment
import org.openrndr.shape.ShapeContour
import org.openrndr.shape.contour
import kotlin.math.PI
import kotlin.math.sin

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

        class SpiralShape(
            val template: ShapeContour,
            val startRadius: Double,
            val endRadius: Double,
            val spiralLengthOffset: Double = 0.0,
            val animationMode: AnimationMode = AnimationMode.REVEAL,
            val animationFrames: Int = (60 * 10.0).toInt(),
            val animationStartFrame: Int = 0,
        ) {

            val spiralDensity = 50 // 10..50
            val templateResolution = 3000
            val templatePoints = template.equidistantPositions(templateResolution)
            val shapePoints = mutableListOf<Vector2>()
            val animationFunction = when(animationMode) {
                AnimationMode.REVEAL -> RevealAnimationFunction()
                AnimationMode.REVEAL_BOUNCE -> RevealBounceAnimationFunction()
                AnimationMode.REVEAL_HIDE -> RevealHideAnimationFunction()
            }

            fun build() {
                var time = 0.0
                repeat(templateResolution) {
                    time += 1.0 / templateResolution
                    val radius = startRadius + time * (endRadius - startRadius)
                    val center = templatePoints[it]
                    val newPoint =
                        center + Vector2.fromPolar(Polar(360 * time * spiralDensity + spiralLengthOffset, radius))
                    shapePoints.add(newPoint)
                }
            }

            var shapeProgress = Pair(0, 0)

            fun update() {
                val currentFrameCount = (frameCount - animationStartFrame) % animationFrames
                shapeProgress = animationFunction.updateShapeProgress(shapeProgress, currentFrameCount, shapePoints.lastIndex, animationFrames)
            }

            val contour: ShapeContour
                get() = contour {
                    moveTo(shapePoints[shapeProgress.first])
                    val pointsToDraw = shapePoints.subList(shapeProgress.first, shapeProgress.second)
                    pointsToDraw.forEach { lineTo(it) }
                }

            val templateContour: ShapeContour
                get() = contour {
                    moveTo(templatePoints[0])
                    templatePoints.forEach { lineTo(it) }
                }
        }



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

                it.update()
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

private enum class AnimationMode {
    REVEAL,
    REVEAL_BOUNCE,
    REVEAL_HIDE,
}

private interface AnimationFunction {
    fun updateShapeProgress(shapeProgress: Pair<Int, Int>, currentFrameCount: Int, lastIndex: Int, animationFrames: Int): Pair<Int, Int>
}

private class RevealAnimationFunction: AnimationFunction {
    override fun updateShapeProgress(shapeProgress: Pair<Int, Int>, currentFrameCount: Int, lastIndex: Int, animationFrames: Int): Pair<Int, Int> {
        val revealPointerFunction = SineInOut().ease(currentFrameCount.toDouble(), 0.0, lastIndex.toDouble(), animationFrames.toDouble()).toInt()
        return shapeProgress.copy(second = revealPointerFunction)
    }
}

private class RevealBounceAnimationFunction: AnimationFunction {
    override fun updateShapeProgress(shapeProgress: Pair<Int, Int>, currentFrameCount: Int, lastIndex: Int, animationFrames: Int): Pair<Int, Int> {
        val revealBouncePointerFunction = (sin(currentFrameCount * PI / animationFrames) * lastIndex).toInt()
        return shapeProgress.copy(second = revealBouncePointerFunction)
    }
}

private class RevealHideAnimationFunction: AnimationFunction {
    override fun updateShapeProgress(shapeProgress: Pair<Int, Int>, currentFrameCount: Int, lastIndex: Int, animationFrames: Int): Pair<Int, Int> {
        val revealHideEndPointerFunction = SineInOut().ease(currentFrameCount.toDouble(), 0.0, (lastIndex).toDouble(), animationFrames/2.0).toInt()
        val revealHideStartPointerFunction = SineInOut().ease(currentFrameCount - animationFrames / 2.0, 0.0, (lastIndex).toDouble(), animationFrames/2.0).toInt()
        return if (currentFrameCount == animationFrames - 1) {
            Pair(0, 0)
        } else if (currentFrameCount < animationFrames / 2) {
            shapeProgress.copy(second = revealHideEndPointerFunction)
        } else {
            shapeProgress.copy(first = revealHideStartPointerFunction)
        }
    }
}
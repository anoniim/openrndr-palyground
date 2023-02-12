package net.solvetheriddle.openrndr.spiral_shapes

import org.openrndr.animatable.easing.SineInOut
import org.openrndr.math.Polar
import org.openrndr.math.Vector2
import org.openrndr.shape.ShapeContour
import org.openrndr.shape.contour
import kotlin.math.PI
import kotlin.math.sin

internal class SpiralShape(
    template: ShapeContour,
    val startRadius: Double,
    val endRadius: Double,
    val spiralLengthOffset: Double = 0.0,
    animationMode: AnimationMode = AnimationMode.REVEAL_BOUNCE,
    private val animationFrames: Int = (60 * 10.0).toInt(),
    private val animationStartFrame: Int = 0,
) {

    private val spiralDensity = 50 // 10..50
    private val templateResolution = 3000

    private val templatePoints = template.equidistantPositions(templateResolution)
    private val shapePoints = mutableListOf<Vector2>()
    private val animationFunction = when(animationMode) {
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

    private var shapeProgress = Pair(0, 0)

    fun update(frameCount: Int) {
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

internal enum class AnimationMode {
    REVEAL,
    REVEAL_BOUNCE,
    REVEAL_HIDE,
}

internal interface AnimationFunction {
    fun updateShapeProgress(shapeProgress: Pair<Int, Int>, currentFrameCount: Int, lastIndex: Int, animationFrames: Int): Pair<Int, Int>
}

internal class RevealAnimationFunction: AnimationFunction {
    override fun updateShapeProgress(shapeProgress: Pair<Int, Int>, currentFrameCount: Int, lastIndex: Int, animationFrames: Int): Pair<Int, Int> {
        val revealPointerFunction = SineInOut().ease(currentFrameCount.toDouble(), 0.0, lastIndex.toDouble(), animationFrames.toDouble()).toInt()
        return shapeProgress.copy(second = revealPointerFunction)
    }
}

internal class RevealBounceAnimationFunction: AnimationFunction {
    override fun updateShapeProgress(shapeProgress: Pair<Int, Int>, currentFrameCount: Int, lastIndex: Int, animationFrames: Int): Pair<Int, Int> {
        val revealBouncePointerFunction = (sin(currentFrameCount * PI / animationFrames) * lastIndex).toInt()
        return shapeProgress.copy(second = revealBouncePointerFunction)
    }
}

internal class RevealHideAnimationFunction: AnimationFunction {
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
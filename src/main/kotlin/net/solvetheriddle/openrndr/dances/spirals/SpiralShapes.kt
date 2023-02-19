package net.solvetheriddle.openrndr.dances.spirals

import org.openrndr.animatable.easing.CubicInOut
import org.openrndr.math.Polar
import org.openrndr.math.Vector2
import org.openrndr.shape.Segment
import org.openrndr.shape.ShapeContour
import org.openrndr.shape.contour
import kotlin.math.PI
import kotlin.math.sin

internal class SpiralShape(
    templatePoints: List<Vector2>,
    private val startRadius: Double,
    private val endRadius: Double,
    private val spiralDensity: Double = .03, // 0.1..1
    /** Polar angle of the end of the spiral */
    private val spiralEndOffset: Double = 0.0,
    animationMode: AnimationMode = AnimationMode.REVEAL_BOUNCE,
    /** Length of the animation in frames (60 fps * seconds rounded) */
    val animationLength: Int = (60 * 10.0).toInt(),
) {

    private val templateResolution = 3000

    private val templateResampled = if (templatePoints.size > 1) {
        resampleTemplate(templatePoints, templateResolution)
    } else List(templateResolution) { templatePoints[0] }
    private val shapePoints = mutableListOf<Vector2>()
    private val animationFunction = when (animationMode) {
        AnimationMode.STATIC -> NoAnimationFunction()
        AnimationMode.REVEAL -> RevealAnimationFunction()
        AnimationMode.REVEAL_BOUNCE -> RevealBounceAnimationFunction()
        AnimationMode.REVEAL_HIDE -> RevealHideAnimationFunction()
        AnimationMode.REVERSED_REVEAL -> ReversedRevealAnimationFunction()
        AnimationMode.HIDE -> HideAnimationFunction()
    }

    init {
        var time = 0.0
        repeat(templateResolution) {
            time += 1.0 / templateResolution
            val radiusDelta = endRadius - startRadius
            val radius = startRadius + time * radiusDelta
            val center = templateResampled[it]
            val newPoint =
                center + Vector2.fromPolar(Polar(360 * time * radiusDelta * spiralDensity + spiralEndOffset, radius))
            shapePoints.add(newPoint)
        }
    }

    private val startEmptyModes = listOf(
        AnimationMode.REVEAL,
        AnimationMode.REVEAL_BOUNCE,
        AnimationMode.REVEAL_HIDE,
    )
    private var shapeProgress = if (animationMode in startEmptyModes) {
        Pair(0, 0) // start empty
    } else {
        Pair(0, shapePoints.lastIndex) // start full
    }

    /**
     * Expects [frameCount] in range 0 until [animationLength]
     */
    fun update(frameCount: Int) {
        val currentFrame = (frameCount) % animationLength
        shapeProgress = animationFunction.updateShapeProgress(shapeProgress, currentFrame, shapePoints.lastIndex, animationLength - 1)
    }

    val contour: ShapeContour
        get() = contour {
            moveTo(shapePoints[shapeProgress.first])
            val pointsToDraw = shapePoints.subList(shapeProgress.first, shapeProgress.second)
            pointsToDraw.forEach { lineTo(it) }
        }

    val templateContour: ShapeContour
        get() = contour {
            moveTo(templateResampled[0])
            templateResampled.forEach { lineTo(it) }
        }

    private fun resampleTemplate(templatePoints: List<Vector2>, templateResolution: Int): List<Vector2> {

        val template = ShapeContour(
            templatePoints.windowed(2)
                .map { Segment(it[0], it[1]) },
            false
        )
        return template.equidistantPositions(templateResolution)
    }
}

internal enum class AnimationMode {
    STATIC,
    REVEAL,
    REVEAL_BOUNCE,
    REVEAL_HIDE,
    REVERSED_REVEAL,
    HIDE,
}

internal interface AnimationFunction {
    fun updateShapeProgress(shapeProgress: Pair<Int, Int>, currentFrame: Int, lastIndex: Int, endFrame: Int): Pair<Int, Int>
}

internal class NoAnimationFunction : AnimationFunction {
    override fun updateShapeProgress(
        shapeProgress: Pair<Int, Int>,
        currentFrame: Int,
        lastIndex: Int,
        endFrame: Int
    ): Pair<Int, Int> {
        return shapeProgress
    }
}

internal class RevealAnimationFunction : AnimationFunction {
    override fun updateShapeProgress(
        shapeProgress: Pair<Int, Int>,
        currentFrame: Int,
        lastIndex: Int,
        endFrame: Int
    ): Pair<Int, Int> {
        val revealPointerFunction =
            CubicInOut().ease(currentFrame.toDouble(), 0.0, lastIndex.toDouble(), endFrame.toDouble()).toInt()
        return shapeProgress.copy(second = revealPointerFunction)
    }
}

internal class RevealBounceAnimationFunction : AnimationFunction {
    override fun updateShapeProgress(
        shapeProgress: Pair<Int, Int>,
        currentFrame: Int,
        lastIndex: Int,
        endFrame: Int
    ): Pair<Int, Int> {
        val revealBouncePointerFunction = (sin(currentFrame * PI / endFrame) * lastIndex).toInt()
        return shapeProgress.copy(second = revealBouncePointerFunction)
    }
}

internal class RevealHideAnimationFunction : AnimationFunction {
    override fun updateShapeProgress(
        shapeProgress: Pair<Int, Int>,
        currentFrame: Int,
        lastIndex: Int,
        endFrame: Int
    ): Pair<Int, Int> {
        val revealHideEndPointerFunction =
            CubicInOut().ease(currentFrame.toDouble(), 0.0, (lastIndex).toDouble(), endFrame / 2.0).toInt()
        val revealHideStartPointerFunction =
            CubicInOut().ease(currentFrame - endFrame / 2.0, 0.0, (lastIndex).toDouble(), endFrame / 2.0).toInt()
        return if (currentFrame == endFrame) {
            Pair(0, 0)
        } else if (currentFrame < endFrame / 2) {
            shapeProgress.copy(second = revealHideEndPointerFunction)
        } else {
            shapeProgress.copy(first = revealHideStartPointerFunction)
        }
    }
}

internal class ReversedRevealAnimationFunction : AnimationFunction {
    override fun updateShapeProgress(
        shapeProgress: Pair<Int, Int>,
        currentFrame: Int,
        lastIndex: Int,
        endFrame: Int
    ): Pair<Int, Int> {
        val revealPointerFunction =
            CubicInOut().ease(currentFrame.toDouble(), lastIndex.toDouble(), -lastIndex.toDouble(), endFrame.toDouble()).toInt()
        return shapeProgress.copy(second = revealPointerFunction)
    }
}

internal class HideAnimationFunction : AnimationFunction {
    override fun updateShapeProgress(
        shapeProgress: Pair<Int, Int>,
        currentFrame: Int,
        lastIndex: Int,
        endFrame: Int
    ): Pair<Int, Int> {
        val revealPointerFunction =
            CubicInOut().ease(currentFrame.toDouble(), 0.0, lastIndex.toDouble(), endFrame.toDouble()).toInt()
        return shapeProgress.copy(first = revealPointerFunction)
    }
}
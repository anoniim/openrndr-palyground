package net.solvetheriddle.openrndr.spiral_shapes

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
    private val spiralDensity: Int = 50, // 10..50
    /** Polar angle of the end of the spiral */
    private val spiralEnd: Double = 0.0,
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
            val radius = startRadius + time * (endRadius - startRadius)
            val center = templateResampled[it]
            val newPoint =
                center + Vector2.fromPolar(Polar(360 * time * spiralDensity + spiralEnd, radius))
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
        Pair(0, shapePoints.size) // start full
    }

    /**
     * Expects [frameCount] in range 0..[animationLength]
     */
    fun update(frameCount: Int) {
        val currentFrameCount = (frameCount) % animationLength
        shapeProgress = animationFunction.updateShapeProgress(shapeProgress, currentFrameCount, shapePoints.lastIndex, animationLength)
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
    fun updateShapeProgress(shapeProgress: Pair<Int, Int>, currentFrameCount: Int, lastIndex: Int, animationFrames: Int): Pair<Int, Int>
}

internal class NoAnimationFunction : AnimationFunction {
    override fun updateShapeProgress(
        shapeProgress: Pair<Int, Int>,
        currentFrameCount: Int,
        lastIndex: Int,
        animationFrames: Int
    ): Pair<Int, Int> {
        return shapeProgress
    }
}

internal class RevealAnimationFunction : AnimationFunction {
    override fun updateShapeProgress(
        shapeProgress: Pair<Int, Int>,
        currentFrameCount: Int,
        lastIndex: Int,
        animationFrames: Int
    ): Pair<Int, Int> {
        val revealPointerFunction =
            CubicInOut().ease(currentFrameCount.toDouble(), 0.0, lastIndex.toDouble(), animationFrames.toDouble()).toInt()
        return shapeProgress.copy(second = revealPointerFunction)
    }
}

internal class RevealBounceAnimationFunction : AnimationFunction {
    override fun updateShapeProgress(
        shapeProgress: Pair<Int, Int>,
        currentFrameCount: Int,
        lastIndex: Int,
        animationFrames: Int
    ): Pair<Int, Int> {
        val revealBouncePointerFunction = (sin(currentFrameCount * PI / animationFrames) * lastIndex).toInt()
        return shapeProgress.copy(second = revealBouncePointerFunction)
    }
}

internal class RevealHideAnimationFunction : AnimationFunction {
    override fun updateShapeProgress(
        shapeProgress: Pair<Int, Int>,
        currentFrameCount: Int,
        lastIndex: Int,
        animationFrames: Int
    ): Pair<Int, Int> {
        val revealHideEndPointerFunction =
            CubicInOut().ease(currentFrameCount.toDouble(), 0.0, (lastIndex).toDouble(), animationFrames / 2.0).toInt()
        val revealHideStartPointerFunction =
            CubicInOut().ease(currentFrameCount - animationFrames / 2.0, 0.0, (lastIndex).toDouble(), animationFrames / 2.0).toInt()
        return if (currentFrameCount == animationFrames - 1) {
            Pair(0, 0)
        } else if (currentFrameCount < animationFrames / 2) {
            shapeProgress.copy(second = revealHideEndPointerFunction)
        } else {
            shapeProgress.copy(first = revealHideStartPointerFunction)
        }
    }
}

internal class ReversedRevealAnimationFunction : AnimationFunction {
    override fun updateShapeProgress(
        shapeProgress: Pair<Int, Int>,
        currentFrameCount: Int,
        lastIndex: Int,
        animationFrames: Int
    ): Pair<Int, Int> {
        val revealPointerFunction =
            CubicInOut().ease(currentFrameCount.toDouble(), lastIndex.toDouble(), -lastIndex.toDouble(), animationFrames.toDouble()).toInt()
        return shapeProgress.copy(second = revealPointerFunction)
    }
}

internal class HideAnimationFunction : AnimationFunction {
    override fun updateShapeProgress(
        shapeProgress: Pair<Int, Int>,
        currentFrameCount: Int,
        lastIndex: Int,
        animationFrames: Int
    ): Pair<Int, Int> {
        val revealPointerFunction =
            CubicInOut().ease(currentFrameCount.toDouble(), 0.0, lastIndex.toDouble(), animationFrames.toDouble()).toInt()
        return shapeProgress.copy(first = revealPointerFunction)
    }
}
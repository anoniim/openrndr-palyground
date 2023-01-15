package net.solvetheriddle.openrndr.ideas

import org.openrndr.Program
import org.openrndr.animatable.Animatable
import org.openrndr.animatable.easing.Easing
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.Drawer
import org.openrndr.draw.isolated
import org.openrndr.extra.shapes.grid
import org.openrndr.math.Polar
import org.openrndr.math.Vector2
import org.openrndr.shape.Rectangle
import org.openrndr.shape.ShapeContour
import kotlin.random.Random

/**
 *  This is a template for a live program.
 *
 *  It uses oliveProgram {} instead of program {}. All code inside the
 *  oliveProgram {} can be changed while the program is running.
 */

fun main() = application {
    configure {
        width = 900
        height = 900
    }
    program {

//        extend(ScreenRecorder())

        val faces = generateFaces(drawer.bounds)
        val target = AttentionTarget(Vector2(100.0, 100.0))

        extend {
            with(drawer) {
                clear(ColorRGBa.GRAY.shade(2.0))
                faces.forEach { it.draw(target.position) }
//                test()
            }

            // config target
            target.update()
            target.followMouse()
//            target.draw()
        }

        mouse.buttonUp.listen {
            faces.random().smile()
        }
    }
}

context(Program)
        private fun test() {
    drawer.isolated {
        drawer.translate(bounds.center)
        val a = Vector2(-100.0, -100.0)
        val b = Vector2(100.0, -100.0)
        val abDown = Vector2(0.0, 100.0)
        val c = Vector2(100.0, 100.0)
        val d = Vector2(-100.0, 100.0)

        val shape = org.openrndr.shape.contour {
            moveTo(a)
            curveTo(a, mouse.position, b)
            curveTo(b, abDown, a)
//            curveTo(c, a, d)
//            curveTo(d, b, a)
        }
        drawer.contour(shape)

        drawer.circle(a, 5.0)
        drawer.circle(b, 5.0)
//        circle(c, 5.0)
//        circle(d, 5.0)

    }
}

private fun generateFaces(drawerBounds: Rectangle): List<Face> {
    // config face dimensions and layout
    val faceDiameter = 120.0
    val maxOffset = 55.0
    val grid = drawerBounds.grid(
        faceDiameter, faceDiameter,
        gutterX = maxOffset,
        gutterY = maxOffset
    ).flatten()
    return grid.map {
        val randomOffset = Random.nextDouble(maxOffset) - maxOffset / 2
        Face(it.center + randomOffset, faceDiameter)
    }
}

private class Face(
    val position: Vector2,
    val size: Double
) {

    private val eyeOffset = size / 6
    private val eyeRadius = 4.0
    private val mouth = Mouth(size)
    private var currentExpression = Mouth.Expression.SMILE

    fun smile() {
        mouth.changeSmile()
    }

    context(Drawer)
    fun draw(target: Vector2) {
        val eyeTarget = target - position
        isolated {
            translate(position)
            drawFace()
            drawEyes(eyeTarget)
            drawMouth(target)
        }
    }

    private fun Drawer.drawFace() {
        fill = ColorRGBa.PINK
        stroke = ColorRGBa.BLACK
        strokeWeight = 2.0
        circle(Vector2.ZERO, size / 2)
    }

    private val maxEyeDistance = 15.0

    private val leftEyeOffset = Vector2(-eyeOffset, -eyeOffset)
    private val rightEyeOffset = Vector2(eyeOffset, -eyeOffset)
    private val eyeCenter = Vector2(0.0, -eyeOffset)
    private var currentEyeDirection = eyeCenter.normalized * maxEyeDistance
    private fun Drawer.drawEyes(eyeTarget: Vector2) {
        stroke = null
        fill = ColorRGBa.BLACK
        val targetEyeDirection = (eyeTarget - eyeCenter).normalized * maxEyeDistance
        val dampenedTargetEyeDirection = (targetEyeDirection - currentEyeDirection) * 0.1
        val direction = currentEyeDirection + dampenedTargetEyeDirection
        currentEyeDirection = direction
        circle(leftEyeOffset + direction, eyeRadius)
        circle(rightEyeOffset + direction, eyeRadius)
    }

    private fun Drawer.drawMouth(mousePosition: Vector2) {
        val newExpression = getExpression(mousePosition.distanceTo(position))
        changeExpression(newExpression)
        stroke = ColorRGBa.BLACK
        strokeWeight = 2.0
        contour(mouth.updateMouth())
    }

    private fun getExpression(distanceToTarget: Double): Mouth.Expression {
        return when {
            distanceToTarget < size / 2 -> Mouth.Expression.AFRAID
            distanceToTarget < size * 2.5 -> Mouth.Expression.SAD
            else -> Mouth.Expression.SMILE
        }
    }

    private fun changeExpression(expression: Mouth.Expression) {
        if (currentExpression != expression) {
            currentExpression = expression
            when (expression) {
                Mouth.Expression.AFRAID -> mouth.animateAfraid()
                Mouth.Expression.SAD -> mouth.animateSad()
                else -> mouth.animateSmile()
            }
        } else if (shouldChangeSmile()) {
            mouth.changeSmile()
        }
    }

    private fun shouldChangeSmile() = currentExpression == Mouth.Expression.SMILE && Random.nextDouble() < 0.01

    class Mouth(faceSize: Double) : Animatable() {

        private val mouthScale = faceSize / 4
        private val mouthYPosition = mouthScale * 0.7
        private val mouthTop = -mouthScale / 3.0
        private val mouthBottom = 2 * mouthScale
        private var topControl = mouthBottom
        private var bottomControl = mouthBottom
        private var mouthWidthControl = mouthScale
        private val mouthWidthNormal = mouthScale
        private val mouthWidthAfraid = mouthScale * 0.5
        private val expressionChangeDuration = 500L

        fun changeSmile() {
            val randomSmile = generateRandomSmile()
            val duration = expressionChangeDuration * 3
            ::topControl.cancel()
            ::bottomControl.cancel()
            ::topControl.animate(randomSmile, duration)
            ::bottomControl.animate(randomSmile, duration)
            ::mouthWidthControl.cancel()
            ::mouthWidthControl.animate(mouthWidthNormal, duration)
        }

        fun updateMouth(): ShapeContour {
            updateAnimation()
            return org.openrndr.shape.contour {
                val mouthTipLeft = Vector2(-mouthWidthControl, mouthYPosition)
                val mouthTipRight = Vector2(mouthWidthControl, mouthYPosition)
                moveTo(mouthTipLeft)
                curveTo(Vector2(0.0, topControl), mouthTipRight)
                curveTo(Vector2(0.0, bottomControl), mouthTipLeft)
            }
        }

        fun animateAfraid() {
            ::topControl.cancel()
            val randomDelay = generateRandomDelay(200)
            ::topControl.animate(
                mouthTop, expressionChangeDuration,
                predelayInMs = randomDelay,
                easing = Easing.CubicIn
            )
            ::bottomControl.cancel()
            ::bottomControl.animate(
                mouthBottom * 0.8, expressionChangeDuration,
                predelayInMs = randomDelay,
                easing = Easing.CubicIn
            )
            ::mouthWidthControl.cancel()
            ::mouthWidthControl.animate(
                mouthWidthAfraid, expressionChangeDuration,
                predelayInMs = randomDelay,
                easing = Easing.CubicIn
            )
        }

        fun animateSad() {
            val randomDelay = generateRandomDelay(500)
            ::topControl.cancel()
            ::bottomControl.cancel()
            ::topControl.animate(mouthTop, expressionChangeDuration, predelayInMs = randomDelay)
            ::bottomControl.animate(mouthTop, expressionChangeDuration, predelayInMs = randomDelay)
            ::mouthWidthControl.cancel()
            ::mouthWidthControl.animate(mouthWidthNormal, expressionChangeDuration, predelayInMs = randomDelay)
        }

        fun animateSmile() {
            ::topControl.cancel()
            ::bottomControl.cancel()
            val randomDelay = generateRandomDelay(1000)
            val randomSmile = generateRandomSmile()
            val duration = expressionChangeDuration * 2
            ::topControl.animate(randomSmile, duration, predelayInMs = randomDelay)
            ::bottomControl.animate(randomSmile, duration, predelayInMs = randomDelay)
            ::mouthWidthControl.cancel()
            ::mouthWidthControl.animate(mouthWidthNormal, duration, predelayInMs = randomDelay)
        }

        private fun generateRandomDelay(maxDelay: Long) = Random.nextLong(maxDelay)

        private fun generateRandomSmile() = Random.nextDouble(mouthScale / 2, mouthBottom)

        enum class Expression {
            SMILE,
            SAD,
            AFRAID,
        }
    }
}

@Suppress("unused")
private class AttentionTarget(initial: Vector2) {

    var position = initial

    context(Program)
    fun getRandomDirection(): Vector2 {
        val randomDirection = Polar(org.openrndr.extra.noise.Random.simplex(seconds / 10, 0.0) * 360)
        return Vector2.fromPolar(randomDirection)
    }

    context(Program)
    fun update() {
        val newDirection = getRandomDirection()
        if (drawer.bounds.contains(position + newDirection)) {
            position += newDirection
        } else position = drawer.bounds.center
    }

    context(Program)
    fun followMouse() {
        position = mouse.position
    }

    context (Program)
    fun draw() {
        drawer.fill = ColorRGBa.GREEN
        drawer.circle(position, 10.0)
    }
}

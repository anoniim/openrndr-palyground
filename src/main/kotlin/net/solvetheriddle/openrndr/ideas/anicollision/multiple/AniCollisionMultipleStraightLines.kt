package net.solvetheriddle.openrndr.ideas.anicollision.multiple

import net.solvetheriddle.openrndr.Display
import net.solvetheriddle.openrndr.sketchSize
import org.openrndr.Program
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extensions.Screenshots
import org.openrndr.extra.color.presets.AQUAMARINE
import org.openrndr.extra.color.presets.BLUE_STEEL
import org.openrndr.extra.shapes.RoundedRectangle
import org.openrndr.ffmpeg.ScreenRecorder
import org.openrndr.math.Vector2
import org.openrndr.shape.ShapeContour
import kotlin.math.abs
import kotlin.random.Random

// sketch config
private val useDisplay = Display.LG_ULTRAWIDE
private const val enableScreenshots = false
private const val enableScreenRecording = false

private const val obstacleWidth = 20.0
private const val obstacleSpacing = 5.0

/**
 * Keyboard shortcuts:
 * - press space to reset
 */
fun main() {
    application {
        configure {
            sketchSize(useDisplay)
        }
        program {
            // RECORD
            setupScreenshotsIfEnabled()
            setupScreenRecordingIfEnabled()

            val bullet = createBullet(0.0, drawer.height / 3.0)
//            val obstacles = createObstacles(listOf(bullet))
            val bullet2 = createBullet(-200.0, 2 * drawer.height / 3.0)
            val obstacles = createObstacles(listOf(bullet, bullet2))
            // DRAW
            extend {
                drawer.clear(ColorRGBa.BLACK)
                drawer.fill = ColorRGBa.BLUE_STEEL
                drawer.stroke = ColorRGBa.BLUE_STEEL
                val obstacleContours = obstacles.map { it.contour() }.flatten()
                drawer.contours(obstacleContours)
                obstacles.forEach { it.update() }
                bullet.drawAndUpdate()
                bullet2.drawAndUpdate()
            }

            // UI
//            enableReset(objects)
//            enableSpeedUp(objects)
        }
    }
}

private fun createBullet(xOffset: Double, y: Double) = Bullet(
    xOffset = xOffset,
    y = y,
    speed = Vector2(5.0, 0.0),
    width = 80.0,
)

private fun Program.createObstacles(bullets: List<Bullet>): List<Obstacle> {
    val numOfObstacles = drawer.width / (obstacleWidth + obstacleSpacing)
    return List(numOfObstacles.toInt() + 1) {
        Obstacle(
            bullets = bullets,
            x = obstacleSpacing + it * (obstacleWidth + obstacleSpacing),
            speed = Random.nextDouble(1.0, 4.0),
//            speed = 2.0,
//            direction = Direction.random(),
            direction = Direction.DOWN,
        )
    }
}

private class Bullet(
    val xOffset: Double,
    val y: Double,
    val speed: Vector2,
    val width: Double,
) {

    val height: Double = 20.0
    private val radius: Double = 10.0

    private val initialPosition = Vector2(xOffset - width, y)
    var position = initialPosition

    context(Program)
    fun drawAndUpdate() {
        val contour = RoundedRectangle(position, width, height, radius).contour
        drawer.fill = ColorRGBa.AQUAMARINE
        drawer.stroke = ColorRGBa.AQUAMARINE
        drawer.contour(contour)

        update()
    }

    fun update() {
        position += speed
    }

    fun reset() {
        position = initialPosition
    }
}

enum class Direction(val factor: Double) {
    UP(-1.0),
    DOWN(1.0);

    companion object {
        fun random(): Direction {
            return if (Random.nextBoolean()) UP else DOWN
        }
    }
}

private class Obstacle(
    private val bullets: List<Bullet>,
    private val x: Double,
    private val speed: Double,
    private val direction: Direction,
) {

    private val speedVector = Vector2(0.0, direction.factor * speed)
    private val segments = generateSegments()

    private fun generateSegments(): List<ObstacleSegment> {
        val collisionPoints = bullets.getCollisionPoints(x).sortedBy { it.first.frame }
        return when (direction) {
            Direction.UP -> {
                collisionPoints.mapIndexed { index, it ->
                    when (index) {
                        0 -> listOf(generateFirstSegment(it))
                        collisionPoints.lastIndex -> listOf(generateLastSegment(it))
                        else -> listOf(generateSegment(it, collisionPoints[index + 1]))
                    }
                }.flatten()
            }

            Direction.DOWN -> {
                collisionPoints.mapIndexed { index, it ->
                    when (index) {
                        0 -> listOf(generateFirstSegment(it))
                        collisionPoints.lastIndex -> listOf(generateSegment(collisionPoints[index - 1], it), generateLastSegment(it))
                        else -> listOf(generateSegment(it, collisionPoints[index + 1]))
                    }
                }.flatten()
            }
        }
    }

    private fun generateFirstSegment(collisionPointPair: Pair<CollisionPoint, CollisionPoint>): ObstacleSegment {
        val height = 100.0
        val offset = bullets[0].height - direction.factor * collisionPointPair.first.frame * speed
        return ObstacleSegment(
            collisionPointPair.first.y,
            x = x,
            height = height,
            offset = offset,
            speedVector,
        )
    }

    private fun generateLastSegment(collisionPointPair: Pair<CollisionPoint, CollisionPoint>): ObstacleSegment {
        val height = 100.0
        val offset = -direction.factor * height - direction.factor * collisionPointPair.second.frame * speed
        return ObstacleSegment(
            collisionPointPair.first.y,
            x = x,
            height = height,
            offset = offset,
            speedVector,
        )
    }

    private fun generateSegment(collisionPoint1: Pair<CollisionPoint, CollisionPoint>, collisionPoint2: Pair<CollisionPoint, CollisionPoint>): ObstacleSegment {
        val offsetTop = bullets[0].height - direction.factor * collisionPoint2.first.frame * speed
        val offsetBottom = - direction.factor * collisionPoint1.second.frame * speed
        val height = abs(offsetTop - offsetBottom)
        return ObstacleSegment(
            collisionPoint2.first.y,
            x = x,
            height = height,
            offset = offsetTop,
            speedVector,
        )
    }

    private fun List<Bullet>.getCollisionPoints(x: Double): List<Pair<CollisionPoint, CollisionPoint>> {
        return map { bullet ->
            val collisionStartFrame = (-bullet.xOffset + x + obstacleWidth / 2.0) / bullet.speed.x
            val collisionEndFrame = (-bullet.xOffset + bullet.width + x + obstacleWidth / 2.0) / bullet.speed.x
            Pair(CollisionPoint(bullet.y, collisionStartFrame), CollisionPoint(bullet.y, collisionEndFrame))
        }
    }

    context(Program)
    fun contour(): List<ShapeContour> {
        return segments.map { it.contour() }
    }

    fun update() {
        segments.forEach { it.update() }
    }
}

private class CollisionPoint(
    val y: Double,
    val frame: Double,
)

private class ObstacleSegment(
    y: Double,
    val x: Double,
    val height: Double,
    val offset: Double,
    val speed: Vector2,
) {

    private val radius: Double = 10.0

    private val initialPosition = Vector2(x, y + offset)
    var position = initialPosition

    fun contour(): ShapeContour {
        return RoundedRectangle(position, obstacleWidth, height, radius).contour
    }

    fun update() {
        position += speed
    }
}

//private fun Program.enableReset(bullet: List<Bullet>) {
//    resetOn("space") {
//        bullet.forEach(Bullet::reset)
//    }
//}
//
//fun Program.resetOn(key: String, resetFunction: () -> Unit) {
//    keyboard.keyDown.listen {
//        if (it.name == key) {
//            resetFunction()
//        }
//    }
//}

private fun Program.setupScreenshotsIfEnabled() {
    if (enableScreenshots) {
        extend(Screenshots())
    }
}

private fun Program.setupScreenRecordingIfEnabled() {
    if (enableScreenRecording) {
        extend(ScreenRecorder()) {
//            name = ""
        }
    }
}
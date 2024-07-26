package net.solvetheriddle.openrndr.ideas.anicollision

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
import kotlin.random.Random

// sketch config
private val useDisplay = Display.LG_SQUARE_LEFT
private const val enableScreenshots = false
private const val enableScreenRecording = false

private const val obstacleWidth = 20.0
private const val obstacleSpacing = 5.0

private lateinit var bullet: Bullet

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

            bullet = createBullet()
            val obstacles = createObstacles()
            // DRAW
            extend {
                drawer.clear(ColorRGBa.BLACK)
                drawer.fill = ColorRGBa.BLUE_STEEL
                drawer.stroke = ColorRGBa.BLUE_STEEL
                val obstacleContours = obstacles.map { it.contour() }.flatten()
                drawer.contours(obstacleContours)
                obstacles.forEach { it.update() }
                bullet.drawAndUpdate()
            }

            // UI
//            enableReset(objects)
//            enableSpeedUp(objects)
        }
    }
}

private fun Program.createBullet() = Bullet(
    y = drawer.height / 2.0,
    speed = Vector2(4.0, 0.0),
    width = 60.0,
)

private fun Program.createObstacles(): List<Obstacle> {
    val numOfObstacles = drawer.width / (obstacleWidth + obstacleSpacing)
    return List(numOfObstacles.toInt() + 1) {
        Obstacle(
            x = obstacleSpacing + it * (obstacleWidth + obstacleSpacing),
//            speed = Random.nextDouble(2.0, 5.0),
            speed = 2.0,
            direction = Direction.random()
        )
    }
}

private class Bullet(
    val y: Double,
    val speed: Vector2,
    val width: Double,
) {

    val height: Double = 20.0
    private val radius: Double = 10.0

    private val initialPosition = Vector2(-width, y)
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

enum class Position {
    ABOVE,
    BELOW
}

private class Obstacle(
    private val x: Double,
    private val speed: Double,
    private val direction: Direction,
) {

    private val speedVector = Vector2(0.0, direction.factor * speed)
    private val segments = generateSegments()

    private fun generateSegments(): List<ObstacleSegment> {
        val above = generateSegment(Position.ABOVE)
        val below = generateSegment(Position.BELOW)
        return listOf(
            above,
            below,
        ) + addMore(above, Position.ABOVE) + addMore(below, Position.BELOW)
    }

    private fun addMore(baseObstacle: ObstacleSegment, position: Position): List<ObstacleSegment> {
        var previousOffset = baseObstacle.offset + if (position == Position.BELOW) baseObstacle.height else 0.0
        return List(5) {
            val height = Random.nextInt(1, 5) * bullet.width
            val spacing = bullet.width
            val additionalOffset = if (position == Position.ABOVE) -1.0 * (spacing + height) else spacing
            val currentOffset = previousOffset + additionalOffset
            previousOffset += additionalOffset + if (position == Position.BELOW) height else 0.0
            ObstacleSegment(baseObstacle.x, height, currentOffset, baseObstacle.speed)
        }
    }

    private fun generateSegment(position: Position): ObstacleSegment {
        val height = Random.nextInt(1, 5) * bullet.width
        val subtractHeight = if (shouldSubtractHeight(position)) -direction.factor * height else 0.0
        val bulletHeight = if (shouldAddBulletHeight(position)) bullet.height else 0.0
        val bulletWidth = if (shouldAddBulletWidth(position)) bullet.width else 0.0
        val collisionStartFrame = (bulletWidth + x + obstacleWidth / 2.0) / bullet.speed.x
        return ObstacleSegment(
            x = x,
            height = height,
            offset = bulletHeight + direction.factor * subtractHeight - direction.factor * collisionStartFrame * speed,
            speedVector,
        )
    }

    private fun shouldSubtractHeight(position: Position) = position == Position.ABOVE

    private fun shouldAddBulletWidth(position: Position) =
        (position == Position.ABOVE && direction == Direction.DOWN)
                || (position == Position.BELOW && direction == Direction.UP)

    private fun shouldAddBulletHeight(position: Position) = position == Position.BELOW

    context(Program)
    fun contour(): List<ShapeContour> {
        return segments.map { it.contour() }
    }

    fun update() {
        segments.forEach { it.update() }
    }
}

private class ObstacleSegment(
    val x: Double,
    val height: Double,
    val offset: Double,
    val speed: Vector2,
) {

    private val radius: Double = 10.0

    private val initialPosition = Vector2(x, bullet.y + offset)
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
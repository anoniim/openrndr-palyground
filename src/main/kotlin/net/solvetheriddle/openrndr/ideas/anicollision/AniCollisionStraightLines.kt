package net.solvetheriddle.openrndr.ideas.anicollision

import net.solvetheriddle.openrndr.Display
import net.solvetheriddle.openrndr.sketchSize
import org.openrndr.Program
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extensions.Screenshots
import org.openrndr.extra.shapes.RoundedRectangle
import org.openrndr.ffmpeg.ScreenRecorder
import org.openrndr.math.Vector2
import org.openrndr.shape.ShapeContour

// sketch config
private val useDisplay = Display.LG_ULTRAWIDE
private const val enableScreenshots = false
private const val enableScreenRecording = false

private lateinit var bullet: Bullet

/**
 * Keyboard shortcuts:
 * - press space to reset
 * - hold enter to speed up
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
            val objects: List<Object> = createObstacles() + bullet
            extend {
                // DRAW
                drawer.clear(ColorRGBa.BLACK)
                drawer.fill = ColorRGBa.WHITE
                drawer.stroke = ColorRGBa.WHITE
                drawer.contours(objects.map(Object::contour))

                objects.forEach(Object::update)
            }

            // UI
//            enableReset(objects)
//            enableSpeedUp(objects)
        }
    }
}

private interface Object {
    fun update()
    fun contour(): ShapeContour
}

private class Bullet(
    val y: Double,
    val speed: Vector2,
    val width: Double = 60.0
) : Object {

    private val height: Double = 20.0
    private val radius: Double = 5.0

    private val initialPosition = Vector2(-width, y)
    var position = initialPosition

    override fun contour(): ShapeContour {
        return RoundedRectangle(position, width, height, radius).contour
    }

    override fun update() {
        position += speed
    }

    fun speedUp() {
//        speed = normalSpeed * Vector2(2.0, 0.0)
    }

    fun speedDown() {
//        speed = normalSpeed
    }

    fun reset() {
        position = initialPosition
    }
}

private fun Program.createBullet() = Bullet(
    y = drawer.height / 2.0,
    speed = Vector2(0.5, 0.0),
    width = 60.0,
)

private class Obstacle(
    x: Double,
    val speed: Vector2,
) : Object {

    private val width: Double = 20.0
    private val height: Double = (bullet.width / bullet.speed.x) * speed.y
    private val radius: Double = 5.0

    val targetY = bullet.y - height
    val collisionStartFrame = x / bullet.speed.x

    private val initialPosition = Vector2(x, targetY - (collisionStartFrame * speed.y))
    var position = initialPosition

    override fun contour(): ShapeContour {
        return RoundedRectangle(position, width, height, radius).contour
    }

    override fun update() {
        position += speed
    }
}

private fun Program.createObstacles() = listOf(
    Obstacle(
        x = drawer.width / 40.0,
        speed = Vector2(0.0, 1.0),
    ),
    Obstacle(
        x = drawer.width / 10.0,
        speed = Vector2(0.0, 4.0),
    ),
    Obstacle(
        x = drawer.width / 7.0,
        speed = Vector2(0.0, 1.0),
    ),
    Obstacle(
        x = drawer.width / 5.0,
        speed = Vector2(0.0, 3.0),
    ),
    Obstacle(
        x = drawer.width / 4.0,
        speed = Vector2(0.0, 2.0),
    ),
    Obstacle(
        x = drawer.width / 3.0,
        speed = Vector2(0.0, 1.0),
    ),
    Obstacle(
        x = drawer.width / 2.0,
        speed = Vector2(0.0, 4.0),
    ),
)

private fun Program.enableSpeedUp(bullet: List<Bullet>) {
    keyboard.keyDown.listen {
        bullet.forEach(Bullet::speedUp)
    }
    keyboard.keyUp.listen {
        bullet.forEach(Bullet::speedDown)
    }
}
//
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
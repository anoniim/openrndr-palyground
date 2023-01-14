package explorers

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.CircleBatchBuilder
import org.openrndr.draw.circleBatch
import org.openrndr.extensions.Screenshots
import org.openrndr.extra.noise.random
import org.openrndr.math.Vector2
import org.openrndr.math.map
import tools.PrintSize
import tools.PrintSize.Companion.A4
import kotlin.math.ceil

fun main() {
    application {
        configure {
//            display = displays[1]
            val printSize = A4()
            width = printSize.width
            height = printSize.height
//            width = 700
//            height = 1000
            hideCursor = true
        }

        program {
//            drawer.scale(0.1)
            val margin = height / 100
            val spacing = height / 40 // 80
            val minRadius = height / 18.0 // 200
            val maxRadius = height / 2.0 - 2 * margin
            val numOfCircles = ceil((maxRadius - minRadius) / spacing).toInt()
            val circles = List(numOfCircles) {
                Circle(radius = it * spacing + random(0.0, height/400.0), minRadius = minRadius, maxRadius = maxRadius)
            }

            extend(Screenshots())

            mouse.buttonDown.listen {
//                println("clicked at [${mouse.position.x}, ${mouse.position.y}]")
                println("clicked at [" +
                        "${mouse.position.x.map(0.0..drawer.bounds.width, -2.0..2.0)}, " +
                        "${mouse.position.y.map(0.0..drawer.bounds.height, -2.0..2.0)}]")
            }

            extend {
                drawer.clear(ColorRGBa.WHITE)
                drawer.translate(drawer.bounds.center)

                val batch = drawer.circleBatch {
                    circles.forEach {
                        it.update(
                            -0.475,
                            0.829
                        )
//                        it.update(
//                            mouse.position.x.map(0.0..drawer.bounds.width, -2.0..2.0),
//                            mouse.position.y.map(0.0..drawer.bounds.height, -2.0..2.0),
//                        )
                        it.getShape()
                    }
                }
                drawer.circles(batch)
            }
        }
    }
}

private class Circle(
    private var center: Vector2 = Vector2.ZERO,
    private var radius: Double,
    private var minRadius: Double,
    private val maxRadius: Double
) {

    private var alpha = 1.0
    private val speed = .5

    fun update(xControl: Double, yControl: Double) {
        radius += speed
        if (alpha < 1) alpha += 0.1
        if (radius > maxRadius) {
            radius -= speed
            alpha -= 0.2
            if (alpha < 0) {
                radius = minRadius
            }
        }
        moveCenter(xControl, yControl)
    }

    private fun moveCenter(xControl: Double, yControl: Double) {
        val x = xControl * (maxRadius - radius)
        val y = yControl * (maxRadius - radius)
        center = Vector2(x, y)
    }

    context(CircleBatchBuilder)
    fun getShape() {
        fill = null
        stroke = ColorRGBa.BLACK.opacify(alpha)
        val strokeThinness = drawer.height / 200 // 65.0
        strokeWeight = ((maxRadius - radius) / strokeThinness)
        return circle(center.x, center.y, radius)
    }
}
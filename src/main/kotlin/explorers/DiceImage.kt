package explorers

import org.openrndr.PresentationMode
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.Drawer
import org.openrndr.draw.colorBuffer
import org.openrndr.draw.loadImage
import org.openrndr.extra.fx.distort.Tiles
import org.openrndr.extra.noise.Random.isolated
import org.openrndr.extra.shapes.RoundedRectangle
import org.openrndr.extra.shapes.grid
import org.openrndr.math.Polar
import org.openrndr.math.Vector2
import org.openrndr.shape.*
import java.nio.ByteBuffer

fun main() {
    application {
        configure {
//            display = displays[1]
//            fullscreen = Fullscreen.SET_DISPLAY_MODE
            width = 1600
            height = 900
        }

        program {
//            extend(Screenshots())

            var selectedValue = 0
            mouse.buttonDown.listen {
                selectedValue = (selectedValue + 1) % 6
//                println("clicked at [${mouse.position.x}, ${mouse.position.y}]")
            }

            window.presentationMode = PresentationMode.MANUAL
            window.requestDraw()

            val size = 20.0
            val grid = drawer.bounds.grid(size, size)
            val diePositions = grid
                .flatten()
                .map { it.center }
            val dice = List(6) {
                Die(it + 1, size / 8.0)
            }

            val image = loadImage("data/images/butterfly1.png")
            val filter = Tiles()
            val filtered = colorBuffer(image.width, image.height)

            // -- create a buffer (on CPU) that matches size and layout of the color buffer
            val buffer =
                ByteBuffer.allocateDirect(filtered.width * filtered.height * filtered.format.componentCount * filtered.type.componentSize)

            // -- fill buffer with random data
            for (y in 0 until image.height) {
                for (x in 0 until image.width) {
                    for (c in 0 until image.format.componentCount) {
                        buffer.put((Math.random() * 255).toInt().toByte())
                    }
                }
            }

            // -- rewind the buffer, this is essential as upload will be from the position we left the buffer at
            buffer.rewind()
            // -- write into color buffer
            filtered.write(buffer)

            val newImage = colorBuffer(image.width, image.height)
            // -- download data into buffer
            newImage.write(buffer)

            extend {

//                drawer.image(filtered)

//                filter.xSegments = (drawer.bounds.width / size).toInt()
//                filter.ySegments = (drawer.bounds.height / size).toInt()
                filter.xSegments = 50
                filter.ySegments = 50
                filter.apply(image, filtered)
                drawer.image(filtered)
            }



            extend {
//                drawer.clear(ColorRGBa.DARK_GRAY)

//                diePositions.forEach { position ->
//                    with(drawer) {
//                        dice[(position.x.toInt() / 6) % 6].drawShape(position)
//                    }
//                }

//                val allDice = dice.map {
//                    it.getShape(Vector2.ZERO)
//                }
//                with(drawer) {
//                    dice[selectedValue].drawShape(Vector2.ZERO)
//                }
            }
        }
    }
}

private class Die(
    private val value: Int,
    private val size: Double = 100.0,
) {

    context(Drawer)
    fun drawShape(position: Vector2) {
        isolated {
//            translate(position)
            fill = null
            stroke = ColorRGBa.GRAY
            strokeWeight = size / 15.0
//            rectangle(Rectangle(position, 2 * size, 2 * size))
            val corner = -4.0 * size
            val cornerPosition = position + Vector2(corner, corner)
            contour(RoundedRectangle(cornerPosition, 8 * size, 8 * size, 10.0).contour)
            fill = ColorRGBa.GRAY
            stroke = null
            shapes(compound {
                union {
                    getShapeForValue(value, position)
                }
            })
        }
    }

    context(CompoundBuilder.OpBuilder) private fun getShapeForValue(value: Int, position: Vector2) {
        when (value) {
            1 -> dieOne(position)
            2 -> dieTwo(position)
            3 -> dieThree(position)
            4 -> dieFour(position)
            5 -> dieFive(position)
            6 -> dieSix(position)
        }

    }

    private fun CompoundBuilder.OpBuilder.dieOne(position: Vector2) {
        shape(Circle(position, size).shape)
    }

    private fun CompoundBuilder.OpBuilder.dieTwo(position: Vector2) {
        shape(Circle(position + Polar(45.0, size * 2).cartesian, size).shape)
        shape(Circle(position + Polar(5 * 45.0, size * 2).cartesian, size).shape)
    }

    private fun CompoundBuilder.OpBuilder.dieThree(position: Vector2) {
        shape(Circle(position + Polar(3 * 45.0, size * 2.5).cartesian, size).shape)
        shape(Circle(position, size).shape)
        shape(Circle(position + Polar(7 * 45.0, size * 2.5).cartesian, size).shape)
    }

    private fun CompoundBuilder.OpBuilder.dieFour(position: Vector2) {
        shape(Circle(position + Polar(45.0, size * 2).cartesian, size).shape)
        shape(Circle(position + Polar(3 * 45.0, size * 2).cartesian, size).shape)
        shape(Circle(position + Polar(5 * 45.0, size * 2).cartesian, size).shape)
        shape(Circle(position + Polar(7 * 45.0, size * 2).cartesian, size).shape)
    }

    private fun CompoundBuilder.OpBuilder.dieFive(position: Vector2) {
        shape(Circle(position + Polar(45.0, size * 2.5).cartesian, size).shape)
        shape(Circle(position + Polar(3 * 45.0, size * 2.5).cartesian, size).shape)
        shape(Circle(position, size).shape)
        shape(Circle(position + Polar(5 * 45.0, size * 2.5).cartesian, size).shape)
        shape(Circle(position + Polar(7 * 45.0, size * 2.5).cartesian, size).shape)
    }

    private fun CompoundBuilder.OpBuilder.dieSix(position: Vector2) {
        shape(Circle(position + Vector2(-1.5 * size, -2.5 * size), size).shape)
        shape(Circle(position + Vector2(-1.5 * size, 0.0), size).shape)
        shape(Circle(position + Vector2(-1.5 * size, 2.5 * size), size).shape)
        shape(Circle(position + Vector2(1.5 * size, -2.5 * size), size).shape)
        shape(Circle(position + Vector2(1.5 * size, 0.0), size).shape)
        shape(Circle(position + Vector2(1.5 * size, 2.5 * size), size).shape)
    }
}

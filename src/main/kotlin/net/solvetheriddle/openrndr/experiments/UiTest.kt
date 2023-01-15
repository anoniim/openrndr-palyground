import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.loadFont
import org.openrndr.draw.loadImage
import org.openrndr.draw.tint
import org.openrndr.panel.ControlManager
import org.openrndr.panel.elements.button
import org.openrndr.panel.elements.clicked
import org.openrndr.panel.elements.layout
import org.openrndr.panel.layout
import kotlin.math.cos
import kotlin.math.sin

fun main() = application {
    configure {
        width = 768
        height = 576
    }

    program {
        var color = ColorRGBa.GRAY.shade(0.250)
        extend(ControlManager()) {
            layout {
                button {
                    label = "state.label"
                    println(label)
//                    color = ColorRGBa(Math.random(), Math.random(), Math.random())
                    clicked {
                        color = ColorRGBa(Math.random(), Math.random(), Math.random())
                    }
                }
            }
        }

        extend {
            drawer.clear(color)
        }

    }
}

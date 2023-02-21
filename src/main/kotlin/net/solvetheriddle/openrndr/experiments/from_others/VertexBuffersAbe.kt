package net.solvetheriddle.openrndr.experiments.from_others

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.DrawPrimitive
import org.openrndr.draw.shadeStyle
import org.openrndr.draw.vertexBuffer
import org.openrndr.draw.vertexFormat
import org.openrndr.extra.color.presets.DARK_GOLDEN_ROD
import org.openrndr.extra.noise.Random
import org.openrndr.extra.noise.simplex3D
import org.openrndr.extra.noise.uniform
import org.openrndr.extra.noise.withVector2Output
import org.openrndr.extra.shadestyles.linearGradient
import org.openrndr.math.Vector2

fun main() = application {
    configure {
        width = 800
        height = 800
    }
    program {
        // create a buffer and specify its format and size.
        val geometry = vertexBuffer(vertexFormat {
            position(3)
            color(4)
        }, 3 * 1_000)

        // create an area with some padding around the edges
        val area = drawer.bounds.offsetEdges(-50.0)

        // populate the vertex buffer.
        geometry.put {
            for (i in 0 until geometry.vertexCount / 3) {
                val p = area.uniform()
                write(p.vector3(z = 0.0)) // v1
                write(Random.vector4(0.0, 1.0)) // color

                write((p + Vector2(1.0, 1.0) * 30.0).vector3(z = 0.0)) // v2
                write(Random.vector4(0.0, 1.0)) // color

                write((p + Vector2(-1.0, 1.0) * 30.0).vector3(z = 0.0)) // v3
                write(Random.vector4(0.0, 1.0)) // color
            }
        }

        extend {
            // shader using the color attributes from our buffer
            drawer.shadeStyle = shadeStyle {
                fragmentTransform = "x_fill = va_color;"
            }
//            val color0 = ColorRGBa.DARK_GOLDEN_ROD.opacify(0.0)
//            val color1 = ColorRGBa.DARK_GOLDEN_ROD.opacify(1.0)
//            drawer.shadeStyle = linearGradient(color0, color1)
            drawer.vertexBuffer(geometry, DrawPrimitive.TRIANGLE_STRIP)
        }
    }
}
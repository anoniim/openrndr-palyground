package net.solvetheriddle.openrndr.experiments.from_others

import net.solvetheriddle.openrndr.Display
import net.solvetheriddle.openrndr.sketchSize
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.DrawQuality
import org.openrndr.extra.color.presets.*
import org.openrndr.extra.noise.Random
import org.openrndr.extra.noise.uniform
import org.openrndr.math.Vector2
import org.openrndr.math.Vector4

fun main() = application {
    configure {
        sketchSize(Display.LG_ULTRAWIDE)
    }
    program {
        val count = 12
        val colorPalette: List<ColorRGBa> = listOf(
            ColorRGBa.PINK,
            ColorRGBa.OLIVE,
            ColorRGBa.GRAY,
            ColorRGBa.MAGENTA,
            ColorRGBa.RED,
            ColorRGBa.DARK_SALMON,
            ColorRGBa.CYAN,
            ColorRGBa.DARK_SEA_GREEN,
            ColorRGBa.YELLOW,
            ColorRGBa.SEASHELL,
            ColorRGBa.CHARTREUSE,
            ColorRGBa.OLD_LACE,
        )

        val segs = List(count) {
//            drawer.bounds.uniform(50.0).vector3(z = 0.0)
            Vector2(100.0 + it * 10.0, 100.0 + it * 20.0).vector3(z = 0.0)
        }
        val thicks = List(count + 1) {
//            Random.double(1.0, 4.0)
            if (it == 2) 3.0 else 10.0 * it
        }
        val colors = List(count) {
//            ColorRGBa.fromVector(Vector4.uniform(0.0, 1.0))
            colorPalette[it]
        }

        drawer.drawStyle.quality = DrawQuality.QUALITY
        extend {
            drawer.lineSegments(segs, thicks, colors)
        }
    }
}
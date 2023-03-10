package net.solvetheriddle.openrndr.experiments

import net.solvetheriddle.openrndr.Display
import net.solvetheriddle.openrndr.sketchSize
import org.openrndr.application
import org.openrndr.color.ColorRGBa

fun main() = application {
    configure {
//        width = 2560 // 1/3 of 3440 = 1146
//        height = 1550
//        position = IntVector2(1280, 0)
        sketchSize(Display.LG_ULTRAWIDE)
//        net.solvetheriddle.openrndr.sketchSize(DisplayConfig.FULLSCREEN)

//        windowResizable = true
        title = ""

//        fullscreen = Fullscreen.CURRENT_DISPLAY_MODE // uses display resolution
//        fullscreen = Fullscreen.SET_DISPLAY_MODE // Changes display resolution to width/height
    }

    program {
        extend {
            drawer.clear(ColorRGBa.PINK)
        }
    }
}

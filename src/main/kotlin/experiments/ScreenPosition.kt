package experiments

import sketchSize
import org.openrndr.application
import org.openrndr.color.ColorRGBa

fun main() = application {
    configure {
//        width = 2560 // 1/3 of 3440 = 1146
//        height = 1550
//        position = IntVector2(1280, 0)
        sketchSize(Displays.LG_ULTRAWIDE)

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

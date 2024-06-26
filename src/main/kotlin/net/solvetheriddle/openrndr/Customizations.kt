@file:Suppress("unused")

package net.solvetheriddle.openrndr

import org.openrndr.Configuration
import org.openrndr.Fullscreen
import org.openrndr.math.IntVector2

fun Configuration.sketchSize(config: SketchSizeConfig) {
    if (config is Display) {
        width = config.width
        height = config.height
        position = IntVector2(config.xPosition, config.yPosition)

    } else if (config == Display.FULLSCREEN) {
        fullscreen = Fullscreen.CURRENT_DISPLAY_MODE
    }
}

interface SketchSizeConfig

enum class Display(val width: Int, val height: Int, val xPosition: Int, val yPosition: Int = 0): SketchSizeConfig {
    LG_ULTRAWIDE(2560, 1550, 1280),
    LG_ULTRAWIDE_LEFT(2560, 1550, 0),
    LG_SQUARE_LEFT(1080, 1080, 100, (LG_ULTRAWIDE.height - 1080) / 2),
    LG_PORTRAIT_LEFT(630, 1305, 325, (LG_ULTRAWIDE.height - 1305) / 2),
    MACBOOK_AIR(1500, 997, 500),
    PRINT_A6(1485,1050,0),
    ;
    object FULLSCREEN: SketchSizeConfig
}
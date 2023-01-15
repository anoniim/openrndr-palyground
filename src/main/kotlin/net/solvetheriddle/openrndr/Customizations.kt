@file:Suppress("unused")

package net.solvetheriddle.openrndr

import org.openrndr.Configuration
import org.openrndr.Fullscreen
import org.openrndr.math.IntVector2

fun Configuration.sketchSize(config: SketchSizeConfig) {
    if (config is Display) {
        width = config.width
        height = config.height
        position = IntVector2(config.xPosition, 0)

    } else if (config == SketchSizeConfig.FULLSCREEN) {
        fullscreen = Fullscreen.CURRENT_DISPLAY_MODE
    }
}

sealed interface SketchSizeConfig {
    object FULLSCREEN: SketchSizeConfig
}

enum class Display(val width: Int, val height: Int, val xPosition: Int): SketchSizeConfig {
    LG_ULTRAWIDE(2560, 1550, 1280),
    MACBOOK_AIR(1500, 997, 500),
}
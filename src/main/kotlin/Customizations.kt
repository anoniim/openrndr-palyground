import org.openrndr.Configuration
import org.openrndr.math.IntVector2

fun Configuration.sketchSize(display: Displays) {
    width = display.width
    height = display.height
    position = IntVector2(display.xPosition, 0)

}

enum class Displays(val width: Int, val height: Int, val xPosition: Int) {
    LG_ULTRAWIDE(2560, 1550, 1280)
}
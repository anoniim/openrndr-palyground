@file:Suppress("unused")

package maurer

import org.openrndr.Program
import org.openrndr.animatable.Animatable
import org.openrndr.animatable.easing.Easing
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.*
import org.openrndr.extensions.Screenshots
import org.openrndr.extra.fx.color.LumaOpacity
import org.openrndr.ffmpeg.ScreenRecorder
import org.openrndr.math.Vector2
import org.openrndr.math.asRadians
import org.openrndr.panel.ControlManager
import org.openrndr.panel.elements.Range
import org.openrndr.panel.elements.Slider
import org.openrndr.panel.elements.slider
import org.openrndr.panel.layout
import org.openrndr.shape.contour
import kotlin.math.*

// config Manual or Pre-defined
//private val maurer.aConfig = maurer.AnimationConfig(0.5, 1.0, 2.0, 10_000, Easing.SineInOut)
private val aConfig = AnimationConfig.A6

//private const val screenWidth = 896
//private const val screenHeight = 896
private const val screenWidth = 1163
private const val screenHeight = 900

//private const val maurer.screenWidth = 1748
//private const val maurer.screenHeight = 1240
//private const val radius = screenHeight / 2 * 0.95
private const val radius = 896 / 2 * 0.95
private val initialN = aConfig.n1
private const val initialD = 3.1
private const val closeShape = false
private const val curvesEnabled = true

private val rose = MaurerRose()
private lateinit var nSlider: Slider

fun main() {
    application {
        configure {
            width = screenWidth
            height = screenHeight
        }
        program {
//            extend(Screenshots()) {
//                name = "screenshots/maurer_rose_${maurer.aConfig.serial}.png"
//            }
            extend(ScreenRecorder()) {
                name = "maurer_rose_vid_${maurer.aConfig.serial}"
            }

//            maurer.addUi()
//            maurer.enableKeyboardControls()

            val image = maurer.BackgroundImage("data/images/otis_picture-background.png")
            draw(maurer.rose, image)
//            draw(rose)

            enableRoseAnimation()
        }
    }
}

class BackgroundImage(imagePath: String) {
    val image = loadImage(imagePath)
    val filter = LumaOpacity()
    val filtered = colorBuffer(image.width, image.height)
}

private fun Program.enableRoseAnimation() {
    // config animation on key or at certain point of animation (frame)
//    maurer.animateOnKey("space") {
    extend {
        if (frameCount == 250) {
            val animationDuration = aConfig.animationDuration
            val animationEasing = aConfig.animationEasing
            rose.animateN(aConfig.n2, animationDuration, easing = animationEasing)
            rose.animateN(aConfig.n3, animationDuration, animationDuration, animationEasing)
            rose.animateN(aConfig.n2, animationDuration, 2 * animationDuration, animationEasing)
            rose.animateN(aConfig.n1, animationDuration, 3 * animationDuration, animationEasing)
        }
    }
}

private fun Program.draw(rose: MaurerRose, image: BackgroundImage? = null) {
    extend {
        drawer.clear(ColorRGBa.BLACK)
        // config animate the background
        val time = frameCount/30.0 / 2
//        val time = 0.0
        if (image != null && time < 3 * PI / 2) {
            with(image.filter) {
                backgroundOpacity = 1.0
                foregroundOpacity = cos(time)
                apply(image.image, image.filtered)
            }
            drawer.image(image.filtered, 0.0, 0.0, width = 1163.0, height = 900.0)
            if (time > PI) {
                val opacityFactor = sin(time - PI)
                drawer.fill = ColorRGBa.BLACK.opacify(opacityFactor)
                drawer.contour(drawer.bounds.contour)
            }
        }
        drawer.translate(drawer.bounds.center)
        drawer.translate(0.0, 6.0)
        rose.draw()
        rose.updateAnimation()
    }
}

private class MaurerRose() : Animatable() {

    var n = initialN // number of petals
    var d = initialD // angle factor

    fun animateN(targetValue: Double, duration: Long, predelay: Long = 0, easing: Easing = Easing.CubicInOut) {
//        ::n.cancel()
        ::n.animate(targetValue, duration, easing, predelay)
    }

    context(Program)
    fun draw() {
        val c = contour {
            val firstPoint = getPointForAngle(0)
            moveTo(firstPoint)
            // config reveal maurer.rose gradually
            val numOfConnectedPoints = 360
//            val numOfConnectedPoints = frameCount.coerceAtMost(360)
            for (angle in 0..numOfConnectedPoints) {
                val nextPoint = getPointForAngle(angle)
                if (curvesEnabled) {
                    continueTo(nextPoint)
                } else {
                    lineTo(nextPoint)
                }
            }
            if (closeShape) {
                if (curvesEnabled) {
                    continueTo(firstPoint)
                } else {
                    lineTo(firstPoint)
                }
            }
        }
        drawer.fill = null
        // config fade out maurer.rose
        drawer.stroke = ColorRGBa.WHITE.opacify(0.9)
//        drawer.stroke = ColorRGBa.WHITE.opacify(1 - seconds/5)
        drawer.contour(c)
    }

    private fun getPointForAngle(angle: Int): Vector2 {
        val k = angle * d.asRadians
        val r = radius * sin(n * k)
        val x = r * cos(k)
        val y = r * sin(k)
        return Vector2(x, y)
    }
}

private fun Program.addUi() {
    extend(ControlManager()) {
        layout {
            nSlider = slider {
                label = "n"
                range = Range(0.0, 300.0)
                value = initialN
                precision = 4
                events.valueChanged.listen {
                    rose.n = it.newValue
                }
            }
            slider {
                label = "d"
                range = Range(0.0, 300.0)
                value = initialD
                precision = 1
                events.valueChanged.listen {
                    rose.d = it.newValue
                }
            }
            // TODO add checkbox for maurer.curvesEnabled
        }
    }
}

private fun Program.animateOnKey(keyName: String, function: () -> Unit) {
    keyboard.keyUp.listen {
        if (it.name == keyName) {
            function()
        }
    }
}

private fun Program.enableKeyboardControls() {
    if (::nSlider.isInitialized) {
        keyboard.keyRepeat.listen {
            when (it.name) {
                "a" -> nSlider.value -= 1.0
                "s" -> nSlider.value -= 0.1
                "d" -> nSlider.value -= 0.01
                "f" -> nSlider.value -= 0.001
                "g" -> nSlider.value -= 0.0005
                "h" -> nSlider.value += 0.0005
                "j" -> nSlider.value += 0.001
                "k" -> nSlider.value += 0.01
                "l" -> nSlider.value += 0.1
                ";" -> nSlider.value += 1.0
            }
        }
    } else {
        keyboard.keyRepeat.listen {
            when (it.name) {
                "a" -> rose.n -= 1.0
                "s" -> rose.n -= 0.1
                "d" -> rose.n -= 0.01
                "f" -> rose.n -= 0.001
                "g" -> rose.n -= 0.0005
                "h" -> rose.n += 0.0005
                "j" -> rose.n += 0.001
                "k" -> rose.n += 0.01
                "l" -> rose.n += 0.1
                ";" -> rose.n += 1.0
            }
        }
    }
}

private open class AnimationConfig(
    val serial: Int,
    val n1: Double,
    val n2: Double,
    val n3: Double,
    val animationDuration: Long,
    val animationEasing: Easing
) {

    object A1 : AnimationConfig(
        serial = 1,
        n1 = .5,
        n2 = 1.0,
        n3 = 2.0,
        animationDuration = 10_000L,
        animationEasing = Easing.SineInOut,
    )

    object A2 : AnimationConfig(
        // Miso
        serial = 2,
        n1 = 2.0,
        n2 = 3.0,
        n3 = 4.0,
        animationDuration = 10_000L,
        animationEasing = Easing.SineInOut,
    )

    object A3 : AnimationConfig(
        // Filip
        serial = 3,
        n1 = 4.0,
        n2 = 5.0,
        n3 = 6.0,
        animationDuration = 10_000L,
        animationEasing = Easing.SineInOut,
    )

    object A4 : AnimationConfig(
        // Miruna
        serial = 4,
        n1 = 6.0,
        n2 = 7.005,
        n3 = 9.0,
        animationDuration = 20_000L,
        animationEasing = Easing.CubicInOut,
    )

    object A5 : AnimationConfig(
        // Terka
        serial = 5,
        n1 = 9.0,
        n2 = 11.0,
        n3 = 13.0,
        animationDuration = 20_000L,
        animationEasing = Easing.CubicInOut,
    )

    object A6 : AnimationConfig(
        // Otis (stage)
        serial = 6,
        n1 = 13.0,
        n2 = 14.0111,
        n3 = 15.0144,
        animationDuration = 20_000L,
        animationEasing = Easing.CubicInOut,
    )

    object A7 : AnimationConfig(
        // Isa (butterfly)
        serial = 7,
        n1 = 15.0144,
        n2 = 17.0186,
        n3 = 19.0226,
        animationDuration = 20_000L,
        animationEasing = Easing.CubicInOut,
    )
}


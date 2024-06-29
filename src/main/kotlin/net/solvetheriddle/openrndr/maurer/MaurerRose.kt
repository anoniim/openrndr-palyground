@file:Suppress("unused")

package net.solvetheriddle.openrndr.maurer

import net.solvetheriddle.openrndr.Display
import net.solvetheriddle.openrndr.sketchSize
import org.openrndr.KeyEvent
import org.openrndr.Program
import org.openrndr.animatable.Animatable
import org.openrndr.animatable.easing.Easing
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.*
import org.openrndr.extensions.Screenshots
import org.openrndr.extra.color.presets.DARK_GREY
import org.openrndr.extra.color.presets.GREY
import org.openrndr.extra.fx.color.LumaOpacity
import org.openrndr.ffmpeg.ScreenRecorder
import org.openrndr.math.Vector2
import org.openrndr.math.asRadians
import org.openrndr.panel.ControlManager
import org.openrndr.panel.elements.*
import org.openrndr.panel.layout
import org.openrndr.shape.ContourBuilder
import org.openrndr.shape.ShapeContour
import org.openrndr.shape.contour
import java.io.File
import kotlin.math.*

// sketch config
private val useDisplay = Display.MACBOOK_AIR // Display.MACBOOK_AIR
private const val showUi = true
private const val enableScreenshots = false
private const val enableScreenRecording = false

// config background image
@Suppress("RedundantNullableReturnType", "RedundantSuppression") // can be set to null
private val backgroundImage: String? = null // "data/images/otis_picture-background.png"
private var fadeOutBackground = false

// config Manual or Pre-defined
//private val aConfig = AnimationConfig(0.5, 1.0, 2.0, 10_000, Easing.SineInOut)
private val aConfig = AnimationConfig.Something1

private val initialN = aConfig.n1
private const val initialD = 28.8 // For AnimationConfig.AX = 3.1
private const val lineOpacity = 0.6
private const val fillFactor = 0.95
private const val closeShape = false
private var curvesEnabled = false
private const val revealDuration = 5.0
private const val fadeOutDuration = 3.0 // seconds

private val rose = MaurerRose()
private val seedStore = File("data/maurer_roses_store.txt").apply { createNewFile() }
private lateinit var smallFont: FontImageMap
private lateinit var bigFont: FontImageMap

/**
 * This program draws and animates Maurer Rose - https://en.wikipedia.org/wiki/Maurer_rose.
 *
 * It allows to control the number of petals (N) and the angle factor (D) by sliders and keyboard.
 *  - Use A S D F G T R E W Q keys to decrease N
 *  - Use ; L K J H Y U I O P keys to increase N
 *  - Use Z X C V B keys to decrease D
 *  - Use N M , . / keys to increase D
 *
 *  Press ESCAPE to fade out the rose
 *  Press ENTER to fade in the rose
 *  Press RIGHT SHIFT to reveal the rose gradually
 */
@Suppress("GrazieInspection")
fun main() {
    application {
        configure {
            sketchSize(useDisplay)
        }
        program {
            addUiIfEnabled()
            enableKeyboardControls()
            setupScreenshotsIfEnabled()
            setupScreenRecordingIfEnabled()

            // DRAW
            draw(rose, backgroundImage)

            // ANIMATE
            enableRoseAnimation()

            enableCheckpointView()
        }
    }
}

private fun Program.draw(rose: MaurerRose, imagePath: String?) {
    extend {
        drawer.clear(ColorRGBa.BLACK)
        drawBackgroundIfSet(imagePath)
        drawer.translate(drawer.bounds.center)
        drawer.translate(0.0, 6.0)
        rose.draw()
        rose.updateAnimation()
        drawer.translate(0.0, -6.0)
        drawer.translate(-drawer.bounds.center)
    }
}

private fun Program.drawBackgroundIfSet(imagePath: String?) {
    if (imagePath != null) {
        val image = BackgroundImage(imagePath)
        val time = if (fadeOutBackground) frameCount / 30.0 / 2 else 0.0
        if (time < 3 * PI / 2) {
            applyFilter(image, time)
            drawer.image(image.filtered, 0.0, 0.0, width = 1163.0, height = 900.0)
            fadeOutIntoBlack(time)
        }
    }
}

private fun applyFilter(image: BackgroundImage, time: Double) {
    with(image.filter) {
        backgroundOpacity = 1.0
        foregroundOpacity = cos(time)
        apply(image.image, image.filtered)
    }
}

private fun Program.fadeOutIntoBlack(time: Double) {
    if (time > PI) {
        val opacityFactor = sin(time - PI)
        drawer.fill = ColorRGBa.BLACK.opacify(opacityFactor)
        drawer.contour(drawer.bounds.contour)
    }
}

class BackgroundImage(imagePath: String) {
    val image = loadImage(imagePath)
    val filter = LumaOpacity()
    val filtered = colorBuffer(image.width, image.height)
}

private class MaurerRose : Animatable() {

    var n = initialN // number of petals
    var d = initialD // angle factor

    fun animateN(targetValue: Double, duration: Long, predelay: Long = 0, easing: Easing = Easing.CubicInOut) {
//        ::n.cancel()
        ::n.animate(targetValue, duration, easing, predelay)
    }

    context(Program)
    fun draw() {
        drawer.fill = null
        drawer.stroke = when (visibility) {
            Visibility.VISIBLE -> ColorRGBa.WHITE.opacify(lineOpacity)
            Visibility.FADE_OUT -> ColorRGBa.WHITE.opacify(lineOpacity - (seconds - visibilityChangeTime) / fadeOutDuration)
            Visibility.FADE_IN -> ColorRGBa.WHITE.opacify(min(lineOpacity, (seconds - visibilityChangeTime) / fadeOutDuration))
        }
        drawer.contour(shapeContour())
    }

    private fun Program.shapeContour(): ShapeContour {
        val c = contour {
            val radius = drawer.height / 2.0 * fillFactor
            val firstPoint = getPointForAngle(0, radius)
            moveTo(firstPoint)
            val numOfConnectedPoints = 360
            for (angle in 0..numOfConnectedPoints) {
                val nextPoint = getPointForAngle(angle, radius)
                if (curvesEnabled) continueTo(nextPoint) else lineTo(nextPoint)
            }
            closeShapeIfEnabled(firstPoint)
        }
        return if (revealRoseGradually) c.sub(0.0, min((seconds - revealChangeTime) / revealDuration, 1.0)) else c
    }

    private fun ContourBuilder.closeShapeIfEnabled(firstPoint: Vector2) {
        if (closeShape) {
            if (curvesEnabled) continueTo(firstPoint) else lineTo(firstPoint)
        }
    }

    private fun getPointForAngle(angle: Int, radius: Double): Vector2 {
        val k = angle * d.asRadians
        val r = radius * sin(n * k)
        val x = r * cos(k)
        val y = r * sin(k)
        return Vector2(x, y)
    }
}

private enum class Visibility {
    VISIBLE, FADE_OUT, FADE_IN
}

private var visibility = Visibility.VISIBLE
private var visibilityChangeTime = 0.0
private var revealRoseGradually = false
private var revealChangeTime = 0.0

private fun Program.enableRoseAnimation() {
    executeOnKey("space") {
        val animationDuration = aConfig.animationDuration
        val animationEasing = aConfig.animationEasing
        rose.animateN(aConfig.n2, animationDuration, easing = animationEasing)
        rose.animateN(aConfig.n3, animationDuration, animationDuration, animationEasing)
        rose.animateN(aConfig.n2, animationDuration, 2 * animationDuration, animationEasing)
        rose.animateN(aConfig.n1, animationDuration, 3 * animationDuration, animationEasing)
    }
    executeOnKey("escape") {
        visibility = Visibility.FADE_OUT
        visibilityChangeTime = seconds
        revealRoseGradually = false
    }
    executeOnKey("enter") {
        visibility = Visibility.FADE_IN
        visibilityChangeTime = seconds

    }
    executeOnKey("right-shift") {
        visibility = Visibility.VISIBLE
        revealRoseGradually = true
        revealChangeTime = seconds
    }
}

private fun Program.executeOnKey(keyName: String, function: () -> Unit) {
    keyboard.keyUp.listen {
        if (it.name == keyName) {
            function()
        }
    }
}

private lateinit var nSlider: Slider
private lateinit var dSlider: Slider

private fun Program.addUiIfEnabled() {

    if (showUi) extend(ControlManager()) {
        layout {
            addNSlider()
            addDSlider()
            addCurvesButton()
        }
    }
}

private fun Body.addNSlider() {
    nSlider = slider {
        label = "n"
        range = Range(0.0, 300.0)
        value = initialN
        precision = 6
        events.valueChanged.listen {
            rose.n = it.newValue
        }
    }
}

private fun Body.addDSlider() {
    dSlider = slider {
        label = "d"
        range = Range(0.0, 300.0)
        value = initialD
        precision = 6
        events.valueChanged.listen {
            rose.d = it.newValue
        }
    }
}

private fun Body.addCurvesButton() {
    button {
        fun getCurvesButtonLabel() = if (curvesEnabled) "Curves ON" else "Curves OFF"
        label = getCurvesButtonLabel()
        events.clicked.listen {
            curvesEnabled = !curvesEnabled
            label = getCurvesButtonLabel()
        }
    }
}

private var lastSelectedSlot = -1
private val seeds = loadSeeds()
private fun loadSeeds(): MutableList<RoseSeed> {
    val loadedSeeds = seedStore.readLines().map {
        val (nValue, dValue) = it.split(",")
        RoseSeed(nValue = nValue.toDouble(), dValue = dValue.toDouble())
    }.toMutableList()
    return if (loadedSeeds.size < 9) MutableList(9) { RoseSeed(nValue = 0.0, dValue = 0.0) } else loadedSeeds
}

private fun Program.enableCheckpointView() {
    smallFont = loadFont("data/fonts/Rowdies-Light.ttf", 12.0)
    bigFont = loadFont("data/fonts/Rowdies-Bold.ttf", 40.0)
    onNumberKeys { slot ->
        lastSelectedSlot = slot
        val seed = RoseSeed(nValue = rose.n, dValue = rose.d)
        seeds[slot] = seed
        seedStore.put(seeds)
    }
    extend {
        seeds.forEachIndexed { index, seed ->
            drawCheckPoint(index, seed.nValue, seed.dValue)
        }
    }
}

private fun File.put(seeds: List<RoseSeed>) {
    writeText(seeds.joinToString("\n") { "${it.nValue},${it.dValue}" })
}

private fun Program.drawCheckPoint(slot: Int, nValue: Double, dValue: Double) {
    drawer.translate(10.0, 160.0 + slot * 40.0)
    drawer.fill = if (lastSelectedSlot == slot) ColorRGBa.DARK_GREY else ColorRGBa.GREY
    drawer.fontMap = bigFont
    drawer.text((slot + 1).toString(), 0.0, 0.0)
    drawer.fontMap = smallFont
    drawer.text("N: $nValue", 28.0, -15.0)
    drawer.text("D: $dValue", 28.0, 0.0)
    drawer.translate(-10.0, -(160.0 + slot * 40.0))
}

private fun Program.enableKeyboardControls() {
    // Control N
    val updateNSlider: (KeyEvent) -> Unit = { keyEvent -> keyEvent.mapAsdfKeyRow { nSlider.value += it } }
    val setNValue: (KeyEvent) -> Unit = { keyEvent -> keyEvent.mapAsdfKeyRow { rose.n += it } }
    updateValue(updateNSlider, setNValue)
    // Control D
    val updateDSlider: (KeyEvent) -> Unit = { keyEvent -> keyEvent.mapZxcvKeyRow { dSlider.value += it } }
    val setDValue: (KeyEvent) -> Unit = { keyEvent -> keyEvent.mapZxcvKeyRow { rose.d += it } }
    updateValue(updateDSlider, setDValue)
}

private fun Program.updateValue(updateSlider: (KeyEvent) -> Unit, setValue: (KeyEvent) -> Unit) {
    if (showUi) {
        keyboard.keyRepeat.listen(updateSlider)
        keyboard.keyDown.listen(updateSlider)
    } else {
        keyboard.keyRepeat.listen(setValue)
        keyboard.keyDown.listen(setValue)
    }
}

private fun Program.setupScreenRecordingIfEnabled() {
    if (enableScreenRecording) {
        extend(ScreenRecorder()) {
            name = "maurer_rose_vid_${aConfig.serial}"
        }
    }
}

private fun Program.setupScreenshotsIfEnabled() {
    if (enableScreenshots) {
        extend(Screenshots()) {
            name = "screenshots/maurer_rose_${aConfig.serial}.png"
        }
    }
}

private fun KeyEvent.mapAsdfKeyRow(setValue: (Double) -> Unit) {
    when (name) {
        "a" -> setValue(-1.0)
        "s" -> setValue(-0.1)
        "d" -> setValue(-0.01)
        "f" -> setValue(-0.001)
        "g" -> setValue(-0.0005)
        "t" -> setValue(-0.0001)
        "r" -> setValue(-0.00005)
        "e" -> setValue(-0.00001)
        "w" -> setValue(-0.000005)
        "q" -> setValue(-0.000001)
        "p" -> setValue(+0.000001)
        "o" -> setValue(+0.000005)
        "i" -> setValue(+0.00001)
        "u" -> setValue(+0.00005)
        "y" -> setValue(+0.0001)
        "h" -> setValue(+0.0005)
        "j" -> setValue(+0.001)
        "k" -> setValue(+0.01)
        "l" -> setValue(+0.1)
        ";" -> setValue(+1.0)
    }
}

private fun KeyEvent.mapZxcvKeyRow(setValue: (Double) -> Unit) {
    when (name) {
        "z" -> setValue(-0.01)
        "x" -> setValue(-0.001)
        "c" -> setValue(-0.0001)
        "v" -> setValue(-0.00005)
        "b" -> setValue(-0.00001)
        "n" -> setValue(+0.00001)
        "m" -> setValue(+0.00005)
        "," -> setValue(+0.0001)
        "." -> setValue(+0.001)
        "/" -> setValue(+0.01)
    }
}

context(Program)
private fun onNumberKeys(storeCheckpoint: (Int) -> Unit) {
    keyboard.keyDown.listen {
        if (it.name in "123456789") storeCheckpoint(it.name.toInt() - 1)
    }
}

private data class RoseSeed(
    val nValue: Double,
    val dValue: Double
)

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

    object Something1 : AnimationConfig(
        serial = 100,
        n1 = 142.821594,
        n2 = 142.826144,
        n3 = 142.828444,
        animationDuration = 20_000L,
        animationEasing = Easing.CubicInOut,
    )

    object Something2 : AnimationConfig(
        serial = 100,
        n1 = 201.132662,
        n2 = 142.826144,
        n3 = 142.828444,
        animationDuration = 20_000L,
        animationEasing = Easing.CubicInOut,
    )
}
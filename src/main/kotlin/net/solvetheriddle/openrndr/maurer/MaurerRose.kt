@file:Suppress("unused")

package net.solvetheriddle.openrndr.maurer

import net.solvetheriddle.openrndr.Display
import net.solvetheriddle.openrndr.sketchSize
import org.openrndr.KeyEvent
import org.openrndr.MouseButton
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
import kotlin.properties.Delegates

// sketch config
private val useDisplay = Display.LG_ULTRAWIDE // Display.MACBOOK_AIR
private const val showUi = true
private const val enableScreenshots = false // on SPACE, disables rose animations
private const val enableScreenRecording = false
private const val storeFile = "maurer_roses_store"

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
private const val closeShape = false
private const val revealDuration = 7.0 // seconds
private const val fadeOutDuration = 3.0 // seconds
private const val animationDuration = 5_000L // milliseconds

private val rose = MaurerRose()

/**
 * This program draws and animates Maurer Rose - https://en.wikipedia.org/wiki/Maurer_rose.
 *
 * Control the number of petals (N) and the angle factor (D) by sliders and keyboard:
 *  - Press A S D F G T R E W Q keys to decrease N
 *  - Press ; L K J H Y U I O P keys to increase N
 *  - Press Z X C V B keys to decrease D
 *  - Press N M , . / keys to increase D
 *
 *  Control visibility and reveal of the rose:
 *  - Press = to fade out the rose
 *  - Press ENTER to fade in the rose
 *  - Press RIGHT SHIFT to reveal the rose gradually
 *  - Press BACKSPACE to hide the rose gradually
 *
 *  Save and load seeds:
 *  - Press § to toggle edit mode for seeds
 *  - Press 1-9 to read or write seeds
 *
 *  Animate between pre-defined seeds:
 *  - Press LEFT ALT/CMD to animate to the previous seed
 *  - Press RIGHT ALT/CMD to animate to the next seed
 *  - Press SPACE to run animation across all seeds
 *
 *  Zoom in and out:
 *  - Press + to zoom in
 *  - Press - to zoom out
 *  - Scroll mouse wheel to zoom in and out
 *  - Press middle mouse button to reset zoom
 */
@Suppress("GrazieInspection")
fun main() {
    application {
        configure {
            sketchSize(useDisplay)
        }
        program {
            // RECORD
            setupScreenshotsIfEnabled()
            setupScreenRecordingIfEnabled()

            // DRAW
            draw(rose, backgroundImage)

            // ANIMATE
            enableVisibilityAnimations()
            enableRoseAnimations()

            // UI
            enableSeedView()
            addUiIfEnabled()
            enableKeyboardControls()
        }
    }
}

private fun Program.draw(rose: MaurerRose, imagePath: String?) {
    extend {
        drawer.clear(ColorRGBa.BLACK)
        drawBackgroundIfSet(imagePath)
        drawer.isolated {
            drawer.translate(drawer.bounds.center)
            drawer.translate(0.0, 6.0)
            rose.draw()
            rose.updateAnimation()
        }
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

    // number of petals
    var n: Double by Delegates.observable(initialN) { _, _, newValue ->
        if (::nSlider.isInitialized) nSlider.value = newValue
        if (::screenshots.isInitialized) screenshots.updateName(n = newValue)
    }

    // angle factor
    var d: Double by Delegates.observable(initialD) { _, _, newValue ->
        if (::dSlider.isInitialized) dSlider.value = newValue
        if (::screenshots.isInitialized) screenshots.updateName(d = newValue)
    }

    fun animate(targetNValue: Double, targetDValue: Double, duration: Long, predelay: Long = 0, easing: Easing = Easing.CubicInOut) {
        ::n.animate(targetNValue, duration, easing, predelay)
        ::d.animate(targetDValue, duration, easing, predelay)
    }

    context(Program)
    fun draw() {
        drawer.fill = null
        drawer.stroke = when (visibility) {
            Visibility.FADE_OUT -> ColorRGBa.WHITE.opacify(lineOpacity - (seconds - visibilityChangeTime) / fadeOutDuration)
            Visibility.FADE_IN -> ColorRGBa.WHITE.opacify(min(lineOpacity, (seconds - visibilityChangeTime) / fadeOutDuration))
            else -> ColorRGBa.WHITE.opacify(lineOpacity)
        }
        drawer.contour(shapeContour())
    }

    private fun Program.shapeContour(): ShapeContour {
        val c = contour {
            val radius = drawer.height / 2.0 * zoom
            val firstPoint = getPointForAngle(0, radius)
            moveTo(firstPoint)
            val numOfConnectedPoints = 360
            for (angle in 0..numOfConnectedPoints) {
                val nextPoint = getPointForAngle(angle, radius)
                if (curvesEnabled) continueTo(nextPoint) else lineTo(nextPoint)
            }
            closeShapeIfEnabled(firstPoint)
        }
        return when (reveal) {
            Reveal.GRADUAL_IN -> c.sampleEquidistant(10_000).sub(0.0, min((seconds - revealChangeTime) / revealDuration, 1.0))
            Reveal.GRADUAL_OUT -> c.sampleEquidistant(10_000).sub(1.0, max((seconds - revealChangeTime) / revealDuration, 0.0))
            else -> c
        }
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

private enum class Reveal {
    REVEALED, GRADUAL_IN, GRADUAL_OUT
}

private var visibility = Visibility.VISIBLE
private var reveal = Reveal.REVEALED
private var visibilityChangeTime = 0.0
private var revealChangeTime = 0.0

private fun Program.enableRoseAnimations() {
    executeOnKey("left-super") {
        val targetSeedIndex = max(lastSelectedSeed - 1, 0)
        animateToTarget(targetSeedIndex)
    }
    executeOnKey("right-super") {
        val targetSeedIndex = min(lastSelectedSeed + 1, seeds.lastIndex)
        animateToTarget(targetSeedIndex)
    }
    if (!enableScreenshots) executeOnKey("space") {
        val firstTarget = lastSelectedSeed + 1
        seeds.subList(firstTarget, seeds.size)
            .filter { it.isNotEmpty() }
            .forEachIndexed { index, _ ->
                animateToTarget(firstTarget + index, index * animationDuration)
            }
    }
}

private fun animateToTarget(targetSeedIndex: Int, predelay: Long = 0L) {
    val targetSeed = seeds[targetSeedIndex]
    rose.animate(targetSeed.nValue, targetSeed.dValue, animationDuration, predelay, easing = aConfig.animationEasing)
    lastSelectedSeed = targetSeedIndex
}

private fun Program.enableVisibilityAnimations() {
    executeOnKey("=") {
        visibility = Visibility.FADE_OUT
        visibilityChangeTime = seconds
    }
    executeOnKey("enter") {
        visibility = Visibility.FADE_IN
        reveal = Reveal.REVEALED
        visibilityChangeTime = seconds
    }
    executeOnKey("right-shift") {
        reveal = Reveal.GRADUAL_IN
        visibility = Visibility.VISIBLE
        revealChangeTime = seconds
    }
    executeOnKey("backspace") {
        reveal = Reveal.GRADUAL_OUT
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

private var curvesEnabled = false

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

private var lastSelectedSeed = 0
private val seedStore = File("data/$storeFile.txt").apply { createNewFile() }
private val seeds = loadSeeds()

private fun loadSeeds(): MutableList<RoseSeed> {
    val loadedSeeds = seedStore.readLines().map {
        val (nValue, dValue) = it.split(",")
        RoseSeed(nValue = nValue.toDouble(), dValue = dValue.toDouble())
    }.toMutableList()
    return if (loadedSeeds.size < 9) MutableList(9) { RoseSeed.Empty } else loadedSeeds
}

private var editMode = false
private lateinit var smallFont: FontImageMap
private lateinit var bigFont: FontImageMap

private fun Program.enableSeedView() {
    executeOnKey("§") { editMode = !editMode }
    onNumberKeys { slot -> if (editMode) writeSeed(slot) else readSeed(slot) }
    smallFont = loadFont("data/fonts/Rowdies-Light.ttf", 12.0)
    bigFont = loadFont("data/fonts/Rowdies-Bold.ttf", 40.0)
    if (showUi) extend {
        seeds.forEachIndexed { index, seed ->
            if (seed.isNotEmpty()) drawSeed(index, seed.nValue, seed.dValue)
        }
    }
}

fun readSeed(slot: Int) {
    lastSelectedSeed = slot
    seeds[slot].let {
        rose.n = it.nValue
        rose.d = it.dValue
    }
}

private fun writeSeed(slot: Int) {
    lastSelectedSeed = slot
    val seed = RoseSeed(nValue = rose.n, dValue = rose.d)
    seeds[slot] = seed
    seedStore.put(seeds)
}

private fun File.put(seeds: List<RoseSeed>) {
    writeText(seeds.joinToString("\n") { "${it.nValue},${it.dValue}" })
}

private fun Program.drawSeed(slot: Int, nValue: Double, dValue: Double) {
    with(drawer) {
        isolated {
            translate(10.0, 160.0 + slot * 40.0)
            fill = if (lastSelectedSeed == slot) ColorRGBa.DARK_GREY else ColorRGBa.GREY
            fontMap = bigFont
            text((slot + 1).toString(), 0.0, 0.0)
            if (editMode) {
                fontMap = smallFont
                text("N: $nValue", 28.0, -15.0)
                text("D: $dValue", 28.0, 0.0)
            }
        }
    }
}

private fun Program.enableKeyboardControls() {
    onKeyEvent { keyEvent -> keyEvent.mapAsdfKeyRow { rose.n += it } }
    onKeyEvent { keyEvent -> keyEvent.mapZxcvKeyRow { rose.d += it } }
    enableMouseControl()
}

private var zoom = 0.95

fun Program.enableMouseControl() {
    mouse.buttonUp.listen {
        if (it.button == MouseButton.CENTER) {
            zoom = 0.95
        }
    }
    val scrollSpeedDampening = 50
    mouse.scrolled.listen {
        zoom += it.rotation.y / scrollSpeedDampening
    }
    onKeyEvent {
        if (it.name == "+") zoom += 0.1
        if (it.name == "-") zoom -= 0.1
    }
}

private fun Program.onKeyEvent(setValue: (KeyEvent) -> Unit) {
    keyboard.keyRepeat.listen(setValue)
    keyboard.keyDown.listen(setValue)
}

private fun Program.setupScreenRecordingIfEnabled() {
    if (enableScreenRecording) {
        extend(ScreenRecorder()) {
            name = "maurer_rose_vid_${aConfig.serial}"
        }
    }
}

private lateinit var screenshots: RoseScreenshots

private fun Program.setupScreenshotsIfEnabled() {
    if (enableScreenshots) {
        screenshots = RoseScreenshots()
        extend(screenshots)
    }
}

private class RoseScreenshots : Screenshots() {

    private val customFolderName = "screenshots/maurer_roses"

    init {
        name = "$customFolderName/rose_${rose.d}-${rose.n}.png"
    }

    fun updateName(n: Double? = null, d: Double? = null) {
        val newN = n ?: rose.n
        val newD = d ?: rose.d
        name = "$customFolderName/rose_$newD-$newN.png"
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
    val dValue: Double,
) {
    fun isNotEmpty() = this != Empty

    companion object {
        val Empty = RoseSeed(0.0, 0.0)
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

    object Something1 : AnimationConfig(
        serial = 100,
        n1 = 142.821594,
        n2 = 142.826144,
        n3 = 142.828444,
        animationDuration = 20_000L,
        animationEasing = Easing.CubicInOut,
    )

    object Something2 : AnimationConfig(
        serial = 101,
        n1 = 201.132662,
        n2 = 142.826144,
        n3 = 142.828444,
        animationDuration = 20_000L,
        animationEasing = Easing.CubicInOut,
    )
}
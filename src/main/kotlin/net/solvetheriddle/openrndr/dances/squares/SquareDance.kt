package net.solvetheriddle.openrndr.dances.squares

import net.solvetheriddle.openrndr.Colors
import net.solvetheriddle.openrndr.Display
import net.solvetheriddle.openrndr.sketchSize
import net.solvetheriddle.openrndr.tools.Movie
import net.solvetheriddle.openrndr.tools.Scene
import org.openrndr.Program
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.RectangleBatchBuilder
import org.openrndr.extra.color.presets.BROWN
import org.openrndr.extra.color.presets.LIME_GREEN
import org.openrndr.extra.color.presets.MOCCASIN
import org.openrndr.extra.color.presets.ROYAL_BLUE
import org.openrndr.extra.videoprofiles.ProresProfile
import org.openrndr.ffmpeg.ScreenRecorder
import org.openrndr.math.Vector2
import org.openrndr.math.mix
import org.openrndr.shape.Rectangle

private const val RECORDING = false

fun main() = application {
    configure {
        sketchSize(Display.LG_SQUARE_LEFT)
    }
    program {
        if (RECORDING) {
            extend(ScreenRecorder().apply {
                profile = ProresProfile()
            })
        }

        val movie = Movie(!RECORDING).apply {
            append(SolidSpiralScene(900, drawer.bounds))
        }

        extend {
            drawer.clear(ColorRGBa.BLACK)

            movie.play {
                if (RECORDING) program.application.exit()
            }
        }
    }
}

private class SolidSpiralScene(
    length: Int,
    sketchBounds: Rectangle,
) : Scene(length) {

    val singleMoveLength = 60 * 7
    val squares: List<InOutSquareUnit> = generateSquares(sketchBounds, singleMoveLength)

    var pointer = 0

    override fun Program.sceneFunction(frameCount: Int) {

        if (frameCount % 2 == 0) squares[pointer++].sendOut(frameCount)
        if (pointer > squares.lastIndex) pointer = 0

        drawer.rectangles {
            squares.forEach {
                it.update(frameCount)
                it.draw()
            }
        }
    }

    override fun reset() {
        squares.forEach { it.reset() }
    }

    private fun generateSquares(sketchBounds: Rectangle, animationFrames: Int): List<InOutSquareUnit> {
        val color1 = ColorRGBa.LIME_GREEN.opacify(0.8)
        val color2 = ColorRGBa.ROYAL_BLUE.opacify(0.8)
        val color3 = ColorRGBa.MOCCASIN.opacify(0.8)
        val color4 = ColorRGBa.BROWN.opacify(0.8)
        val numOfPoints = 80
        val quarterSize = numOfPoints / 4.0

        return sketchBounds.scaledBy(0.5).contour.equidistantPositions(numOfPoints).take(numOfPoints) // 1 point gets added because contour is closed
            .mapIndexed { index, it ->
                val color = when (index) {
                    in 0 until quarterSize.toInt() -> color1.mix(color2, index % quarterSize / quarterSize)
                    in quarterSize.toInt() until 2 * quarterSize.toInt() -> color2.mix(color3, index % quarterSize / quarterSize)
                    in 2 * quarterSize.toInt() until 3 * quarterSize.toInt() -> color3.mix(color4, index % quarterSize / quarterSize)
                    else -> color4.mix(color1, index % quarterSize / quarterSize)
                }
                InOutSquareUnit(sketchBounds.center, it, animationFrames, color)
            }
    }
}

private class InOutSquareUnit(
    private val initPosition: Vector2,
    private val edgePosition: Vector2,
    private val animationFrames: Int,
    private val color: ColorRGBa = Colors.random,
) {

    val initSize = 300.0
    private val targetSize: Double = 10.0
    private var state = State.STATIC

    var position = initPosition
    var size = initSize
    var startFrameCount = 0
    var countDown = 2

    fun sendOut(frameCount: Int) {
        startFrameCount = frameCount
        if (countDown > 0) {
            state = State.GOING_OUT
            countDown--
        } else {
            sendIn()
        }
    }

    fun sendIn() {
        state = State.GOING_IN
    }

    fun update(sceneFrameCount: Int) {
        val frameCount = sceneFrameCount - startFrameCount
        when (state) {
            State.GOING_OUT -> updateMoving(frameCount) { progress ->

                val progress = (frameCount * 7) / animationFrames.toDouble()
                position = mix(initPosition, edgePosition, progress)
                size = mix(initSize, targetSize, progress)
            }

            State.GOING_IN -> updateMoving(frameCount) { progress ->

                val progress = (frameCount) / animationFrames.toDouble()
                position = mix(edgePosition, initPosition, progress)
                size = mix(targetSize, initSize, progress)
            }

            else -> { /* do nothing */
            }
        }
    }

    private fun updateMoving(frameCount: Int, updateFunction: (Double) -> Unit) {
        val progress = (frameCount * 7) / animationFrames.toDouble()
        updateFunction(progress)
    }

    context(RectangleBatchBuilder)
    fun draw() {
        fill = color
        stroke = null
        rectangle(Rectangle(position - size / 2.0, size, size))
    }

    fun reset() {
        countDown = 5
        state = State.STATIC
    }

    enum class State {
        STATIC,
        GOING_OUT,
        GOING_IN,
    }
}

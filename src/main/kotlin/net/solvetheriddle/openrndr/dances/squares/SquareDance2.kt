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
import org.openrndr.draw.isolated
import org.openrndr.extra.color.presets.*
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

        val initSize = 200.0
        val maxEdgeScale = 0.95
        val movie = Movie(!RECORDING).apply {
            addEdgingScene(drawer.bounds, maxEdgeScale, maxEdgeScale, initSize, 10.0, 240, 0)
            addEdgingScene(drawer.bounds, 0.9, maxEdgeScale, initSize, 15.0, 180, 60)
            addEdgingScene(drawer.bounds, 0.84, maxEdgeScale, initSize, 20.0, 132, 90)
            addEdgingScene(drawer.bounds, 0.764, maxEdgeScale, initSize, 30.0, 88, 60)
            addEdgingScene(drawer.bounds, 0.67, maxEdgeScale, initSize, 40.0, 56, 20)
            addEdgingScene(drawer.bounds, 0.545, maxEdgeScale, initSize, 60.0, 32, 10)
            addEdgingScene(drawer.bounds, 0.38, maxEdgeScale, initSize, 80.0, 16, 0)
        }

        extend {
            drawer.clear(ColorRGBa.BLACK)

            movie.play {
                if (RECORDING) program.application.exit()
            }
        }
    }
}

private fun Movie.addEdgingScene(
    sketchBounds: Rectangle,
    edgeScale: Double,
    maxEdgeScale: Double,
    initSize: Double,
    targetSize: Double,
    numOfRects: Int,
    startOffset: Int
) {
    val singleMoveLength = 30
    val length = 240 / numOfRects * numOfRects + singleMoveLength
    val afterglowLength = 180
    add(EdgingScene(length, singleMoveLength, afterglowLength, sketchBounds, numOfRects, edgeScale, maxEdgeScale, initSize, targetSize), startOffset)
}

private class EdgingScene(
    length: Int,
    singleMoveLength: Int,
    afterglowLength: Int,
    sketchBounds: Rectangle,
    numOfRects: Int,
    edgeScale: Double,
    private val maxEdgeScale: Double,
    private val initSize: Double,
    targetSize: Double,
) : Scene(length + afterglowLength) {

    private val color1 = ColorRGBa.LIME_GREEN
    private val color2 = ColorRGBa.ROYAL_BLUE
    private val color3 = ColorRGBa.BLUE_VIOLET
    private val color4 = ColorRGBa.BROWN
    private val colorCentral = ColorRGBa.YELLOW
    private val squares: List<SquareUnit> = generateSquares(sketchBounds, singleMoveLength, numOfRects, edgeScale, initSize, targetSize)

    private val interval = length / numOfRects
    private var pointer = 0

    override fun Program.sceneFunction(frameCount: Int) {
        if (pointer <= squares.lastIndex && frameCount % interval == 0) squares[pointer++].sendOut(frameCount)

        drawer.rectangles {
            squares.forEach {
                it.update(frameCount)
                it.draw()
            }
        }
        drawCentralSquare()
    }

    context(Program)
    private fun drawCentralSquare() {
        drawer.fill = colorCentral.opacify(0.5)
        drawer.stroke = null
        val centerRectSize = initSize
        drawer.isolated {
            drawer.translate(drawer.bounds.center)
            drawer.rotate(seconds * 50)
            drawer.rectangle(Rectangle(Vector2.ZERO - centerRectSize / 2.0, centerRectSize, centerRectSize))
        }
    }

    override fun reset() {
        squares.forEach { it.reset() }
    }

    private fun generateSquares(
        sketchBounds: Rectangle,
        animationFrames: Int,
        numOfPoints: Int,
        scale: Double,
        initSize: Double,
        targetSize: Double
    ): List<SquareUnit> {
        val quarterSize = numOfPoints / 4.0
        return sketchBounds.scaledBy(scale).contour.equidistantPositions(numOfPoints).take(numOfPoints) // 1 point gets added because contour is closed
            .mapIndexed { index, it ->
                val color = calculateColor(index, quarterSize, color1, color2, color3, color4, scale)
                SquareUnit(sketchBounds.center, it, animationFrames, color, initSize, targetSize)
            }
    }

    private fun calculateColor(
        index: Int,
        quarterSize: Double,
        color1: ColorRGBa,
        color2: ColorRGBa,
        color3: ColorRGBa,
        color4: ColorRGBa,
        scale: Double,
    ): ColorRGBa {
        val color = when (index) {
            in 0 until quarterSize.toInt() -> color1.mix(color2, index % quarterSize / quarterSize)
            in quarterSize.toInt() until 2 * quarterSize.toInt() -> color2.mix(color3, index % quarterSize / quarterSize)
            in 2 * quarterSize.toInt() until 3 * quarterSize.toInt() -> color3.mix(color4, index % quarterSize / quarterSize)
            else -> color4.mix(color1, index % quarterSize / quarterSize)
        }
        return color.mix(colorCentral, 1.0 - scale * (1.0 / maxEdgeScale))
    }
}

private class SquareUnit(
    private val initPosition: Vector2,
    private val edgePosition: Vector2,
    private val animationFrames: Int,
    private val color: ColorRGBa = Colors.random,
    private val initSize: Double,
    private val targetSize: Double,
) {

    private var state = State.STATIC
    private var position = initPosition
    private var size = initSize
    private var startFrameCount = 0

    fun sendOut(frameCount: Int) {
        startFrameCount = frameCount
        state = State.GOING_OUT
    }

    fun sendIn(frameCount: Int) {
        startFrameCount = frameCount
        state = State.GOING_IN
    }

    fun update(sceneFrameCount: Int) {
        val frameCount = sceneFrameCount - startFrameCount
        val progress = (frameCount) / animationFrames.toDouble()
        when (state) {
            State.GOING_OUT -> {
                position = mix(initPosition, edgePosition, progress)
                size = mix(initSize, targetSize, progress)
                if (progress >= 1.0) state = State.STATIC
            }

            State.GOING_IN -> {
                position = mix(edgePosition, initPosition, progress)
                size = mix(targetSize, initSize, progress)
                if (progress >= 1.0) state = State.STATIC
            }

            else -> { /* do nothing */
            }
        }
    }

    context(RectangleBatchBuilder)
    fun draw() {
        fill = color
        stroke = null
        rectangle(Rectangle(position - size / 2.0, size, size))
    }

    fun reset() {
        state = State.STATIC
//        position = initPosition
//        size = initSize
    }

    enum class State {
        STATIC,
        GOING_OUT,
        GOING_IN,
    }
}

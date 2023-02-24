package net.solvetheriddle.openrndr.dances.bars

import net.solvetheriddle.openrndr.Display
import net.solvetheriddle.openrndr.sketchSize
import net.solvetheriddle.openrndr.tools.Move
import net.solvetheriddle.openrndr.tools.Movie
import org.openrndr.Program
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extra.color.presets.*
import org.openrndr.extra.shapes.grid
import org.openrndr.extra.videoprofiles.ProresProfile
import org.openrndr.ffmpeg.ScreenRecorder
import org.openrndr.shape.Rectangle

// config recording
private const val RECORDING = false

fun main() = application {
    configure {
        sketchSize(Display.LG_SQUARE_LEFT)
//        sketchSize(Display.MACBOOK_AIR)
    }

    program {
        if (RECORDING) {
            extend(ScreenRecorder().apply {
                frameRate = 60
                profile = ProresProfile()
            })
        }

        val movie = generateMovie(drawer.bounds)

        extend {
//            drawer.translate(drawer.bounds.center)
            movie.play {
                if (RECORDING) program.application.exit()
            }
        }
    }
}

private fun generateMovie(sketchBounds: Rectangle): Movie {
    val numOfBars = 32
    val framesPerTickToFill = 2
    val framesPerTickToBreakDown = 4
    val tickAlpha = 2.0
    val attack = 0.5
    val decay = 0.06
    val decay2 = decay / 2.0
    val decay3 = decay / 3
    val decay4 = decay / 4
    val decay5 = decay / 8
    val decayLength = (tickAlpha / decay).toInt()

    return Movie(loop = !RECORDING).apply {
//        append(BarTickMove(sketchBounds, heightPercentage = 0.1, numOfBars, framesPerTickToFill, tickAlpha, attack, decay, direction = 1))
//        append(BarTickMove(sketchBounds, heightPercentage = 0.2, numOfBars, framesPerTickToFill, tickAlpha, attack, decay, direction = -1), -decayLength)
//        append(BarTickMove(sketchBounds, heightPercentage = 0.4, numOfBars, framesPerTickToFill, tickAlpha, attack, decay, direction = 1), -decayLength)
//        append(BarTickMove(sketchBounds, heightPercentage = 0.6, numOfBars, framesPerTickToFill, tickAlpha, attack, decay, direction = -1), -decayLength)
//        append(BarTickMove(sketchBounds, heightPercentage = 0.8, numOfBars, framesPerTickToFill, tickAlpha, attack, decay, direction = 1), -decayLength)
//        append(BarTickMove(sketchBounds, heightPercentage = 1.0, numOfBars, framesPerTickToFill, tickAlpha, attack, decay, direction = -1), -decayLength)
//        append(SplitBarTickMove(sketchBounds, numOfSplits = 2, numOfBars, framesPerTickToFill + 1, tickAlpha, attack / 2, decay, direction = 1), -decayLength)
//        append(SplitBarTickMove(sketchBounds, numOfSplits = 4, numOfBars, framesPerTickToFill + 1, tickAlpha, attack / 2, decay2, direction = -1), -(decayLength * 1.2).toInt())
//        append(SplitBarTickMove(sketchBounds, numOfSplits = 8, numOfBars, framesPerTickToFill + 2, tickAlpha, attack / 2, decay3, direction = 1), -calculateDecayLength(tickAlpha, decay2))
//        append(SplitBarTickMove(sketchBounds, numOfSplits = 16, numOfBars, framesPerTickToFill + 2, tickAlpha, attack / 2, decay4, direction = -1), -calculateDecayLength(tickAlpha, decay3))
//        append(SplitBarTickMove(sketchBounds, numOfSplits = 32, numOfBars, framesPerTickToFill + 4, tickAlpha, attack / 2, decay5, direction = 1), -calculateDecayLength(tickAlpha, decay4))
        add(FillMove(sketchBounds, numOfBars, tickAlpha))
        add(SplitBarTickMove(sketchBounds, numOfSplits = 32, numOfBars, framesPerTickToFill + 4, tickAlpha, attack / 2, decay5, direction = 1))
    }
}

internal class FillMove(
    sketchBounds: Rectangle,
    private val numOfBars: Int,
    private val tickAlpha: Double
) : Move(60 * 10) {

    private val rectangles = sketchBounds.grid(numOfBars, numOfBars, gutterX = 10.0, gutterY = 10.0).flatten()
    private val colorSegment = tickAlpha / numOfBars.toDouble()

    override fun Program.moveFunction(frameCount: Int) {
        drawer.stroke = null
//        drawer.rectangles(RectangleBatch( drawer.drawStyle.shadeStyle))
        drawer.rectangles {
            rectangles.forEachIndexed { itemIndex: Int, it ->
                val columnIndex = itemIndex % numOfBars
                val rowIndex = itemIndex / numOfBars
//                val colorDark = getColorForSegment(columnIndex * colorSegment)
//                val colorBright = getColorForSegment((columnIndex + 1) * colorSegment)
                val originalFillColor = ColorRGBa.DARK_GOLDEN_ROD.opacify(tickAlpha)
                val firstColor = ColorRGBa.DARK_MAGENTA
                val secondColor = ColorRGBa.MAGENTA
                val fillColor = if (rowIndex < numOfBars / 2) {
                    val secondary = firstColor.mix(secondColor, (columnIndex + 1.0) / numOfBars)
                    originalFillColor.mix(secondary, (rowIndex + 1.0) / (numOfBars / 2.0))
                } else {
                    val secondary = firstColor.mix(secondColor, (columnIndex + 1.0) / numOfBars)
                    originalFillColor.mix(secondary, (numOfBars - rowIndex + 1.0) / (numOfBars / 2.0))
                }
                fill = fillColor

//                drawer.shadeStyle = linearGradient(colorDark, colorBright, rotation = -90.0)
                rectangle(it)
//                rectangle(it.scaledBy(2.0, 0.5, 0.5))
            }
        }
//        val geometry = null
//        val drawStyle = drawer.drawStyle.copy(shadeStyle = )
//        drawer.rectangles(RectangleBatch(geometry, drawStyle))
    }

    private fun getColorForSegment(factor: Double) = ColorRGBa.DARK_GOLDEN_ROD.opacify(factor)
}

private fun calculateDecayLength(tickAlpha: Double, decay: Double) = ((tickAlpha / decay) * 1.1).toInt()


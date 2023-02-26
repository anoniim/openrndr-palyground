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
//        sketchSize(Display.LG_PORTRAIT_LEFT)
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
        append(BarTickMove(sketchBounds, heightPercentage = 0.1, numOfBars, framesPerTickToFill, tickAlpha, attack, decay, direction = 1))
        append(BarTickMove(sketchBounds, heightPercentage = 0.2, numOfBars, framesPerTickToFill, tickAlpha, attack, decay, direction = -1), -decayLength)
        append(BarTickMove(sketchBounds, heightPercentage = 0.4, numOfBars, framesPerTickToFill, tickAlpha, attack, decay, direction = 1), -decayLength)
        append(BarTickMove(sketchBounds, heightPercentage = 0.6, numOfBars, framesPerTickToFill, tickAlpha, attack, decay, direction = -1), -decayLength)
        append(BarTickMove(sketchBounds, heightPercentage = 0.8, numOfBars, framesPerTickToFill, tickAlpha, attack, decay, direction = 1), -decayLength)
        append(BarTickMove(sketchBounds, heightPercentage = 1.0, numOfBars, framesPerTickToFill, tickAlpha, attack, decay, direction = -1), -decayLength)
        append(SplitBarTickMove(sketchBounds, numOfSplits = 2, numOfBars, framesPerTickToFill + 1, tickAlpha, attack / 2, decay, direction = 1), -decayLength)
        append(SplitBarTickMove(sketchBounds, numOfSplits = 4, numOfBars, framesPerTickToFill + 1, tickAlpha, attack / 2, decay2, direction = -1), -(decayLength * 1.2).toInt())
        append(SplitBarTickMove(sketchBounds, numOfSplits = 8, numOfBars, framesPerTickToFill + 2, tickAlpha, attack / 2, decay3, direction = 1), -calculateDecayLength(tickAlpha, decay2))
        append(SplitBarTickMove(sketchBounds, numOfSplits = 16, numOfBars, framesPerTickToFill + 2, tickAlpha, attack / 2, decay4, direction = -1), -calculateDecayLength(tickAlpha, decay3))

        val firstRectangleMove = RectangleTickMove(sketchBounds, numOfBars, framesPerTickToFill + 3, tickAlpha, attack / 2, decay5, direction = 1)
        append(firstRectangleMove, -calculateDecayLength(tickAlpha, decay4))
        append(SplitBarTickMove(sketchBounds, numOfSplits = 32, numOfBars, framesPerTickToFill + 3, tickAlpha, attack / 2, decay5, direction = 1), -firstRectangleMove.lengthFrames)

//        add(PulseMove(sketchBounds, numOfBars, tickAlpha))
    }

}

private fun calculateDecayLength(tickAlpha: Double, decay: Double) = ((tickAlpha / decay) * 1.1).toInt()

package net.solvetheriddle.openrndr.dances.bars

import net.solvetheriddle.openrndr.Display
import net.solvetheriddle.openrndr.sketchSize
import net.solvetheriddle.openrndr.tools.Move
import net.solvetheriddle.openrndr.tools.Movie
import org.openrndr.Program
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extra.color.presets.DARK_GOLDEN_ROD
import org.openrndr.extra.shapes.grid
import org.openrndr.math.Vector2
import org.openrndr.shape.LineSegment
import org.openrndr.shape.Rectangle
import kotlin.math.ceil

fun main() = application {
    configure {
        sketchSize(Display.LG_SQUARE_LEFT)
//        sketchSize(Display.MACBOOK_AIR)
    }
    program {
        val movie = generateMovie(drawer.bounds)

        extend {
//            drawer.translate(drawer.bounds.center)
            movie.play()
        }
    }
}

private fun generateMovie(sketchBounds: Rectangle): Movie {
    val numOfBars = 20
    val framesPerTickToFill = 2
    val framesPerTickToBreakDown = 4
    val tickAlpha = 2.0
    val attack = 0.5
    val decay = 0.06
    val slowDecay = decay / 5
    val decayLength = (tickAlpha / decay).toInt()

    return Movie(loop = true).apply {
        append(BarTickMove(sketchBounds, heightPercentage = 0.1, numOfBars, framesPerTickToFill, tickAlpha, attack, decay, direction = 1))
        append(BarTickMove(sketchBounds, heightPercentage = 0.2, numOfBars, framesPerTickToFill, tickAlpha, attack, decay, direction = -1), -decayLength)
        append(BarTickMove(sketchBounds, heightPercentage = 0.4, numOfBars, framesPerTickToFill, tickAlpha, attack, decay, direction = 1), -decayLength)
        append(BarTickMove(sketchBounds, heightPercentage = 0.6, numOfBars, framesPerTickToFill, tickAlpha, attack, decay, direction = -1), -decayLength)
        append(BarTickMove(sketchBounds, heightPercentage = 0.8, numOfBars, framesPerTickToFill, tickAlpha, attack, decay, direction = 1), -decayLength)
        append(BarTickMove(sketchBounds, heightPercentage = 1.0, numOfBars, framesPerTickToFill, tickAlpha, attack, decay, direction = -1), -decayLength)
        append(SplitBarTickMove(sketchBounds, numOfSplits = 2, numOfBars, framesPerTickToBreakDown, tickAlpha, attack, decay, direction = 1), -decayLength)
        append(SplitBarTickMove(sketchBounds, numOfSplits = 6, numOfBars, framesPerTickToBreakDown, tickAlpha, attack, decay, direction = -1), -decayLength)
        append(SplitBarTickMove(sketchBounds, numOfSplits = 8, numOfBars, framesPerTickToBreakDown, tickAlpha, attack, decay, direction = 1), -decayLength)
        append(SplitBarTickMove(sketchBounds, numOfSplits = 16, numOfBars, framesPerTickToBreakDown, tickAlpha, attack, decay, direction = -1), -decayLength)
        append(SplitBarTickMove(sketchBounds, numOfSplits = 32, numOfBars, framesPerTickToBreakDown, tickAlpha, attack, decay, direction = 1), -decayLength)
        append(
            SplitBarTickMove(sketchBounds, numOfSplits = 62, numOfBars, framesPerTickToBreakDown, tickAlpha, attack, slowDecay, direction = -1),
            -decayLength
        )
    }
}


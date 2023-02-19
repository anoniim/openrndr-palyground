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
        val numOfBars = 20
        val framesPerTickToFill = 2
        val framesPerTickToBreakDown = 4
        val tickAlpha = 2.0
        val attack = 0.5
        val decay = 0.06
        val slowDecay = decay / 5

        val decayLength = (tickAlpha / decay).toInt()
        val movie = Movie(loop = true).apply {
            append(
                BarTickMove(
                    generateBars(drawer.bounds, heightPercentage = 0.1, numOfBars, tickAlpha, attack, decay),
                    framesPerTickToFill,
                    tickAlpha, attack, decay,
                    direction = 1
                )
            )
            append(
                BarTickMove(
                    generateBars(drawer.bounds, heightPercentage = 0.2, numOfBars, tickAlpha, attack, decay),
                    framesPerTickToFill,
                    tickAlpha, attack, decay,
                    direction = -1
                ), -decayLength
            )
            append(
                BarTickMove(
                    generateBars(drawer.bounds, heightPercentage = 0.4, numOfBars, tickAlpha, attack, decay),
                    framesPerTickToFill,
                    tickAlpha, attack, decay,
                    direction = 1
                ), -decayLength
            )
            append(
                BarTickMove(
                    generateBars(drawer.bounds, heightPercentage = 0.6, numOfBars, tickAlpha, attack, decay),
                    framesPerTickToFill,
                    tickAlpha, attack, decay,
                    direction = -1
                ), -decayLength
            )
            append(
                BarTickMove(
                    generateBars(drawer.bounds, heightPercentage = 0.8, numOfBars, tickAlpha, attack, decay),
                    framesPerTickToFill,
                    tickAlpha, attack, decay,
                    direction = 1
                ), -decayLength
            )
            append(
                BarTickMove(
                    generateBars(drawer.bounds, heightPercentage = 1.0, numOfBars, tickAlpha, attack, decay),
                    framesPerTickToFill,
                    tickAlpha, attack, decay,
                    direction = -1
                ), -decayLength
            )
            append(
                SplitBarTickMove(
                    generateDoubleBars(drawer.bounds, numOfSplits = 2, numOfBars, tickAlpha, attack, decay),
                    framesPerTickToBreakDown,
                    tickAlpha, attack, decay,
                    direction = 1
                ), -decayLength
            )
            append(
                SplitBarTickMove(
                    generateDoubleBars(drawer.bounds, numOfSplits = 6, numOfBars, tickAlpha, attack, decay),
                    framesPerTickToBreakDown,
                    tickAlpha, attack, decay,
                    direction = -1
                ), -decayLength
            )
            append(
                SplitBarTickMove(
                    generateDoubleBars(drawer.bounds, numOfSplits = 8, numOfBars, tickAlpha, attack, decay),
                    framesPerTickToBreakDown,
                    tickAlpha, attack, decay,
                    direction = 1
                ), -decayLength
            )
            append(
                SplitBarTickMove(
                    generateDoubleBars(drawer.bounds, numOfSplits = 16, numOfBars, tickAlpha, attack, decay),
                    framesPerTickToBreakDown,
                    tickAlpha, attack, decay,
                    direction = -1
                ), -decayLength
            )
            append(
                SplitBarTickMove(
                    generateDoubleBars(drawer.bounds, numOfSplits = 32, numOfBars, tickAlpha, attack, slowDecay),
                    framesPerTickToBreakDown,
                    tickAlpha, attack, slowDecay,
                    direction = 1
                ), -decayLength
            )
//            append(
//                DoubleBarTickMove(
//                    generateDoubleBars(drawer.bounds, numOfSplits = 62, numOfBars, tickAlpha, attack, slowDecay),
//                    framesPerTickToBreakDown,
//                    tickAlpha, attack, slowDecay,
//                    direction = -1
//                ), -decayLength
//            )
        }

        extend {
//            drawer.translate(drawer.bounds.center)
            movie.play()
        }
    }
}

private fun generateBars(
    sketchBounds: Rectangle,
    heightPercentage: Double,
    numOfBars: Int,
    tickAlpha: Double,
    attack: Double,
    decay: Double
) = List(numOfBars) { index ->
    val x = sketchBounds.grid(numOfBars, 1).flatten().map { cell -> cell.center.x }[index]
    val height = sketchBounds.height * heightPercentage
    val yCenter = sketchBounds.height / 2.0
    val lineStart = Vector2(x, yCenter - height / 2.0)
    val lineEnd = Vector2(x, yCenter + height / 2.0)
    Bar(lineStart, lineEnd, tickAlpha = tickAlpha, attack = attack, decay = decay)
}

private fun generateDoubleBars(
    sketchBounds: Rectangle,
    numOfSplits: Int,
    numOfBars: Int,
    tickAlpha: Double,
    attack: Double,
    decay: Double
) = List(numOfBars) { index ->
    val grid = sketchBounds.grid(numOfBars, numOfSplits, gutterY = 10.0)
    List(numOfSplits) { splitIndex ->
        val barRect = grid.flatten()[index + splitIndex * (numOfBars - 1) + splitIndex]
        val lineStart = barRect.center - Vector2(0.0, barRect.height / 2.0)
        val lineEnd = barRect.center + Vector2(0.0, barRect.height / 2.0)
        Bar(lineStart, lineEnd, tickAlpha = tickAlpha, attack = attack, decay = decay)
    }
}

private abstract class TickMove<T>(
    val bars: List<T>,
    val framesPerTick: Int,
    tickAlpha: Double, attack: Double, decay: Double,
    val direction: Int = 1,
) : Move(calculateTickMoveLength(bars.size, framesPerTick, tickAlpha, attack, decay)) {

    val initTickPointer = if (direction == 1) 0 else bars.lastIndex
    var tickPointer = initTickPointer

    override fun Program.moveFunction(frameCount: Int) {
        if (frameCount % framesPerTick == 0) {
            if (tickPointer in bars.indices) {
                tick(bars[tickPointer])
                tickPointer += direction
            }
        }
        updateAndDraw(frameCount, bars)
    }

    abstract fun tick(items: T)

    context(Program)
    abstract fun updateAndDraw(frameCount: Int, items: List<T>)

    override fun reset() {
        tickPointer = initTickPointer
    }
}

private fun calculateTickMoveLength(numOfTicks: Int, framesPerTick: Int, tickAlpha: Double, attack: Double, decay: Double): Int {
    val appearFrames = framesPerTick * numOfTicks
    val attackFrames = ceil(tickAlpha / attack).toInt()
    val decayFrames = ceil(tickAlpha / decay).toInt()
    return (appearFrames + attackFrames + decayFrames)
}

private class BarTickMove(
    bars: List<Bar>,
    framesPerTick: Int,
    tickAlpha: Double, attack: Double, decay: Double,
    direction: Int = 1,
) : TickMove<Bar>(bars, framesPerTick, tickAlpha, attack, decay, direction) {

    override fun tick(item: Bar) {
        item.tick()
    }

    context(Program)
    override fun updateAndDraw(frameCount: Int, items: List<Bar>) {
        items.forEach {
            it.update(frameCount)
            it.draw()
        }
    }
}

private class SplitBarTickMove(
    bars: List<List<Bar>>,
    framesPerTick: Int,
    tickAlpha: Double, attack: Double, decay: Double,
    direction: Int = 1,
) : TickMove<List<Bar>>(bars, framesPerTick, tickAlpha, attack, decay, direction) {

    override fun tick(item: List<Bar>) {
        item.forEach {
            it.tick()
        }
    }

    context(Program)
    override fun updateAndDraw(frameCount: Int, items: List<List<Bar>>) {
        items.flatten().forEach {
            it.update(frameCount)
//            it.draw()
        }
        items.forEach {
            drawer.strokeWeight = it.first().strokeWeight
            drawer.stroke = ColorRGBa.DARK_GOLDEN_ROD.copy(alpha = it.first().alpha)
            val lineSegments = it.map { it.lineSegment }
            drawer.lineSegments(lineSegments)

        }
    }
}

private class Bar(
    start: Vector2,
    end: Vector2,
    private val tickAlpha: Double = 1.0,
    private val attack: Double = 0.5,
    private val decay: Double = 0.01,
    val strokeWeight: Double = 40.0,
) {

    val lineSegment = LineSegment(start, end)
    var alpha = 0.0
    private var state = State.DEFAULT

    fun tick() {
        state = State.ATTACK
    }

    fun update(frameCount: Int) {
        if (state == State.ATTACK) {
            alpha += attack
            if (alpha >= tickAlpha) {
                alpha = tickAlpha
                state = State.DECAY
                return
            }
        }
        if (state == State.DECAY && alpha > 0) alpha -= decay
    }

    context(Program)
    fun draw() {
        drawer.strokeWeight = strokeWeight
        drawer.stroke = ColorRGBa.DARK_GOLDEN_ROD.copy(alpha = alpha)
        drawer.lineSegment(lineSegment)
    }

    enum class State {
        DEFAULT,
        ATTACK,
        DECAY,
    }
}

package net.solvetheriddle.openrndr.dances.bars

import net.solvetheriddle.openrndr.tools.Move
import org.openrndr.Program
import org.openrndr.color.ColorRGBa
import org.openrndr.extra.color.presets.DARK_GOLDEN_ROD
import org.openrndr.extra.color.presets.DARK_MAGENTA
import org.openrndr.extra.shapes.grid
import org.openrndr.math.Vector2
import org.openrndr.shape.LineSegment
import org.openrndr.shape.Rectangle
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.pow

internal abstract class TickMove<T>(
    private val bars: List<T>,
    private val framesPerTick: Int,
    tickAlpha: Double, attack: Double, decay: Double,
    private val direction: Int = 1,
) : Move(calculateTickMoveLength(bars.size, framesPerTick, tickAlpha, attack, decay)) {

    private val initTickPointer = if (direction == 1) 0 else bars.lastIndex
    private var tickPointer = initTickPointer

    override fun Program.moveFunction(frameCount: Int) {
        if (frameCount % framesPerTick == 0) {
            if (tickPointer in bars.indices) {
                tick(bars[tickPointer])
                tickPointer += direction
            }
        }
        updateAndDraw(frameCount, bars)
    }

    abstract fun tick(item: T)

    context(Program)
    abstract fun updateAndDraw(frameCount: Int, allItems: List<T>)

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

internal class BarTickMove(
    sketchBounds: Rectangle,
    heightPercentage: Double,
    numOfBars: Int,
    framesPerTick: Int,
    tickAlpha: Double, attack: Double, decay: Double,
    direction: Int = 1,
) : TickMove<Bar>(generateBars(sketchBounds, heightPercentage, numOfBars, tickAlpha, attack, decay), framesPerTick, tickAlpha, attack, decay, direction) {

    override fun tick(item: Bar) {
        item.tick()
    }

    context(Program)
    override fun updateAndDraw(frameCount: Int, allItems: List<Bar>) {
        allItems.forEach {
            it.update(frameCount)
            it.draw()
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
    Bar(lineStart, lineEnd, tickAlpha = tickAlpha, attack = attack, decay = decay, color = ColorRGBa.DARK_GOLDEN_ROD)
}

internal class SplitBarTickMove(
    sketchBounds: Rectangle,
    numOfSplits: Int,
    numOfBars: Int,
    framesPerTick: Int,
    tickAlpha: Double, attack: Double, decay: Double,
    direction: Int = 1,
) : TickMove<List<Bar>>(generateSplitBars(sketchBounds, numOfSplits, numOfBars, tickAlpha, attack, decay), framesPerTick, tickAlpha, attack, decay, direction) {

    override fun tick(item: List<Bar>) {
        item.forEach {
            it.tick()
        }
    }

    context(Program)
    override fun updateAndDraw(frameCount: Int, allItems: List<List<Bar>>) {
        allItems.forEach { column ->
//            drawer.strokeWeight = column.first().strokeWeight
//            drawer.stroke = ColorRGBa.DARK_GOLDEN_ROD.copy(alpha = column.first().alpha)
            drawer.rectangles {
                column.forEach {
                    it.update(frameCount)
                    val strokeWeight = it.strokeWeight
                    val start = it.lineSegment.start
                    fill = it.color.copy(alpha = it.alpha)
                    stroke = null
                    rectangle(start - Vector2(strokeWeight / 2.0, 0.0), it.strokeWeight, it.lineSegment.length)
                }
            }

        }
    }
}

private fun generateSplitBars(
    sketchBounds: Rectangle,
    numOfSplits: Int,
    numOfBars: Int,
    tickAlpha: Double,
    attack: Double,
    decay: Double
) = List(numOfBars) { index ->
    val grid = sketchBounds.grid(numOfBars, numOfSplits, gutterY = 10.0, gutterX = 10.0)
    List(numOfSplits) { splitIndex ->
        val barRect = grid.flatten()[index + splitIndex * (numOfBars - 1) + splitIndex]
        val lineStart = barRect.center - Vector2(0.0, barRect.height / 2.0)
        val lineEnd = barRect.center + Vector2(0.0, barRect.height / 2.0)
        Bar(
            lineStart,
            lineEnd,
            tickAlpha = tickAlpha,
            attack = attack,
            decay = decay,
            color = getColorShade(ColorRGBa.DARK_GOLDEN_ROD, ColorRGBa.DARK_MAGENTA, numOfSplits, splitIndex)
        )
    }
}

private fun getColorShade(color1: ColorRGBa, color2: ColorRGBa, numOfSplits: Int, splitIndex: Int): ColorRGBa {
    return if (numOfSplits > 2) {
        val middle = (numOfSplits - 1) / 2.0
        val factor = (abs(splitIndex - middle) / middle).pow(2)
        val other = color2.mix(color1, 3.0 / numOfSplits)
        color1.mix(other, factor)
    } else color1
}

internal class Bar(
    start: Vector2,
    end: Vector2,
    private val tickAlpha: Double = 1.0,
    private val attack: Double = 0.5,
    private val decay: Double = 0.01,
    val strokeWeight: Double = 25.0,
    val color: ColorRGBa,
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
        drawer.stroke = color.copy(alpha = alpha)
        drawer.lineSegment(lineSegment)
    }

    enum class State {
        DEFAULT,
        ATTACK,
        DECAY,
    }
}
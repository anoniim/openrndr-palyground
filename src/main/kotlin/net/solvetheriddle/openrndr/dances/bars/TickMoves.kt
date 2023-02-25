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
    private val tickUnit: List<T>,
    private val framesPerTick: Int,
    protected val tickAlpha: Double, attack: Double, decay: Double,
    private val direction: Int = 1,
) : Move(calculateTickMoveLength(tickUnit.size, framesPerTick, tickAlpha, attack, decay)) {

    private val initTickPointer = if (direction == 1) 0 else tickUnit.lastIndex
    private var tickPointer = initTickPointer

    override fun Program.moveFunction(frameCount: Int) {
        if (frameCount % framesPerTick == 0) {
            if (tickPointer in tickUnit.indices) {
                tick(tickUnit[tickPointer])
                tickPointer += direction
            }
        }
        updateAndDraw(frameCount, tickUnit)
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
) : TickMove<LineSegmentTickUnit>(
    generateBars(sketchBounds, heightPercentage, numOfBars, tickAlpha, attack, decay),
    framesPerTick,
    tickAlpha,
    attack,
    decay,
    direction
) {

    override fun tick(item: LineSegmentTickUnit) {
        item.tick()
    }

    context(Program)
    override fun updateAndDraw(frameCount: Int, allItems: List<LineSegmentTickUnit>) {
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
    LineSegmentTickUnit(lineStart, lineEnd, tickAlpha = tickAlpha, attack = attack, decay = decay, color = ColorRGBa.DARK_GOLDEN_ROD)
}

internal class SplitBarTickMove(
    sketchBounds: Rectangle,
    numOfSplits: Int,
    numOfBars: Int,
    framesPerTick: Int,
    tickAlpha: Double, attack: Double, decay: Double,
    direction: Int = 1,
) : TickMove<List<LineSegmentTickUnit>>(
    generateSplitBars(sketchBounds, numOfSplits, numOfBars, tickAlpha, attack, decay),
    framesPerTick,
    tickAlpha,
    attack,
    decay,
    direction
) {

    override fun tick(item: List<LineSegmentTickUnit>) {
        item.forEach { it.tick() }
    }

    context(Program)
    override fun updateAndDraw(frameCount: Int, allItems: List<List<LineSegmentTickUnit>>) {
        allItems.forEach { column ->
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
        LineSegmentTickUnit(
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

internal class LineSegmentTickUnit(
    start: Vector2,
    end: Vector2,
    tickAlpha: Double = 1.0,
    attack: Double = 0.5,
    decay: Double = 0.01,
    val strokeWeight: Double = 25.0,
    val color: ColorRGBa,
) : TickUnit(tickAlpha, attack, decay) {

    val lineSegment = LineSegment(start, end)

    context(Program)
    fun draw() {
        drawer.stroke = color.copy(alpha = alpha)
        drawer.strokeWeight = strokeWeight
        drawer.lineSegment(lineSegment)
    }
}

internal class RectangleTickMove(
    sketchBounds: Rectangle,
    private val gridDimension: Int,
    framesPerTick: Int,
    tickAlpha: Double, attack: Double, decay: Double,
    direction: Int = 1,
) : TickMove<List<RectangleTickUnit>>(
    generateRectangles(sketchBounds, gridDimension, tickAlpha, attack, decay),
    framesPerTick,
    tickAlpha,
    attack,
    decay,
    direction
) {

    override fun tick(item: List<RectangleTickUnit>) {
        item.forEach { it.tick() }
    }

    context(Program)
    override fun updateAndDraw(frameCount: Int, allItems: List<List<RectangleTickUnit>>) {
        drawer.stroke = null
        drawer.rectangles {
            allItems.flatten().forEachIndexed { itemIndex: Int, it ->
                it.update(frameCount)
                fill = getFillColor(itemIndex, it.alpha)
                rectangle(it.rectangle)
//                rectangle(it.scaledBy(2.0, 0.5, 0.5))
            }
            if (frameCount == lastFrame) {
                allItems.flatten().forEach { it.reset() }
            }
        }
    }

    private fun getFillColor(itemIndex: Int, alpha: Double): ColorRGBa {
        val columnIndex = itemIndex % gridDimension
        val rowIndex = itemIndex / gridDimension
        val originalFillColor = ColorRGBa.DARK_GOLDEN_ROD.opacify(tickAlpha)
        val firstColor = ColorRGBa.DARK_MAGENTA
        val secondColor = ColorRGBa.MAGENTA
        return if (rowIndex < gridDimension / 2) {
            val secondary = firstColor.mix(secondColor, (columnIndex + 1.0) / gridDimension)
            originalFillColor.mix(secondary, (rowIndex + 1.0) / (gridDimension / 2.0))
        } else {
            val secondary = firstColor.mix(secondColor, (columnIndex + 1.0) / gridDimension)
            originalFillColor.mix(secondary, (gridDimension - rowIndex + 1.0) / (gridDimension / 2.0))
        }.copy(alpha = alpha)
    }
}

internal fun generateRectangles(sketchBounds: Rectangle, gridDimension: Int, tickAlpha: Double, attack: Double, decay: Double): List<List<RectangleTickUnit>> {
    return sketchBounds.grid(gridDimension, gridDimension, gutterX = 10.0, gutterY = 10.0)
        .transpose2()
        .map {
            it.map { rectangle ->
                RectangleTickUnit(rectangle, tickAlpha, attack, 0.0)
            }
        }
}

internal class RectangleTickUnit(
    val rectangle: Rectangle,
    tickAlpha: Double = 1.0,
    attack: Double = 0.5,
    decay: Double = 0.01,
) : TickUnit(tickAlpha, attack, decay) {

    fun reset() {
        alpha = 0.0
        state = State.DEFAULT
    }
}

internal abstract class TickUnit(
    private val tickAlpha: Double = 1.0,
    private val attack: Double = 0.5,
    private val decay: Double = 0.01,
) {

    var alpha = 0.0
    protected var state = State.DEFAULT

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

    enum class State {
        DEFAULT,
        ATTACK,
        DECAY,
    }
}

private fun <E> List<List<E>>.transpose2(): List<List<E>> {
    val maxRowSize = maxOf { it.size }
    val rowIndices = 0 until maxRowSize
    return indices.map { columnIndex ->
        rowIndices.map { rowIndex ->
            // instead of getting input[column][row], get input[row][column]
            val element = get(rowIndex)[columnIndex]
            element
        }
    }
}
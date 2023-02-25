package net.solvetheriddle.openrndr.dances.bars

import org.openrndr.Program
import org.openrndr.color.ColorRGBa
import org.openrndr.extra.color.presets.DARK_GOLDEN_ROD
import org.openrndr.extra.color.presets.DARK_MAGENTA
import org.openrndr.extra.shapes.grid
import org.openrndr.math.Vector2
import org.openrndr.shape.LineSegment
import org.openrndr.shape.Rectangle
import kotlin.math.abs
import kotlin.math.pow

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

object TickUnitFactory {

    internal fun generateBars(
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

    internal fun generateSplitBars(
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

    internal fun generateRectangles(
        sketchBounds: Rectangle,
        gridDimension: Int,
        tickAlpha: Double,
        attack: Double,
        decay: Double
    ): List<List<RectangleTickUnit>> {
        return sketchBounds.grid(gridDimension, gridDimension, gutterX = 10.0, gutterY = 10.0)
            .transpose2()
            .map {
                it.map { rectangle ->
                    RectangleTickUnit(rectangle, tickAlpha, attack, 0.0)
                }
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
}
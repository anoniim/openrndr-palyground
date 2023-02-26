package net.solvetheriddle.openrndr.dances.bars

import net.solvetheriddle.openrndr.tools.Scene
import org.openrndr.Program
import org.openrndr.color.ColorRGBa
import org.openrndr.extra.color.presets.DARK_GOLDEN_ROD
import org.openrndr.extra.color.presets.INDIGO
import org.openrndr.math.Vector2
import org.openrndr.shape.Rectangle
import kotlin.math.ceil

internal abstract class TickScene<T>(
    private val tickUnit: List<T>,
    private val framesPerTick: Int,
    tickAlpha: Double, attack: Double, decay: Double,
    private val direction: Int = 1,
) : Scene(calculateTickSceneLength(tickUnit.size, framesPerTick, tickAlpha, attack, decay)) {

    private val initTickPointer = if (direction == 1) 0 else tickUnit.lastIndex
    private var tickPointer = initTickPointer

    override fun Program.sceneFunction(frameCount: Int) {
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

private fun calculateTickSceneLength(numOfTicks: Int, framesPerTick: Int, tickAlpha: Double, attack: Double, decay: Double): Int {
    val appearFrames = framesPerTick * numOfTicks
    val attackFrames = ceil(tickAlpha / attack).toInt()
    val decayFrames = ceil(tickAlpha / decay).toInt()
    return (appearFrames + attackFrames + decayFrames)
}

internal class BarTickScene(
    sketchBounds: Rectangle,
    heightPercentage: Double,
    numOfBars: Int,
    framesPerTick: Int,
    tickAlpha: Double, attack: Double, decay: Double,
    direction: Int = 1,
) : TickScene<LineSegmentTickUnit>(
    TickUnitFactory.generateBars(sketchBounds, heightPercentage, numOfBars, tickAlpha, attack, decay),
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

internal class SplitBarTickScene(
    sketchBounds: Rectangle,
    numOfSplits: Int,
    numOfBars: Int,
    framesPerTick: Int,
    tickAlpha: Double, attack: Double, decay: Double,
    direction: Int = 1,
) : TickScene<List<LineSegmentTickUnit>>(
    TickUnitFactory.generateSplitBars(sketchBounds, numOfSplits, numOfBars, tickAlpha, attack, decay),
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

internal class RectangleTickScene(
    sketchBounds: Rectangle,
    private val gridDimension: Int,
    framesPerTick: Int,
    tickAlpha: Double, attack: Double, decay: Double,
    direction: Int = 1,
) : TickScene<List<RectangleTickUnit>>(
    TickUnitFactory.generateRectangles(sketchBounds, gridDimension, tickAlpha, attack),
    framesPerTick,
    tickAlpha,
    attack,
    decay,
    direction
) {

    private val originalFillColor = ColorRGBa.INDIGO
    private val centerEdgeColor = ColorRGBa.MAGENTA
    private val centerColor =  ColorRGBa.DARK_GOLDEN_ROD.opacify(tickAlpha)

    override fun tick(item: List<RectangleTickUnit>) {
        item.forEach { it.tick() }
    }

    context(Program)
    override fun updateAndDraw(frameCount: Int, allItems: List<List<RectangleTickUnit>>) {
        drawer.stroke = null
        drawer.rectangles {
            allItems.flatten().forEach {
                it.update(frameCount)
                fill = getFillColor(it.rectangle, drawer.bounds, it.alpha)
                rectangle(it.rectangle)
//                rectangle(it.scaledBy(2.0, 0.5, 0.5))
            }
            if (frameCount == lastFrame) {
                allItems.flatten().forEach { it.reset() }
            }
        }
    }

    private fun getFillColor(position: Rectangle, bounds: Rectangle, alpha: Double): ColorRGBa {
        val distFromCenter = position.center.distanceTo(bounds.center)
        val radialBoundary = bounds.width / 3.0
        return if (distFromCenter < radialBoundary) {
            centerColor.mix(centerEdgeColor, distFromCenter / radialBoundary)
        } else {
            val factor = (distFromCenter - radialBoundary) / (bounds.width / 2.0 - radialBoundary)
            centerEdgeColor.mix(originalFillColor, factor)
        }.copy(alpha = alpha)
    }
}

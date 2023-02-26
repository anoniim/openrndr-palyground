package net.solvetheriddle.openrndr.dances.bars

import net.solvetheriddle.openrndr.tools.Scene
import org.openrndr.Program
import org.openrndr.color.ColorRGBa
import org.openrndr.extra.color.presets.DARK_GOLDEN_ROD
import org.openrndr.extra.color.presets.DEEP_SKY_BLUE
import org.openrndr.extra.color.presets.INDIGO
import org.openrndr.extra.easing.easeElasticOut
import org.openrndr.extra.shapes.grid
import org.openrndr.shape.Rectangle

internal class PulseScene(
    sketchBounds: Rectangle,
    numOfBars: Int,
) : Scene(60 * 10) {

    private val rectangles = sketchBounds.grid(numOfBars, numOfBars, gutterX = 10.0, gutterY = 10.0).flatten()
    private val pulseUnits = rectangles.map {
        PulseUnit(it, sketchBounds)
    }

    override fun Program.sceneFunction(frameCount: Int) {
        drawer.stroke = null
        drawer.rectangles {
            pulseUnits.forEach {
                it.update(frameCount)
                fill = it.fillColor
                rectangle(it.rectangle)
//                rectangle(it.scaledBy(2.0, 0.5, 0.5))
            }
        }
    }

    override fun reset() {
        pulseUnits.forEach { it.reset() }
    }

    private fun Program.drawCircle(frameCount: Int, lengthFrames: Int) {
        drawer.fill = null
        drawer.stroke = ColorRGBa.DEEP_SKY_BLUE
        val initRadius = drawer.bounds.width / 4.0
        println(frameCount)
        val radius = easeElasticOut(frameCount.toDouble(), initRadius, 50.0, lengthFrames.toDouble())
        drawer.circle(drawer.bounds.center, radius)
    }

    private fun getColorForSegment(factor: Double) = ColorRGBa.DARK_GOLDEN_ROD.opacify(factor)
}

class PulseUnit(
    private val initRectangle: Rectangle,
    private val bounds: Rectangle
) {

    private val originalFillColor = ColorRGBa.MAGENTA
    private val centerEdgeColor = ColorRGBa.INDIGO
    private val centerColor = ColorRGBa.DARK_GOLDEN_ROD.opacify(2.0)

    var fillColor: ColorRGBa = getFillColor(initRectangle, bounds)
    var rectangle = initRectangle

    fun update(frameCount: Int) {
        fillColor = getFillColor(initRectangle, bounds)
        rectangle = initRectangle // .scaledBy(2.0, 0.5, 0.5)
    }

    private fun getFillColor(position: Rectangle, bounds: Rectangle): ColorRGBa {
        val distFromCenter = position.center.distanceTo(bounds.center)
        val radialBoundary = bounds.width / 4.0
        return if (distFromCenter < radialBoundary) {
            centerColor.mix(centerEdgeColor, distFromCenter / radialBoundary)
        } else {
            val factor = (distFromCenter - radialBoundary) / (bounds.width / 1 - radialBoundary)
            centerEdgeColor.mix(originalFillColor, factor)
        }
    }

    fun reset() {

    }
}

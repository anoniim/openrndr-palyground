package net.solvetheriddle.openrndr.ideas

import org.openrndr.Program
import org.openrndr.animatable.Animatable
import org.openrndr.animatable.easing.Easing
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extra.shapes.grid
import org.openrndr.extra.color.presets.*
import org.openrndr.math.Vector2
import org.openrndr.panel.ControlManager
import org.openrndr.panel.elements.button
import org.openrndr.panel.elements.clicked
import org.openrndr.panel.layout
import org.openrndr.shape.Rectangle

private var state: State = State.Randomize
private const val stripeThickness = 5.0

fun main() = application {
    configure {
        width = 1200
        height = 760
    }

    program {
        setupButtons()
        val stripes = generateStripes(stripeThickness)
        draw(stripes)
        setupMouseClicks(stripes)
    }
}

private fun Program.setupButtons() {
    extend(ControlManager()) {
        layout {
            button {
                label = state.label
                println(label)
                clicked {
                    state = state.switch()
                    label = state.label
                }
            }
        }
    }
}

private fun Program.draw(stripes: List<Stripe>) {
    extend {
        drawer.stroke = null
        drawer.rectangles {
            stripes.forEach { stripe ->
                fill = stripe.color
                rectangle(stripe.originalRect)
            }
            stripes.forEach { stripe ->
                stripe.update()
                fill = stripe.color
                rectangle(stripe.currentRect)
            }
        }
    }
}

private fun Program.setupMouseClicks(stripes: List<Stripe>) {
    mouse.buttonUp.listen {
        if (state is State.Randomize) {
            repeat(100) {
                val randomPosition = stripes.random().originalRect.corner
                stripes.random().moveTo(randomPosition)
            }
        } else {
            stripes.forEach {
                it.moveTo(it.originalRect.corner)
            }
        }
    }
}

private fun Program.generateStripes(stripeThickness: Double): List<Stripe> {
    val grid = drawer.bounds.grid(
        width.toDouble(), stripeThickness,
    ).flatten()
    val gradientGroup = listOf(
        ColorRGBa.ORCHID,
        ColorRGBa.CORAL,
        ColorRGBa.BURLY_WOOD,
        ColorRGBa.AQUAMARINE,
        ColorRGBa.CORNFLOWER_BLUE
    )
    val stripesPerGroup = grid.size / (gradientGroup.size - 1)
    val stripes = grid.mapIndexed { index, rect ->
        val color = getColorForStripe(index, stripesPerGroup, gradientGroup)
        Stripe(rect, color)
    }
    return stripes
}

private fun getColorForStripe(
    index: Int,
    stripesPerGroup: Int,
    gradientGroup: List<ColorRGBa>
): ColorRGBa {
    val colorIndex = index / stripesPerGroup
    val startColor = gradientGroup[colorIndex]
    val endColor = gradientGroup[(colorIndex + 1) % gradientGroup.size]
    val gradientFactor = 1.0 / stripesPerGroup * (index % stripesPerGroup)
    return startColor.mix(endColor, gradientFactor)
}

class Stripe(
    val originalRect: Rectangle,
    val color: ColorRGBa
) : Animatable() {

    var currentRect = originalRect
    private var position = originalRect.corner

    fun moveTo(newPosition: Vector2, duration: Long = 1000L) {
        ::position.cancel()
        ::position.animate(newPosition, duration, Easing.CubicInOut)
    }

    fun update() {
        updateAnimation()
        currentRect = currentRect.movedTo(position)
    }
}

private sealed class State(val label: String) {
    object Randomize : State("Randomizing")
    object Reverse : State("Reversing")

    fun switch(): State {
        return if (this is Randomize) Reverse else Randomize
    }
}
package net.solvetheriddle.openrndr.dances.bars

import net.solvetheriddle.openrndr.Display
import net.solvetheriddle.openrndr.sketchSize
import net.solvetheriddle.openrndr.tools.Move
import net.solvetheriddle.openrndr.tools.Movie
import org.openrndr.Program
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extra.color.presets.DARK_GOLDEN_ROD
import org.openrndr.math.Vector2
import org.openrndr.shape.LineSegment

fun main() = application {
    configure {
        sketchSize(Display.LG_SQUARE_LEFT)
//        sketchSize(Display.MACBOOK_AIR)
    }
    program {
        val numOfArms = 15
        val framesPerTick = 10
        val tickAlpha = 2.0
        val attack = 0.5
        val decay = 0.020

        val decayLength = (tickAlpha / decay).toInt().also { println("decayLength: $it") }
        val movie = Movie(loop = true).apply {
            append(
                TickMove(
                    generateBars(width, height, numOfArms, tickAlpha, attack, decay),
                    framesPerTick,
                    tickAlpha, attack, decay,
                    direction = 1
                ))
            append(
                TickMove(
                    generateBars(width, height, numOfArms, tickAlpha, attack, decay),
                    framesPerTick,
                    tickAlpha, attack, decay,
                    direction = -1
                ), framesPerTick - decayLength)
        }

        extend {
//            drawer.translate(drawer.bounds.center)
            movie.play()
        }
    }
}

private fun generateBars(
    sketchWidth: Int,
    sketchHeight: Int,
    numOfBars: Int,
    tickAlpha: Double,
    attack: Double,
    decay: Double
) = List(numOfBars) {
    val lineStart = Vector2(sketchWidth / (numOfBars + 2.0) * (it + 2), sketchHeight / 4.0)
    val lineEnd = Vector2(sketchWidth / (numOfBars + 2.0) * (it + 2), 3 * sketchHeight / 4.0)
    Arm(
        lineStart, lineEnd,
        tickAlpha = tickAlpha,
        attack = attack,
        decay = decay
    )
}

private class TickMove(
    val arms: List<Arm>,
    val framesPerTick: Int,
    tickAlpha: Double, attack: Double, decay: Double,
    val direction: Int = 1,
): Move(calculateTickMoveLength(arms.size, framesPerTick, tickAlpha, attack, decay)) {

    val initTickPointer = if (direction == 1) 0 else arms.lastIndex
    var tickPointer = initTickPointer

    override fun Program.moveFunction(frameCount: Int) {
        if (frameCount % framesPerTick == 0) {
            tickPointer += direction
            if (tickPointer > 0 && tickPointer < arms.size) arms[tickPointer].tick()
        }
        arms.forEach {
            it.update(frameCount)
            it.draw()
        }
    }

    override fun reset() {
        tickPointer = initTickPointer
    }
}

private fun calculateTickMoveLength(numOfTicks: Int, framesPerTick: Int, tickAlpha: Double, attack: Double, decay: Double) : Int {
    val appearFrames = framesPerTick * (numOfTicks - 2)
    val attackFrames = (tickAlpha / attack).toInt()
    val decayFrames = (tickAlpha / decay).toInt()
    return (appearFrames + attackFrames + decayFrames)
}

private class Arm(
    start: Vector2,
    end: Vector2,
    private val tickAlpha: Double = 1.0,
    private val attack: Double = 0.5,
    private val decay: Double = 0.01,
    val strokeWeight: Double = 40.0,
) {

    private val lineSegment = LineSegment(start, end)
    private var alpha = 0.0
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

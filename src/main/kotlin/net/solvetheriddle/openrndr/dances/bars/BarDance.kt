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

        val arms = List(numOfArms) {
            val lineStart = Vector2(width/(numOfArms + 2.0) * (it + 2), height/4.0)
            val lineEnd = Vector2(width/(numOfArms + 2.0) * (it + 2), 3 * height/4.0)
            Arm(
                lineStart, lineEnd,
                initAlpha = 0.0,
                tickAlpha = tickAlpha,
                attack = attack,
                decay = decay
            )
        }

        val movie = Movie(loop = false).apply {
            append(TickMove(arms, framesPerTick, tickAlpha, attack, decay))
        }

        extend {
//            drawer.translate(drawer.bounds.center)
            movie.play()
        }
    }
}

private class TickMove(
    val arms: List<Arm>,
    val framesPerTick: Int,
    tickAlpha: Double, attack: Double, decay: Double,
): Move(calculateTickMoveLength(arms.size, framesPerTick, tickAlpha, attack, decay)) {

    var tickPointer = 0

    override fun Program.moveFunction(frameCount: Int) {
        if (frameCount % framesPerTick == 0) {
            tickPointer++
            if (tickPointer < arms.size) arms[tickPointer].tick()
        }
        arms.forEach {
            it.update(frameCount)
            it.draw()
        }
    }
}

private fun calculateTickMoveLength(numOfTicks: Int, framesPerTick: Int, tickAlpha: Double, attack: Double, decay: Double) : Int {
    val attackFrames = (tickAlpha / attack).toInt()
    val decayFrames = (tickAlpha / decay).toInt()
    val appearFrames = framesPerTick * (numOfTicks - 2)
    return appearFrames + attackFrames + decayFrames
}

private class Arm(
    start: Vector2,
    end: Vector2,
    private val tickAlpha: Double = 1.0,
    initAlpha: Double = 0.0,
    private val attack: Double = 0.5,
    private val decay: Double = 0.01,
    val strokeWeight: Double = 40.0,
) {

    private val lineSegment = LineSegment(start, end)
    private var alpha = initAlpha
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

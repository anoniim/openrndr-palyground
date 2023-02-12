package net.solvetheriddle.openrndr.tools

import org.openrndr.Program

internal class Movie(
    private val loop: Boolean,
    private val moves: List<Move>
) {

    private val length = moves.sumOf { it.lengthFrames }

    context(Program)
    fun play(frameCount: Int) {
        moves.forEach {
            it.execute(if (loop) frameCount % length else frameCount)
        }
    }
}

internal open class Move(
    private val fromFrame: Int,
    val lengthFrames: Int,
    val moveFunction: Program.(Int) -> Unit
) {
    context(Program)
    fun execute(globalFrameCount: Int) {
        if (globalFrameCount < fromFrame || globalFrameCount > fromFrame + lengthFrames) return
        val frameCount = globalFrameCount - fromFrame
        moveFunction(this@Program, frameCount)
    }
}
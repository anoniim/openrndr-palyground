package net.solvetheriddle.openrndr.tools

import org.openrndr.Program
import java.lang.IllegalStateException

/** Represents a collection of [Move]s that are executed at certain point (frame) within the program. */
class Movie(
    private val loop: Boolean = true,
) {

    private val moves: MutableMap<Move, Int> = mutableMapOf()

    /** Appends given [move] to the end of the movie (after the last added Move). [startOffset] is factored in when calculating start frame. */
    fun append(move: Move, startOffset: Int = 0) {
        val startFrame = if (moves.isNotEmpty()) {
            val lastMove = moves.keys.last()
            val lastMoveStart = moves[lastMove] ?: throw IllegalStateException()
            lastMoveStart + lastMove.lengthFrames + startOffset
        } else 0

        add(move, startFrame)
    }

    /** Adds given [move] to the movie. The Move starts on the given [startFrame] */
    @Suppress("MemberVisibilityCanBePrivate")
    fun add(move: Move, startFrame: Int = 0) {
        moves[move] = startFrame
        updateTotalLength()
    }

    context(Program)
    fun play(onFinish: () -> Unit = {}) {
        val movieFrameCount = if (loop) frameCount % totalLength else frameCount
        moves.keys.forEach {
            val moveStart = moves[it] ?: throw IllegalStateException()
            val moveEnd = moveStart + it.lengthFrames - 1
            // Skip if it's not this move's turn
            if (movieFrameCount in moveStart .. moveEnd) {
                val localFrameCount = movieFrameCount - moveStart
                it.execute(localFrameCount)
            }
            // Reset after last frame of the move
            if (movieFrameCount == moveEnd) it.reset()
        }
        if (movieFrameCount == totalLength) {
            onFinish()
        }
    }

    private var totalLength = 0

    private fun updateTotalLength() {
        totalLength = moves.keys.maxOf {
            val startFrame = moves[it] ?: throw IllegalStateException()
            startFrame + it.lengthFrames
        }
    }
}

abstract class Move(
    val lengthFrames: Int,
) {

    val lastFrame = lengthFrames - 1

    /** Executed when it's this moves time in the movie */
    abstract fun Program.moveFunction(frameCount: Int)

    context(Program)
    internal fun execute(localFrameCount: Int) {
        moveFunction(localFrameCount)
    }

    /** Resets the move so that it can be executed again in looping movies. Called after the last frame of the move. */
    open fun reset() {}
}
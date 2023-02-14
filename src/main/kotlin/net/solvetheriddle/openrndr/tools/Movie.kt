package net.solvetheriddle.openrndr.tools

import org.openrndr.Program
import java.lang.IllegalStateException

/** Represents a collection of [Move]s that are executed at certain point (frame) within the program. */
internal class Movie(
    private val loop: Boolean,
) {

    private val moves: MutableMap<Move, Int> = mutableMapOf()
    private val totalLength
        get() = moves.keys.maxOf {
            val fromFrame = moves[it] ?: throw IllegalStateException()
            fromFrame + it.lengthFrames
        }

    /** Appends given [move] to the end of the movie (after the last added Move) */
    fun append(move: Move) {
        val fromFrame = if (moves.isNotEmpty()) {
            val lastMove = moves.keys.last()
            val lastMoveFrom = moves[lastMove] ?: throw IllegalStateException()
            lastMoveFrom + lastMove.lengthFrames
        } else 0

        add(move, fromFrame)
    }

    /** Adds given [move] to the movie. The Move starts on the given [startFrame] */
    @Suppress("MemberVisibilityCanBePrivate")
    fun add(move: Move, startFrame: Int = 0) {
        moves[move] = startFrame
    }

    context(Program)
    fun play() {
        val movieFrameCount = if (loop) frameCount % totalLength else frameCount
        moves.keys.forEach {
            val fromFrame = moves[it] ?: throw IllegalStateException()
            // Skip if it's not this move's turn
            if (movieFrameCount >= fromFrame && movieFrameCount < fromFrame + it.lengthFrames) {
                val localFrameCount = movieFrameCount - fromFrame
                it.execute(localFrameCount)
            }
        }
    }
}

internal open class Move(
    val lengthFrames: Int,
    val moveFunction: Program.(Int) -> Unit
) {
    context(Program)
    fun execute(localFrameCount: Int) {
        moveFunction(this@Program, localFrameCount)
    }
}
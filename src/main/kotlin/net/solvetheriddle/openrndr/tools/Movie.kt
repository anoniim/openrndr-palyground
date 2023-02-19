package net.solvetheriddle.openrndr.tools

import org.openrndr.Program
import java.lang.IllegalStateException

/** Represents a collection of [Move]s that are executed at certain point (frame) within the program. */
internal class Movie(
    private val loop: Boolean = true,
) {

    private val moves: MutableMap<Move, MoveTime> = mutableMapOf()

    /** Appends given [move] to the end of the movie (after the last added Move) */
    fun append(move: Move, frameOffset: Int = 0) {
        val startFrame = if (moves.isNotEmpty()) {
            val lastMove = moves.keys.last()
            val lastMoveStart = moves[lastMove]?.startFrame ?: throw IllegalStateException()
            lastMoveStart + lastMove.lengthFrames + frameOffset
        } else 0

        add(move, startFrame, frameOffset)
    }

    /** Adds given [move] to the movie. The Move starts on the given [startFrame] +- given [startOffset] */
    @Suppress("MemberVisibilityCanBePrivate")
    fun add(move: Move, startFrame: Int = 0, startOffset: Int = 0) {
        moves[move] = MoveTime(startFrame, startOffset)
        updateTotalLength()
    }

    context(Program)
    fun play(onFinish: () -> Unit = {}) {
        val movieFrameCount = if (loop) frameCount % totalLength else frameCount
        moves.keys.forEach {
            val startFrame = moves[it]?.startFrame ?: throw IllegalStateException()
            // Skip if it's not this move's turn
            if (movieFrameCount >= startFrame && movieFrameCount < startFrame + it.lengthFrames) {
                val localFrameCount = movieFrameCount - startFrame
                it.execute(localFrameCount)
            }
        }
        if (movieFrameCount > totalLength) {
            onFinish()
        }
    }

    private var totalLength = 0

    private fun updateTotalLength() {
        totalLength = moves.keys.maxOf {
            val startFrame = moves[it]?.startFrame ?: throw IllegalStateException()
            val startOffset = moves[it]?.startOffset ?: throw IllegalStateException()
            startFrame + startOffset + it.lengthFrames
        }
    }
}

internal abstract class Move(
    val lengthFrames: Int,
) {

    abstract fun Program.moveFunction(frameCount: Int)

    context(Program)
    fun execute(localFrameCount: Int) {
        moveFunction(localFrameCount)
    }
}

private class MoveTime(
    val startFrame: Int = 0,
    val startOffset: Int = 0,
)
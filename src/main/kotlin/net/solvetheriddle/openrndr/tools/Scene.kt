package net.solvetheriddle.openrndr.tools

import org.openrndr.Program

abstract class Scene(
    val lengthFrames: Int,
) {

    private val lastFrame = lengthFrames - 1

    /**
     * Executed when it's this [Scene]'s time in the movie.
     * [frameCount] is in range 0 .. [lastFrame]
     */
    abstract fun Program.sceneFunction(frameCount: Int)

    context(Program)
    internal fun execute(sceneFrameCount: Int) {
        sceneFunction(sceneFrameCount)
        if (sceneFrameCount == lastFrame) reset()
    }

    /**
     * Resets the [Scene] so that it can be executed again in looping movies.
     * Called on this [Scene]'s last frame (after its [sceneFunction] has been called).
     */
    open fun reset() {}
}
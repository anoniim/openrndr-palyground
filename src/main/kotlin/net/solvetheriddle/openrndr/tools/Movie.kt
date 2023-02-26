package net.solvetheriddle.openrndr.tools

import org.openrndr.Program

/** Represents a collection of [Scene]s that are executed at certain point (frame) within the program. */
class Movie(
    private val loop: Boolean = true,
) {

    private val scenes: MutableList<MovieScene> = mutableListOf()

    /** Appends given [scene] to the end of the movie (after the last added [Scene]). [startOffset] is factored in when calculating start frame. */
    fun append(scene: Scene, startOffset: Int = 0) {
        val startFrame = if (scenes.isNotEmpty()) {
            val lastScene = scenes.last()
            val lastSceneStart = lastScene.startFrame
            lastSceneStart + lastScene.length + startOffset
        } else 0

        add(scene, startFrame)
    }

    /** Adds given [scene] to the movie. The [Scene] starts on the given [startFrame] */
    @Suppress("MemberVisibilityCanBePrivate")
    fun add(scene: Scene, startFrame: Int = 0) {
        scenes.add(MovieScene(scene, startFrame))
        updateTotalLength()
    }

    context(Program)
    fun play(onFinish: () -> Unit = {}) {
        val movieFrameCount = if (loop) frameCount % totalLength else frameCount
        scenes.forEach {
            val sceneStart = it.startFrame
            val sceneEnd = sceneStart + it.length - 1
            // Skip if this scene is not supposed to be played right now
            if (movieFrameCount in sceneStart .. sceneEnd) {
                val sceneFrameCount = movieFrameCount - sceneStart
                it.scene.execute(sceneFrameCount)
            }
            // Reset after last frame of the scene
            if (movieFrameCount == sceneEnd) it.scene.reset()
        }
        if (movieFrameCount == totalLength) { // FIXME movieFrameCount would never be totalLength in looping movies due to `if (loop) frameCount % totalLength`
            onFinish()
        }
    }

    private var totalLength = 0

    private fun updateTotalLength() {
        totalLength = scenes.maxOf {
            val startFrame = it.startFrame
            startFrame + it.length
        }
    }
}

private class MovieScene(
    val scene: Scene,
    val startFrame: Int,
) {
    val length = scene.lengthFrames
}

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
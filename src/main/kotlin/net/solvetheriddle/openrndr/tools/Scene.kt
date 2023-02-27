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

fun <T> compoundScene(
    initState: T,
    buildScene: CompoundSceneBuilder<T>.() -> Unit,
): Scene {
    val builder = CompoundSceneBuilder<T>()
    buildScene(builder)
    val sceneLength = builder.totalSceneLength
    return CompoundScene(sceneLength, builder.moveFactory, initState)
}

class CompoundSceneBuilder<T> {

    internal val moveFactory: MutableList<MoveGenerator<T>> = mutableListOf()

    internal var totalSceneLength = 0

    fun move(length: Int, generator: (Int, T) -> Move<T>) {
        totalSceneLength += length
        moveFactory.add(MoveGenerator(length, generator))
    }
}

class CompoundScene<T>(
    lengthFrames: Int,
    private val moveGenerators: List<MoveGenerator<T>>,
    private val initState: T,
) : Scene(lengthFrames) {

    private var movePointer = 0
    private var currentMove = moveGenerators[movePointer].generate(initState)
    private var passedMovesLength = 0

    override fun Program.sceneFunction(sceneFrameCount: Int) {
        val moveFrameCount = sceneFrameCount - passedMovesLength
        currentMove.execute(moveFrameCount)
        if (moveFrameCount.isLastFrameOfCurrentMove()) switchToNextMove()
    }

    override fun reset() {
        movePointer = 0
        currentMove = moveGenerators[movePointer].generate(initState)
        passedMovesLength = 0
    }

    private fun switchToNextMove() {
        if (movePointer < moveGenerators.lastIndex) {
            passedMovesLength += currentMove.length
            movePointer++
            val previousState = currentMove.state
            currentMove = moveGenerators[movePointer].generate(previousState)
        } else {
            reset()
        }
    }

    private fun Int.isLastFrameOfCurrentMove(): Boolean {
        return this == currentMove.length - 1
    }
}

class MoveGenerator<T>(
    private val length: Int,
    val generator: (Int, T) -> Move<T>
) {
    fun generate(state: T): Move<T> {
        return generator(length, state)
    }
}

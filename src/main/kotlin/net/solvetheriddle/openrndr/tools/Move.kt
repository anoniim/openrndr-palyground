package net.solvetheriddle.openrndr.tools

import org.openrndr.Program

abstract class Move<T>(
    val length: Int,
    initState: T
) {

    var state: T = initState
        protected set

    context(Program)
    abstract fun execute(moveFrameCount: Int)
}
package net.solvetheriddle.openrndr.maurer

import org.openrndr.application
import org.openrndr.draw.loadImage

fun main() {
    application {
        configure {
            width = 1024
            height = 1024
        }

        program {
            val image = loadImage("data/images/butterfly1.png")
        }
    }
}
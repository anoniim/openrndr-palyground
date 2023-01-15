import org.openrndr.animatable.Animatable
import org.openrndr.animatable.easing.Easing
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.loadFont
import org.openrndr.draw.loadImage
import org.openrndr.draw.tint
import org.openrndr.extra.noise.uniform
import kotlin.math.cos
import kotlin.math.sin

fun main() = application {
    program {
        class AnimatedCircle : Animatable() {
            var x = 0.0
            var y = 0.0
            var radius = 100.0
            var latch = 0.0

            fun shrink() {
                // -- first stop any running animations for the radius property
                ::radius.cancel()
                ::radius.animate(10.0, 200, Easing.CubicInOut)
            }

            fun grow() {
                ::radius.cancel()
                ::radius.animate(Double.uniform(60.0, 90.0), 200, Easing.CubicInOut)
            }

            fun jump() {
                ::x.cancel()
                ::y.cancel()
                ::x.animate(Double.uniform(0.0, width.toDouble()), 400, Easing.CubicInOut)
                ::y.animate(Double.uniform(0.0, height.toDouble()), 400, Easing.CubicInOut)
            }

            fun update() {
                updateAnimation()
                if (!::latch.hasAnimations) {
                    val duration = 2000.0.toLong()
                    ::latch.animate(0.0, duration).completed.listen {
                        val action = listOf(::shrink, ::grow, ::jump).random()
                        action()
                    }
                }
            }
        }

        val animatedCircles = List(1) {
            AnimatedCircle()
        }
        extend {
            drawer.fill = ColorRGBa.PINK
            drawer.stroke = null
            for (ac in animatedCircles) {
                ac.update()
                drawer.circle(ac.x, ac.y, ac.radius)
            }
        }
    }
}
import org.openrndr.color.ColorRGBa
import org.openrndr.extra.color.presets.WHITE_SMOKE
import kotlin.random.Random

@Suppress("MemberVisibilityCanBePrivate", "unused")
object Colors {
    val GREY1 = ColorRGBa(0.1, 0.1, 0.1)
    val GREY5 = ColorRGBa(0.5, 0.5, 0.5)
    val GREY9 = ColorRGBa(0.9, 0.9, 0.9)

    val flame = listOf(
        ColorRGBa(0.93, 0.65, 0.17),
        ColorRGBa(0.93, 0.34, 0.13),
        ColorRGBa(0.13, 0.12, 0.13),
        ColorRGBa(0.19, 0.15, 0.24),
        ColorRGBa(0.27, 0.21, 0.36)
    )

    val pastel = listOf(
        ColorRGBa(1.0, 0.68, 0.68),
        ColorRGBa(1.0, 0.84, 0.65),
        ColorRGBa(0.99, 1.0, 0.71),
        ColorRGBa(0.79, 1.0, 0.75),
        ColorRGBa(0.61, 0.96, 1.0),
        ColorRGBa(0.63, 0.77, 1.0),
        ColorRGBa(0.74, 0.7, 1.0),
        ColorRGBa(1.0, 0.78, 1.0),
        ColorRGBa(1.0, 1.0, 0.99),
    )

    val sunrise = listOf(
        ColorRGBa(0.98, 0.25, 0.27),
        ColorRGBa(0.95, 0.45, 0.17),
        ColorRGBa(0.97, 0.59, 0.12),
        ColorRGBa(0.98, 0.52, 0.29),
        ColorRGBa(0.98, 0.78, 0.31),
        ColorRGBa(0.56, 0.75, 0.43),
        ColorRGBa(0.26, 0.67, 0.55),
        ColorRGBa(0.3, 0.56, 0.56),
        ColorRGBa(0.34, 0.46, 0.56),
        ColorRGBa(0.15, 0.49, 0.63),
    )

    val dirtyBeach = listOf(
        ColorRGBa(0.44, 0.44, 0.44),
        ColorRGBa(0.39, 0.39, 0.44),
        ColorRGBa(0.39, 0.39, 0.57),
        ColorRGBa(0.39, 0.51, 0.59),
        ColorRGBa(0.37, 0.53, 0.63),
        ColorRGBa(0.27, 0.55, 0.63),
        ColorRGBa(0.27, 0.61, 0.69),
        ColorRGBa(0.2, 0.65, 0.73),
        ColorRGBa(0.11, 0.68, 0.83),
        ColorRGBa(0.11, 0.68, 0.83),
        ColorRGBa(0.07, 0.63, 0.83),
        ColorRGBa(0.07, 0.63, 0.83),
        ColorRGBa(0.07, 0.68, 0.89),
        ColorRGBa(0.07, 0.68, 0.89),
        ColorRGBa(0.08, 0.72, 0.95),
        ColorRGBa(0.08, 0.72, 0.95),
        ColorRGBa(0.0, 0.63, 1.0),
        ColorRGBa(0.0, 0.63, 1.0),
        ColorRGBa(0.31, 0.71, 1.0),
        ColorRGBa(0.47, 0.78, 1.0),
        ColorRGBa(0.71, 0.9, 1.0),
        ColorRGBa(0.86, 1.0, 1.0),
        ColorRGBa(1.0, 1.0, 1.0)
    )

    val blues = listOf(
        ColorRGBa(0.01, 0.02, 0.37),
        ColorRGBa(0.01, 0.17, 0.41),
        ColorRGBa(0.01, 0.24, 0.54),
        ColorRGBa(0.01, 0.28, 0.58),
        ColorRGBa(0.01, 0.32, 0.6),
        ColorRGBa(0.01, 0.36, 0.62),
        ColorRGBa(0.0, 0.47, 0.71),
        ColorRGBa(0.0, 0.59, 0.78),
        ColorRGBa(0.0, 0.65, 0.8),
        ColorRGBa(0.0, 0.71, 0.85),
        ColorRGBa(0.0, 0.75, 0.86),
        ColorRGBa(0.28, 0.79, 0.89),
        ColorRGBa(0.56, 0.88, 0.94),
        ColorRGBa(0.68, 0.91, 0.96),
        ColorRGBa(0.79, 0.94, 0.97),
    )

    val reds = listOf(
        ColorRGBa(0.35, 0.05, 0.13),
        ColorRGBa(0.5, 0.06, 0.18),
        ColorRGBa(0.64, 0.07, 0.24),
        ColorRGBa(0.72, 0.08, 0.27),
        ColorRGBa(0.79, 0.09, 0.29),
        ColorRGBa(0.87, 0.13, 0.33),
        ColorRGBa(0.95, 0.25, 0.37),
        ColorRGBa(1.0, 0.3, 0.43),
        ColorRGBa(1.0, 0.46, 0.56),
        ColorRGBa(1.0, 0.56, 0.64),
        ColorRGBa(1.0, 0.7, 0.76),
        ColorRGBa(1.0, 0.8, 0.84),
        ColorRGBa(1.0, 0.94, 0.95),
    )

    val greens = listOf(
        ColorRGBa(0.0, 0.29, 0.14),
        ColorRGBa(0.0, 0.39, 0.0),
        ColorRGBa(0.0, 0.45, 0.0),
        ColorRGBa(0.0, 0.5, 0.0),
        ColorRGBa(0.08, 0.58, 0.0),
        ColorRGBa(0.16, 0.66, 0.0),
        ColorRGBa(0.22, 0.69, 0.0),
        ColorRGBa(0.3, 0.77, 0.0),
        ColorRGBa(0.44, 0.88, 0.0),
        ColorRGBa(0.62, 0.94, 0.1),
        ColorRGBa(0.8, 1.0, 0.2),
    )

    val yellowToPurple = listOf(
        ColorRGBa(0.59, 0.59, 0.27),
        ColorRGBa(0.93, 0.89, 0.05),
        ColorRGBa(0.93, 0.49, 0.23),
        ColorRGBa(0.9, 0.3, 0.48),
        ColorRGBa(0.87, 0.11, 0.73),
        ColorRGBa(0.39, 0.27, 0.78),
    )

    val augustiniColors = listOf(
        ColorRGBa(0.0, 0.0, 0.0)
    )

    val blackAndWhite = listOf(
        GREY1,
        ColorRGBa.WHITE_SMOKE,
    )

    val collections = listOf(
        flame, pastel, sunrise, dirtyBeach, blues, reds, greens, yellowToPurple
    )

    val random: ColorRGBa
        get() {
            return ColorRGBa(
                Random.nextDouble(0.20, 0.8),
                Random.nextDouble(0.20, 0.8),
                Random.nextDouble(0.20, 0.8)
            )
        }
}

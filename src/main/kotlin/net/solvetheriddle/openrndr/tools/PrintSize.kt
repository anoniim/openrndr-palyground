package net.solvetheriddle.openrndr.tools

import kotlin.math.ceil

private const val DEFAULT_PPI = 300

open class PrintSize(
    private val widthRatio: Double,
    private val heightRatio: Double,
    private val ppi: Int = DEFAULT_PPI,
) {

    val width: Int by lazy { ceil(ppi * widthRatio).toInt() }
    val height: Int by lazy { ceil(ppi * heightRatio).toInt() }

    fun landscape(): PrintSize {
        return PrintSize(heightRatio, widthRatio, ppi)
    }

    @Suppress("unused", "FunctionName")
    companion object {
        fun A7(ppi: Int = DEFAULT_PPI) = PrintSize(widthRatio = 2.9134, heightRatio = 4.1339, ppi)
        fun A6(ppi: Int = DEFAULT_PPI) = PrintSize(widthRatio = 4.1339, heightRatio = 5.8268, ppi)
        fun A5(ppi: Int = DEFAULT_PPI) = PrintSize(widthRatio = 5.8268, heightRatio = 8.2677, ppi)
        fun A4(ppi: Int = DEFAULT_PPI) = PrintSize(widthRatio = 8.2677, heightRatio = 11.6929, ppi)
        fun A3(ppi: Int = DEFAULT_PPI) = PrintSize(widthRatio = 11.6929, heightRatio = 16.5354, ppi)
        fun A2(ppi: Int = DEFAULT_PPI) = PrintSize(widthRatio = 16.5354, heightRatio = 23.3858, ppi)
        fun A1(ppi: Int = DEFAULT_PPI) = PrintSize(widthRatio = 23.3858, heightRatio = 33.1102, ppi)
        fun A0(ppi: Int = DEFAULT_PPI) = PrintSize(widthRatio = 33.1102, heightRatio = 46.8110, ppi)

        fun custom(widthRatio: Double, heightRatio: Double, ppi: Int) = PrintSize(widthRatio, heightRatio, ppi)
    }
}
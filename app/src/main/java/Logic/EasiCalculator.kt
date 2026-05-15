package Logic

class EasiCalculator {

    fun calculate(
        score: Double
    ): Double {
        return score
    }

    fun severity(
        value: Double
    ): String {
        return when {
            value < 7 -> "Lieve"
            value <= 21 -> "Moderata"
            else -> "Severa"
        }
    }
}
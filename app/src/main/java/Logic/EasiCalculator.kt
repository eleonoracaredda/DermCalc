package Logic

class EasiCalculator {

    fun getMultiplier(bodyPartIndex: Int): Double {
        return when (bodyPartIndex) {
            0 -> 0.1 // Testa e collo
            1 -> 0.3 // Tronco
            2 -> 0.2 // Arti superiori
            3 -> 0.4 // Arti inferiori
            else -> 0.0
        }
    }

    fun calculateRegionScore(
        signsSum: Int,
        areaScore: Int,
        bodyPartIndex: Int
    ): Double {
        return signsSum.toDouble() * areaScore.toDouble() * getMultiplier(bodyPartIndex)
    }

    fun severity(value: Double): String {
        return when {
            value <= 0.0 -> "Assente"
            value <= 1.0 -> "Quasi assente"
            value <= 16.0 -> "Lieve"
            value <= 50.0 -> "Moderata"
            else -> "Severa"
        }
    }
}

package Logic

class EasiCalculator {

    /**
     * Calcola il punteggio EASI per un singolo distretto corporeo.
     * @param signsSum Somma dei 4 segni clinici (0-12)
     * @param areaScore Punteggio area (0-6)
     * @param bodyPartIndex Indice del distretto (0: Testa, 1: Tronco, 2: Arti Sup, 3: Arti Inf)
     */
    fun calculateRegionScore(
        signsSum: Int,
        areaScore: Int,
        bodyPartIndex: Int
    ): Double {
        val multiplier = when (bodyPartIndex) {
            0 -> 0.1 // Testa e collo (10%)
            1 -> 0.3 // Tronco (30%)
            2 -> 0.2 // Arti superiori (20%)
            3 -> 0.4 // Arti inferiori (40%)
            else -> 0.0
        }
        return signsSum.toDouble() * areaScore.toDouble() * multiplier
    }

    fun severity(
        value: Double
    ): String {
        return when {
            value <= 0.0 -> "Assente"
            value <= 16.0 -> "Lieve"
            value <= 50.0 -> "Moderata"
            value <= 72.0 -> "Severa"
            else -> "Severa"
        }
    }
}

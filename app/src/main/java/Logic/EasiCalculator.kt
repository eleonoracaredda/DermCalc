package com.example.dermcalc_princ.logic

// Classe per il calcolo dell'EASI (Eczema Area and Severity Index)
class EasiCalculator {

    // Restituisce il moltiplicatore relativo al distretto corporeo selezionato
    fun getMultiplier(bodyPartIndex: Int): Double {
        return when (bodyPartIndex) {
            0 -> 0.1 // Testa e collo: incide per il 10%
            1 -> 0.3 // Tronco: incide per il 30%
            2 -> 0.2 // Arti superiori: incidono per il 20%
            3 -> 0.4 // Arti inferiori: incidono per il 40%
            else -> 0.0
        }
    }

    // Calcola il punteggio parziale per una specifica regione corporea
    // Somma dei segni * punteggio dell'area * moltiplicatore della regione
    fun calculateRegionScore(
        signsSum: Int,
        areaScore: Int,
        bodyPartIndex: Int
    ): Double {
        return signsSum.toDouble() * areaScore.toDouble() * getMultiplier(bodyPartIndex)
    }

    // Determina la categoria di gravità dell'eczema basandosi sul punteggio EASI totale
    fun severity(value: Double): String {
        return when {
            value <= 0.0 -> "Assente"                 // Punteggio zero: nessuna dermatite
            value <= 1.0 -> "Eczema chiarito"         // Punteggio fino a 1: gravità minima
            value <= 7.0 -> "Dermatite Lieve"         // Punteggio tra 1.1 e 7: gravità lieve
            value <= 21.0 -> "Dermatite Moderata"     // Punteggio tra 7.1 e 21: gravità moderata
            else -> "Dermatite Severa"                // Punteggio oltre 21.1 fino a 72.0: gravità severa
        }
    }
}

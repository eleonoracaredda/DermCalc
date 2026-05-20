package com.example.dermcalc_princ.logic

// Classe per il calcolo dell'Indice di Massa Corporea (BMI)
class BmiCalculator {
    
    // Formula: peso / (altezza in metri al quadrato)
    // L'altezza viene divisa per 100 per convertirla da cm a metri
    fun calculate(weight: Double, height: Double): Double {
        if (height <= 0) return 0.0
        return weight / ((height / 100) * (height / 100))
    }

    // Determina la categoria di peso in base al BMI
    fun getSeverity(bmi: Double): String {
        return when {
            bmi < 18.5 -> "Sottopeso"
            bmi < 25 -> "Normopeso"
            bmi < 30 -> "Sovrappeso"
            else -> "Obesità"
        }
    }
}

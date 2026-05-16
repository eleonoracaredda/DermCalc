package Logic

// Classe per il calcolo dell'Indice di Massa Corporea (BMI)
class BmiCalculator {
    
    // Calcola il BMI partendo da peso (in kg) e altezza (in cm)
    fun calculate(weight: Double, height: Double): Double {
        // Se l'altezza è zero o negativa, restituisce 0 per evitare errori di divisione
        if (height <= 0) return 0.0
        
        // Formula: peso / (altezza in metri al quadrato)
        // L'altezza viene divisa per 100 per convertirla da cm a metri
        return weight / ((height / 100) * (height / 100))
    }
}

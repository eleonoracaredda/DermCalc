package Logic

class BmiCalculator {
    fun calculate(weight: Double, height: Double): Double {
        if (height <= 0) return 0.0
        return weight / ((height / 100) * (height / 100))
    }
}

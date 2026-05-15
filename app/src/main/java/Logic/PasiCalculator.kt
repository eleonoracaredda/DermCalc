package Logic

import Dominio.DatiDistretto

class PasiCalculator {

    fun calculate(
        testa: DatiDistretto,
        artiSup: DatiDistretto,
        tronco: DatiDistretto,
        artiInf: DatiDistretto
    ): Double {

        return score(testa) +
                score(artiSup) +
                score(tronco) +
                score(artiInf)
    }

    private fun score(d: DatiDistretto): Double {
        return (d.eritema +
                d.indurimento +
                d.desquamazione) *
                d.area *
                d.peso
    }

    fun severity(value: Double): String {
        return when {
            value < 5 -> "Lieve"
            value <= 10 -> "Moderata"
            else -> "Severa"
        }
    }
}
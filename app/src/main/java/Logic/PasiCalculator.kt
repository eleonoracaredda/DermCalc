package Logic

import Dominio.DatiDistretto

// Classe per il calcolo del PASI (Psoriasis Area and Severity Index)
class PasiCalculator {

    // Calcola il punteggio PASI totale sommando i punteggi dei quattro distretti corporei:
    // testa, arti superiori, tronco e arti inferiori.
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

    // Calcola il punteggio parziale per un singolo distretto corporeo.
    // Il calcolo somma i valori di eritema, indurimento e desquamazione,
    // e moltiplica il risultato per il valore dell'area e per il peso del distretto.
    private fun score(d: DatiDistretto): Double {
        return (d.eritema +
                d.indurimento +
                d.desquamazione) *
                d.area *
                d.peso
    }

    // Determina la categoria di gravità della psoriasi in base al valore PASI calcolato.
    fun severity(value: Double): String {
        return when {
            value < 5 -> "Lieve"       // Gravità lieve per punteggi minori di 5
            value <= 10 -> "Moderata"  // Gravità moderata per punteggi tra 5 e 10 (inclusi)
            else -> "Severa"           // Gravità severa per punteggi superiori a 10
        }
    }
}

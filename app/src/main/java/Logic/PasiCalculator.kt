package com.example.dermcalc_princ.logic

import com.example.dermcalc_princ.dominio.DatiDistretto

// Classe per il calcolo del PASI (Psoriasis Area and Severity Index)
// Il calcolo si basa sulla somma dei segni clinici pesati per l'estensione dell'area 
// e per il peso specifico del distretto corporeo.
class PasiCalculator {

    // Calcola il punteggio PASI totale sommando i punteggi dei quattro distretti corporei:
    // Testa (10%), Arti Superiori (20%), Tronco (30%), Arti Inferiori (40%).
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
    // Formula: (Eritema + Indurimento + Desquamazione) * Area * Peso del distretto.
    // I segni (E, I, D) hanno punteggio 0-4. L'area (A) ha punteggio 0-6.
    fun score(d: DatiDistretto): Double {
        return (d.eritema +
                d.indurimento +
                d.desquamazione) *
                d.area *
                d.peso
    }

    // Determina la categoria di gravità della psoriasi in base al valore PASI totale.
    fun severity(value: Double): String {
        return when {
            value < 10 -> "Lieve"       // Punteggio inferiore a 10
            value <= 20 -> "Moderata"   // Punteggio compreso tra 10 e 20 (inclusi)
            else -> "Grave"             // Punteggio superiore a 20
        }
    }
}

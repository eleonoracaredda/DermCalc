package dominio

// Rappresenta i dati clinici relativi a un singolo distretto corporeo per il calcolo del PASI
data class DatiDistretto(

    // Intensità dell'arrossamento (eritema)
    val eritema: Int,

    // Grado di ispessimento della pelle (indurimento/infiltrazione)
    val indurimento: Int,

    // Quantità di squame presenti (desquamazione)
    val desquamazione: Int,

    // Estensione della superficie colpita nel distretto
    val area: Int,

    // Peso percentuale del distretto rispetto alla superficie corporea totale
    val peso: Double
)

package Dermacalc_princ.pazienti

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.Dermcalc_princ.R
import Dominio.Pazienti

class PazienteAdapter(
    private val pazienti: List<Pazienti>,
    private val onPazienteClick: (Pazienti) -> Unit, // Callback per il click sull'intero elemento
    private val onEditClick: (Pazienti) -> Unit      // Callback per il click sul tasto di modifica
) : RecyclerView.Adapter<PazienteAdapter.PazienteViewHolder>() {

    // ViewHolder che contiene i riferimenti alle view per ogni riga della lista
    class PazienteViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNome: TextView = view.findViewById(R.id.tvNomePaziente)
        val tvCodiceFiscale: TextView = view.findViewById(R.id.tvCodiceFiscale)
        val btnEdit: ImageButton = view.findViewById(R.id.btnEditPaziente)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PazienteViewHolder {
        // Inflata il layout dell'item della lista
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_paziente, parent, false)
        return PazienteViewHolder(view)
    }

    override fun onBindViewHolder(holder: PazienteViewHolder, position: Int) {
        val paziente = pazienti[position]
        
        // Imposta i dati del paziente nelle view
        holder.tvNome.text = "${paziente.nome} ${paziente.cognome}"
        holder.tvCodiceFiscale.text = paziente.codiceFiscale
        
        // Gestione del click sulla riga per visualizzare i dettagli
        holder.itemView.setOnClickListener {
            onPazienteClick(paziente)
        }

        // Gestione del click sul pulsante di modifica
        holder.btnEdit.setOnClickListener {
            onEditClick(paziente)
        }
    }

    override fun getItemCount() = pazienti.size
}

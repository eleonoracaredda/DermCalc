package dermacalc_princ.misurazioni

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dermcalc_princ.R
import dominio.Misurazione
import java.text.SimpleDateFormat
import java.util.*

class MisurazioniAdapter(
    private val lista: List<Misurazione>,      // Lista delle misurazioni
    private val onClick: (Misurazione) -> Unit // Callback sul click
) : RecyclerView.Adapter<MisurazioniAdapter.ViewHolder>() {

    private val df = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTipo: TextView = view.findViewById(R.id.tvTipo)
        val tvValore: TextView = view.findViewById(R.id.tvValore)
        val tvSeverita: TextView = view.findViewById(R.id.tvSeverita)
        val tvData: TextView = view.findViewById(R.id.tvData)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Card della misurazione
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_misurazione, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val m = lista[position]

        // Popolo la card
        holder.tvTipo.text = m.tipo
        holder.tvValore.text = "Valore: %.2f".format(m.valore)
        holder.tvSeverita.text = "Severità: ${m.severita}"
        holder.tvData.text = df.format(m.data)

        // Click → apro modifica
        holder.itemView.setOnClickListener { onClick(m) }
    }

    override fun getItemCount() = lista.size
}

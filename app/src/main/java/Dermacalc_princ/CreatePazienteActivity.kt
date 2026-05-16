package Dermacalc_princ

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.Dermcalc_princ.R
import com.google.android.material.textfield.TextInputEditText
import Database.AppDatabase
import Dominio.Pazienti
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class CreatePazienteActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_paziente)

        val etNome = findViewById<TextInputEditText>(R.id.etNome)
        val etCognome = findViewById<TextInputEditText>(R.id.etCognome)
        val etCodiceFiscale = findViewById<TextInputEditText>(R.id.etCodiceFiscale)
        val etDataNascita = findViewById<TextInputEditText>(R.id.etDataNascita)
        val btnSalva = findViewById<Button>(R.id.btnSalvaPaziente)

        val database = AppDatabase.getDatabase(this)

        btnSalva.setOnClickListener {
            val nome = etNome.text.toString().trim()
            val cognome = etCognome.text.toString().trim()
            val codiceFiscale = etCodiceFiscale.text.toString().trim()
            val dataNascitaStr = etDataNascita.text.toString().trim()

            if (nome.isNotEmpty() && cognome.isNotEmpty() && codiceFiscale.isNotEmpty() && dataNascitaStr.isNotEmpty()) {
                val date = try {
                    SimpleDateFormat("dd/MM/yyyy", Locale.ITALY).parse(dataNascitaStr)
                } catch (e: Exception) {
                    null
                }

                if (date != null) {
                    lifecycleScope.launch {
                        val nuovoPaziente = Pazienti(
                            nome = nome,
                            cognome = cognome,
                            codFiscale = codiceFiscale, // Usando lo stesso valore per entrambi i campi del database
                            codiceFiscale = codiceFiscale,
                            dataNascita = date
                        )
                        database.pazienteDao().insert(nuovoPaziente)
                        Toast.makeText(this@CreatePazienteActivity, "Paziente salvato con successo", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                } else {
                    Toast.makeText(this, "Formato data non valido (dd/mm/yyyy)", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Compila tutti i campi", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

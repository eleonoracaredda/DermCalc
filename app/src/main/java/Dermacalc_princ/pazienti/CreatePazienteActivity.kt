package Dermacalc_princ.pazienti

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.Dermcalc_princ.R
import com.google.android.material.textfield.TextInputEditText
import Database.AppDatabase
import Dominio.Pazienti
import Utils.SessionManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class CreatePazienteActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_paziente)

        val sessionManager = SessionManager(this)
        val doctorId = sessionManager.getDoctorId()

        if (doctorId == null) {
            Toast.makeText(this, "Errore sessione: effettua nuovamente il login", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val etNome = findViewById<TextInputEditText>(R.id.etNome)
        val etCognome = findViewById<TextInputEditText>(R.id.etCognome)
        val etCodiceFiscale = findViewById<TextInputEditText>(R.id.etCodiceFiscale)
        val etDataNascita = findViewById<TextInputEditText>(R.id.etDataNascita)
        val btnSalva = findViewById<Button>(R.id.btnSalvaPaziente)

        val database = AppDatabase.getDatabase(this)

        // Controllo se sono in modalità modifica
        val pazienteId = intent.getIntExtra("PAZIENTE_ID", -1)
        var pazienteEsistente: Pazienti? = null

        if (pazienteId != -1) {
            lifecycleScope.launch {
                pazienteEsistente = database.pazienteDao().getById(pazienteId)
                pazienteEsistente?.let {
                    etNome.setText(it.nome)
                    etCognome.setText(it.cognome)
                    etCodiceFiscale.setText(it.codiceFiscale)
                    etDataNascita.setText(SimpleDateFormat("dd/MM/yyyy", Locale.ITALY).format(it.dataNascita))
                    btnSalva.text = "Aggiorna Paziente"
                }
            }
        }

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
                        if (pazienteId != -1 && pazienteEsistente != null) {
                            // Modalità MODIFICA
                            val pazienteAggiornato = pazienteEsistente!!.copy(
                                nome = nome,
                                cognome = cognome,
                                codiceFiscale = codiceFiscale,
                                dataNascita = date,
                                terapia = pazienteEsistente!!.terapia, // per ora invariato
                                dataInizioTerapia = pazienteEsistente!!.dataInizioTerapia,
                                consenso = pazienteEsistente!!.consenso,      // mantieni il valore già salvato
                                comorbilita = pazienteEsistente!!.comorbilita,
                                caregiverNome = pazienteEsistente!!.caregiverNome,
                                caregiverTelefono = pazienteEsistente!!.caregiverTelefono
                            )
                            database.pazienteDao().update(pazienteAggiornato)
                            Toast.makeText(this@CreatePazienteActivity, "Paziente aggiornato con successo", Toast.LENGTH_SHORT).show()
                        } else {
                            // Modalità INSERIMENTO
                            val nuovoPaziente = Pazienti(
                                nome = nome,
                                cognome = cognome,
                                codiceFiscale = codiceFiscale,
                                dataNascita = date,
                                dottoreId = doctorId,
                                terapia = null,
                                dataInizioTerapia = null,
                                consenso = false,          // per ora sempre false
                                comorbilita = null,
                                caregiverNome = null,
                                caregiverTelefono = null
                            )
                            database.pazienteDao().insert(nuovoPaziente)
                            Toast.makeText(this@CreatePazienteActivity, "Paziente salvato con successo", Toast.LENGTH_SHORT).show()
                        }
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

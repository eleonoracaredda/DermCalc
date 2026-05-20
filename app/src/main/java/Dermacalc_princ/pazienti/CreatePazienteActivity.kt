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
        val etTerapia = findViewById<TextInputEditText>(R.id.etTerapia)
        val etComorbilita = findViewById<TextInputEditText>(R.id.etComorbilita)
        val etDataInizioTerapia = findViewById<TextInputEditText>(R.id.etDataInizioTerapia)
        val etCaregiverNome = findViewById<TextInputEditText>(R.id.etCaregiverNome)
        val etCaregiverTelefono = findViewById<TextInputEditText>(R.id.etCaregiverTelefono)
        val swConsenso = findViewById<com.google.android.material.switchmaterial.SwitchMaterial>(R.id.swConsenso)
        val rgSesso = findViewById<android.widget.RadioGroup>(R.id.rgSesso)
        val rbMaschio = findViewById<android.widget.RadioButton>(R.id.rbMaschio)
        val rbFemmina = findViewById<android.widget.RadioButton>(R.id.rbFemmina)
        val btnSalva = findViewById<Button>(R.id.btnSalvaPaziente)

        val database = AppDatabase.getDatabase(this)
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.ITALY)

        // Verifica se l'activity è stata aperta per modificare un paziente esistente
        val pazienteId = intent.getIntExtra("PAZIENTE_ID", -1)
        var pazienteEsistente: Pazienti? = null

        if (pazienteId != -1) {
            // Se l'ID è presente, recupera i dati del paziente dal database per popolare i campi
            lifecycleScope.launch {
                pazienteEsistente = database.pazienteDao().getById(pazienteId)
                pazienteEsistente?.let {
                    etNome.setText(it.nome)
                    etCognome.setText(it.cognome)
                    etCodiceFiscale.setText(it.codiceFiscale)
                    etDataNascita.setText(dateFormat.format(it.dataNascita))
                    // Imposta il sesso corretto nel RadioGroup
                    if (it.sesso == "M") rbMaschio.isChecked = true else rbFemmina.isChecked = true
                    etTerapia.setText(it.terapia ?: "")
                    etComorbilita.setText(it.comorbilita ?: "")
                    // Formatta e visualizza la data inizio terapia se presente
                    it.dataInizioTerapia?.let { data -> etDataInizioTerapia.setText(dateFormat.format(data)) }
                    etCaregiverNome.setText(it.caregiverNome ?: "")
                    etCaregiverTelefono.setText(it.caregiverTelefono ?: "")
                    swConsenso.isChecked = it.consenso
                    // Cambia il testo del pulsante in modalità modifica
                    btnSalva.text = "Aggiorna Paziente"
                }
            }
        }

        // Gestione del click sul pulsante Salva/Aggiorna
        btnSalva.setOnClickListener {
            // Recupero dei valori inseriti dall'utente
            val nome = etNome.text.toString().trim()
            val cognome = etCognome.text.toString().trim()
            val codiceFiscale = etCodiceFiscale.text.toString().trim()
            val dataNascitaStr = etDataNascita.text.toString().trim()
            val sesso = if (rgSesso.checkedRadioButtonId == R.id.rbMaschio) "M" else "F"
            val terapia = etTerapia.text.toString().trim()
            val comorbilita = etComorbilita.text.toString().trim()
            val dataInizioTerapiaStr = etDataInizioTerapia.text.toString().trim()
            val caregiverNome = etCaregiverNome.text.toString().trim()
            val caregiverTelefono = etCaregiverTelefono.text.toString().trim()
            val consenso = swConsenso.isChecked

            // Validazione dei campi obbligatori
            if (nome.isNotEmpty() && cognome.isNotEmpty() && codiceFiscale.isNotEmpty() && dataNascitaStr.isNotEmpty()) {
                // Parsing della data di nascita
                val dateNascita = try {
                    dateFormat.parse(dataNascitaStr)
                } catch (e: Exception) {
                    null
                }

                // Parsing opzionale della data inizio terapia
                val dateInizioTerapia = if (dataInizioTerapiaStr.isNotEmpty()) {
                    try {
                        dateFormat.parse(dataInizioTerapiaStr)
                    } catch (e: Exception) {
                        null
                    }
                } else null

                if (dateNascita != null) {
                    lifecycleScope.launch {
                        if (pazienteId != -1 && pazienteEsistente != null) {
                            // Aggiornamento di un paziente esistente (Modalità MODIFICA)
                            val pazienteAggiornato = pazienteEsistente!!.copy(
                                nome = nome,
                                cognome = cognome,
                                codiceFiscale = codiceFiscale,
                                dataNascita = dateNascita,
                                sesso = sesso,
                                terapia = terapia,
                                comorbilita = comorbilita,
                                dataInizioTerapia = dateInizioTerapia,
                                consenso = consenso,
                                caregiverNome = caregiverNome,
                                caregiverTelefono = caregiverTelefono
                            )
                            database.pazienteDao().update(pazienteAggiornato)
                            Toast.makeText(this@CreatePazienteActivity, "Paziente aggiornato con successo", Toast.LENGTH_SHORT).show()
                        } else {
                            // Creazione di un nuovo paziente (Modalità INSERIMENTO)
                            val nuovoPaziente = Pazienti(
                                nome = nome,
                                cognome = cognome,
                                codiceFiscale = codiceFiscale,
                                dataNascita = dateNascita,
                                dottoreId = doctorId, // Associa il paziente al medico attualmente loggato
                                sesso = sesso,
                                terapia = terapia,
                                dataInizioTerapia = dateInizioTerapia,
                                consenso = consenso,
                                comorbilita = comorbilita,
                                caregiverNome = caregiverNome,
                                caregiverTelefono = caregiverTelefono
                            )
                            database.pazienteDao().insert(nuovoPaziente)
                            Toast.makeText(this@CreatePazienteActivity, "Paziente salvato con successo", Toast.LENGTH_SHORT).show()
                        }
                        finish() // Torna alla lista pazienti dopo il salvataggio
                    }
                } else {
                    Toast.makeText(this, "Formato data nascita non valido (dd/mm/yyyy)", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Compila i campi obbligatori (Nome, Cognome, CF, Data Nascita)", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

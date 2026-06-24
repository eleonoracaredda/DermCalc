package dermacalc_princ.pazienti

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.dermcalc_princ.R
import com.google.android.material.textfield.TextInputEditText
import database.AppDatabase
import dominio.Pazienti
import Utils.SessionManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

import Utils.LocaleHelper
import android.content.Context
import android.widget.RadioButton
import android.widget.RadioGroup
import com.google.android.material.switchmaterial.SwitchMaterial

class CreatePazienteActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        // Applica la lingua prima di caricare la UI
        super.attachBaseContext(LocaleHelper.applyLocale(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_paziente)

        // Recuperiamo il medico loggato dalla sessione
        val sessionManager = SessionManager(this)
        val doctorId = sessionManager.getDoctorId()

        // Se non c'è una sessione valida, torniamo al login
        if (doctorId == null) {
            Toast.makeText(this, "Errore sessione: effettua nuovamente il login", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Riferimenti ai componenti della UI
        val etNome = findViewById<TextInputEditText>(R.id.etNome)
        val etCognome = findViewById<TextInputEditText>(R.id.etCognome)
        val etCodiceFiscale = findViewById<TextInputEditText>(R.id.etCodiceFiscale)
        val etDataNascita = findViewById<TextInputEditText>(R.id.etDataNascita)
        val etTerapia = findViewById<TextInputEditText>(R.id.etTerapia)
        val etComorbilita = findViewById<TextInputEditText>(R.id.etComorbilita)
        val etDataInizioTerapia = findViewById<TextInputEditText>(R.id.etDataInizioTerapia)
        val etCaregiverNome = findViewById<TextInputEditText>(R.id.etCaregiverNome)
        val etCaregiverTelefono = findViewById<TextInputEditText>(R.id.etCaregiverTelefono)
        val swConsenso = findViewById<SwitchMaterial>(R.id.swConsenso)
        val rgSesso = findViewById<RadioGroup>(R.id.rgSesso)
        val rbMaschio = findViewById<RadioButton>(R.id.rbMaschio)
        val rbFemmina = findViewById<RadioButton>(R.id.rbFemmina)
        val btnSalva = findViewById<Button>(R.id.btnSalvaPaziente)

        val database = AppDatabase.getDatabase(this)
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.ITALY)

        // Se passiamo un ID, siamo in modalità modifica
        val pazienteId = intent.getIntExtra("PAZIENTE_ID", -1)
        var pazienteEsistente: Pazienti? = null

        if (pazienteId != -1) {
            // Carichiamo i dati del paziente per pre-popolare i campi
            lifecycleScope.launch {
                pazienteEsistente = database.pazienteDao().getById(pazienteId)
                pazienteEsistente?.let {
                    etNome.setText(it.nome)
                    etCognome.setText(it.cognome)
                    etCodiceFiscale.setText(it.codiceFiscale)
                    etDataNascita.setText(dateFormat.format(it.dataNascita))
                    
                    // Gestione sesso
                    if (it.sesso == "M") rbMaschio.isChecked = true else rbFemmina.isChecked = true
                    
                    etTerapia.setText(it.terapia ?: "")
                    etComorbilita.setText(it.comorbilita ?: "")
                    
                    // Data inizio terapia opzionale
                    it.dataInizioTerapia?.let { data -> etDataInizioTerapia.setText(dateFormat.format(data)) }
                    
                    etCaregiverNome.setText(it.caregiverNome ?: "")
                    etCaregiverTelefono.setText(it.caregiverTelefono ?: "")
                    swConsenso.isChecked = it.consenso
                    
                    // Cambiamo la label del pulsante
                    btnSalva.text = "Aggiorna Paziente"
                }
            }
        }

        // Salvataggio dei dati (nuovo o modifica)
        btnSalva.setOnClickListener {
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

            // Verifica campi obbligatori
            if (nome.isNotEmpty() && cognome.isNotEmpty() && codiceFiscale.isNotEmpty() && dataNascitaStr.isNotEmpty()) {
                
                // Parsing delle date
                val dateNascita = try {
                    dateFormat.parse(dataNascitaStr)
                } catch (e: Exception) {
                    null
                }

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
                            // Aggiorna paziente esistente
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
                            // Crea nuovo record
                            val nuovoPaziente = Pazienti(
                                nome = nome,
                                cognome = cognome,
                                codiceFiscale = codiceFiscale,
                                dataNascita = dateNascita,
                                dottoreId = doctorId, 
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
                        finish() // Torna indietro dopo l'operazione
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

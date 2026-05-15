package Dermacalc_princ

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.Dermcalc_princ.R

/**
 * HomeActivity rappresenta il Menu Principale con i vari calcolatori disponibili.
 */
class HomeActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Carica il layout del menu principale
        setContentView(R.layout.activity_home)
        val btnLogout = findViewById<Button>(R.id.btnLogout)

        // Gestione dell'uscita dall'applicazione (Logout)
        btnLogout.setOnClickListener {
            // Crea un Intent per tornare alla schermata di Login
            val intent = Intent(this, LoginActivity::class.java)
            
            // Pulisce tutta la cronologia delle schermate precedenti.
            // Serve per evitare che l'utente possa tornare al menu segreto dopo il logout premendo il tasto back.
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            
            startActivity(intent)
            finish()
        }
        
        // NOTA: I bottoni BMI, PASI ed EASI sono già presenti nel layout XML.
        val btnBmi = findViewById<Button>(R.id.btnBmi)
        val btnPasi = findViewById<Button>(R.id.btnPasi)
        val btnEasi = findViewById<Button>(R.id.btnEasi)

        btnBmi.setOnClickListener {
            val intent = Intent(this, BmiActivity::class.java)
            startActivity(intent)
        }
        btnPasi.setOnClickListener {
            val intent = Intent(this, PasiActivity::class.java)
            startActivity(intent)
        }
        btnEasi.setOnClickListener {
            val intent = Intent(this, EasiActivity::class.java)
            startActivity(intent)
        }
    }
}

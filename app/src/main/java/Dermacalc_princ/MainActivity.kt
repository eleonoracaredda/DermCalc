package Dermacalc_princ

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import com.example.Dermcalc_princ.R

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnLogin = findViewById<Button>(R.id.btnLogin)
        
        btnLogin.setOnClickListener {
            // Per ora permettiamo l'accesso senza controllare email e password
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish() // Chiude la schermata di login così non si può tornare indietro con il tasto back
            Toast.makeText(this, "Login effettuato", Toast.LENGTH_SHORT).show()
        }
    }
}

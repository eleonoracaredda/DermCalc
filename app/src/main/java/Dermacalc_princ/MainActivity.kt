package Dermacalc_princ

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

/**
 * MainActivity ora funge da entry point che reindirizza l'utente alla LoginActivity.
 */
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Reindirizza immediatamente l'utente alla schermata di Login
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        
        // Chiude la MainActivity in modo che non rimanga nello stack delle attività
        finish()
    }
}

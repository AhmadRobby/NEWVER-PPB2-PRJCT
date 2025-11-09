package com.example.newver

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.lifecycleScope
import com.example.newver.databinding.ActivityMainBinding
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {
    // 1. bikin binding dari main activity
    private lateinit var binding: ActivityMainBinding
    private lateinit var credentialManager: androidx.credentials.CredentialManager
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // 2. inisiasi binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        // 3. set content dari binding
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        credentialManager = androidx.credentials.CredentialManager.create(this)
        auth = Firebase.auth

        // 4. daftar event yang di perlukan
        registerEvent()

    }

    fun registerEvent() {
        // 5. daftarkan event ketika di click
        binding.btnLoginGoogle.setOnClickListener(){
            lifecycleScope.launch {
                val request = prepareRequest()
                loginByGoogle(request)
            }
        }
    }

    fun prepareRequest() : androidx.credentials.GetCredentialRequest {
        val serverClient = "848457682929-1pisj8aqfsutspn4h0m8gb2k5lejeden.apps.googleusercontent.com"

        val googleOption = GetGoogleIdOption
            .Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(serverClient)
            .build()

        val request = androidx.credentials.GetCredentialRequest
            .Builder()
            .addCredentialOption(googleOption)
            .build()

        return request

    }

    suspend fun loginByGoogle(request: androidx.credentials.GetCredentialRequest){
        try {
            val result = credentialManager.getCredential(
                context = this,
                request = request
            )
            val credentials = result.credential
            val idToken = GoogleIdTokenCredential.createFrom(credentials.data)

            firebaseLoginCallback(idToken.idToken)

        } catch (exc: NoCredentialException){
            Toast.makeText(this, "Login gagal :" + exc.message, Toast.LENGTH_LONG).show()
        } catch (exc: Exception) {
            Toast.makeText(this, "Login gagal :" + exc.message, Toast.LENGTH_LONG).show()
        }
    }



    fun firebaseLoginCallback(idToken : String){
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) {task ->
                if (task.isSuccessful){
                    Toast.makeText(this, "Login berhasil", Toast.LENGTH_LONG).show()
                    toProfilePage()
                } else {
                    Toast.makeText(this, "Login gagal ", Toast.LENGTH_LONG).show()
                }
            }
    }

    fun isAuthenticated(): Boolean {
        return auth.currentUser != null
    }

    override fun onStart() {
        super.onStart()
        if (isAuthenticated()) {
            toProfilePage()
        }
    }

    private fun toProfilePage() {
        val intent = Intent(this, TodoListActivity::class.java)
        startActivity(intent)
        finish()
    }

}


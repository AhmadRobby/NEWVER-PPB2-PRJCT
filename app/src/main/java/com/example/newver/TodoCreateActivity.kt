package com.example.newver

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.newver.databinding.ActivityTodoCreateBinding
import com.example.newver.entity.Todo
import com.example.newver.usecase.TodoUseCase
import com.google.firebase.auth.FirebaseAuth // 1. Jangan lupa import ini
import kotlinx.coroutines.launch

class TodoCreateActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTodoCreateBinding
    private lateinit var todoUseCase: TodoUseCase

    // Tambahkan variabel Auth
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityTodoCreateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inisialisasi
        auth = FirebaseAuth.getInstance()
        todoUseCase = TodoUseCase()

        registerEvents()
    }

    fun registerEvents(){
        binding.tombolTambah.setOnClickListener{
            // Validasi input dulu biar gak nyimpen data kosong
            if (binding.title.text.isNullOrEmpty()) {
                Toast.makeText(this, "Judul harus diisi ya!", Toast.LENGTH_SHORT).show()
            } else {
                saveDatatoFirestore()
            }
        }
    }

    private fun saveDatatoFirestore(){
        // 2. Cek User Login
        val currentUser = auth.currentUser

        if (currentUser == null) {
            Toast.makeText(this, "Waduh, sesi login habis. Login ulang yuk!", Toast.LENGTH_SHORT).show()
            return
        }

        // 3. Masukkan UID ke dalam object Todo
        val todo = Todo(
            id = "",
            title = binding.title.text.toString(),
            description = binding.description.text.toString(),
            createdTime = System.currentTimeMillis(), // Biar tau kapan dibuat
            userId = currentUser.uid // <--- INI KUNCINYA
        )

        lifecycleScope.launch {
            // Karena pakai UseCase, pastikan UseCase-mu menerima object Todo ini
            todoUseCase.createTodo(todo)

            // Tadi kodemu lupa .show() di akhir Toast, ini saya perbaiki
            Toast.makeText(this@TodoCreateActivity, "Tersimpan, Makasih udah cerita ya!", Toast.LENGTH_LONG).show()

            toTodoListActivity()
        }
    }

    private fun toTodoListActivity(){
        val intent = Intent(this, TodoListActivity::class.java)
        // Tambahkan flag biar gak bisa di-back ke halaman create setelah simpan
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }
}
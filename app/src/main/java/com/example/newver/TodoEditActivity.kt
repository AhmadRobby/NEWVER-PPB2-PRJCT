package com.example.newver

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.newver.databinding.ActivityTodoEditBinding
import com.example.newver.entity.Todo
import com.example.newver.usecase.TodoUseCase
import kotlinx.coroutines.launch

class TodoEditActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTodoEditBinding
    private lateinit var todoUseCase: TodoUseCase
    private lateinit var todoItemId: String

    // Simpan userId pemilik asli biar tidak hilang saat update
    private var currentUserId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityTodoEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        todoUseCase = TodoUseCase()
        todoItemId = intent.getStringExtra("todo_item_id").orEmpty()

        registerEvents()
    }

    override fun onStart() {
        super.onStart()
        loadTodo()
    }

    private fun registerEvents() {
        binding.tombolEdit.setOnClickListener {
            val title = binding.title.text.toString().trim()
            val description = binding.description.text.toString().trim()

            if (title.isEmpty() || description.isEmpty()) {
                displayMessage("Judul dan deskripsi tidak boleh kosong")
                return@setOnClickListener
            }

            // Validasi keamanan: Jangan sampai update kalau data aslinya belum dimuat
            if (currentUserId.isEmpty()) {
                displayMessage("Gagal memuat profil pemilik data")
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val payload = Todo(
                    id = todoItemId,
                    title = title,
                    description = description,
                    userId = currentUserId // <--- PENTING: Masukkan lagi ID pemiliknya!
                )

                try {
                    todoUseCase.updateTodo(payload)
                    displayMessage("Ceritamu berhasil diperbarui")
                    backToList()
                } catch (exc: Exception) {
                    displayMessage("Gagal memperbarui data: ${exc.message}")
                }
            }
        }
    }

    private fun loadTodo() {
        lifecycleScope.launch {
            // PERBAIKAN DI SINI:
            // Pakai 'getTodoById' (ambil satu), BUKAN 'getTodo' (ambil banyak)
            val todo = todoUseCase.getTodoById(todoItemId)

            if (todo == null) {
                displayMessage("Data tidak ditemukan atau error")
                backToList()
                return@launch
            }

            // Simpan userId yang didapat dari database ke variabel sementara
            currentUserId = todo.userId

            // Sekarang todo.title pasti aman karena todo adalah Single Object
            binding.title.setText(todo.title)
            binding.description.setText(todo.description)
        }
    }

    private fun backToList() {
        val intent = Intent(this, TodoListActivity::class.java) // Pastikan nama Activity list kamu benar
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    private fun displayMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityTodoEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // biar layout tidak ketimpa status bar
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

            lifecycleScope.launch {
                val payload = Todo(
                    id = todoItemId,
                    title = title,
                    description = description
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
            val todo = todoUseCase.getTodo(todoItemId)
            if (todo == null) {
                displayMessage("Data yang akan diedit tidak tersedia di server")
                backToList()
                return@launch
            }

            binding.title.setText(todo.title)
            binding.description.setText(todo.description)
        }
    }

    private fun backToList() {
        val intent = Intent(this, TodoListActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun displayMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}

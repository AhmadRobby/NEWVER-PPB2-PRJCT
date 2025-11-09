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
import kotlinx.coroutines.launch

class TodoCreateActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTodoCreateBinding
    private lateinit var  todoUseCase: TodoUseCase

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

        todoUseCase = TodoUseCase()
        registerEvents()
    }

    fun registerEvents(){
        binding.tombolTambah.setOnClickListener{
            saveDatatoFirestore()
        }
    }

    private fun saveDatatoFirestore(){
        val todo = Todo(
            id = "",
            title = binding.title.text.toString(),
            description = binding.description.text.toString()
        )

        lifecycleScope.launch {
            todoUseCase.createTodo(todo)

            Toast.makeText(this@TodoCreateActivity, "Tersimpan, Makasih udah cerita ya!", Toast.LENGTH_LONG)

            toTodoListActivity()
        }
    }

    private fun toTodoListActivity(){
        val intent = Intent(this,TodoListActivity::class.java)
        startActivity(intent)
        finish()
    }
}
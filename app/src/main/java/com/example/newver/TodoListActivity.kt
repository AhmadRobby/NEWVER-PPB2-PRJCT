package com.example.newver

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.newver.adapter.TodoAdapter
import com.example.newver.databinding.ActivityTodoListBinding
import com.example.newver.entity.Todo
import com.example.newver.usecase.TodoUseCase
import kotlinx.coroutines.launch

class TodoListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTodoListBinding
    private lateinit var todoUseCase: TodoUseCase
    private lateinit var todoAdapter: TodoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityTodoListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        todoUseCase = TodoUseCase()
        setupRecyclerView()
        registerEvents()
    }

    override fun onStart() {
        super.onStart()
        initializeData()
    }

    private fun registerEvents() {
        binding.TombolCreateTodo.setOnClickListener {
            startActivity(Intent(this, TodoCreateActivity::class.java))
        }
    }

    private fun setupRecyclerView() {
        todoAdapter = TodoAdapter(mutableListOf(), object : TodoAdapter.TodoItemEvents {
            override fun onEdit(todo: Todo) {
                val intent = Intent(this@TodoListActivity, TodoEditActivity::class.java)
                intent.putExtra("todo_item_id", todo.id)
                startActivity(intent)
            }

            override fun onDelete(todo: Todo) {
                val builder = AlertDialog.Builder(this@TodoListActivity)
                builder.setTitle("Menghapus")
                builder.setMessage("Apakah kamu yakin ingin menghapus cerita ini?")

                builder.setPositiveButton("Ya") { dialog, _ ->
                    lifecycleScope.launch {
                        try {
                            todoUseCase.deleteTodo(todo.id)
                            Toast.makeText(
                                this@TodoListActivity,
                                "Cerita berhasil dihapus",
                                Toast.LENGTH_SHORT
                            ).show()
                            initializeData()
                        } catch (exc: Exception) {
                            displayMessage("Gagal menghapus cerita: ${exc.message}")
                        }
                    }
                }

                builder.setNegativeButton("Gajadi") { dialog, _ ->
                    dialog.dismiss()
                }

                builder.show()
            }
        })

        binding.container.apply {
            adapter = todoAdapter
            layoutManager = LinearLayoutManager(this@TodoListActivity)
        }
    }

    private fun initializeData() {
        lifecycleScope.launch {
            binding.container.visibility = View.GONE
            binding.uiLoading.visibility = View.VISIBLE

            try {
                val todoList = todoUseCase.getTodo()
                Log.d("TodoList", "Data: $todoList")

                binding.uiLoading.visibility = View.GONE
                binding.container.visibility = View.VISIBLE
                todoAdapter.updateData(todoList)
            } catch (e: Exception) {
                Toast.makeText(this@TodoListActivity, e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun displayMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
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
import com.example.newver.api.RetrofitClient // <--- JANGAN LUPA IMPORT INI
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

        // --- TAMBAHAN BARU: Panggil API Quote ---
        fetchQuote()
        // Tambahkan Event Klik pada Text Quote
        binding.tvQuote.setOnClickListener {
            // Panggil ulang fungsinya saat diklik
            fetchQuote()
            Toast.makeText(this, "Mengambil quote baru...", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onStart() {
        super.onStart()
        initializeData() // Load data Todo dari Firebase
    }

    // --- FUNGSI BARU: Logic Ambil Quote dari API ---
    private fun fetchQuote() {
        lifecycleScope.launch {
            try {
                // Set text loading dulu biar user tau
                // Pastikan di XML ID-nya benar: tvQuote
                binding.tvQuote.text = "Mencari inspirasi..."

                // Panggil Retrofit
                val response = RetrofitClient.instance.getRandomQuote()

                if (response.isSuccessful) {
                    val data = response.body()
                    // Format text: "Isi Quote" - Penulis
                    val quoteText = "\"${data?.quote}\"\n- ${data?.author}"

                    binding.tvQuote.text = quoteText
                } else {
                    binding.tvQuote.text = "Gagal memuat motivasi hari ini."
                }
            } catch (e: Exception) {
                // Kalau internet mati atau error lain
                binding.tvQuote.text = "Tetap semangat menjalani hari! (Offline)"
                Log.e("API_ERROR", "Error fetch quote: ${e.message}")
            }
        }
    }

    private fun registerEvents() {
        binding.TombolCreateTodo.setOnClickListener {
            startActivity(Intent(this, TodoCreateActivity::class.java))
        }
    }

    private fun setupRecyclerView() {
        // Pastikan 'container' di XML kamu adalah RecyclerView
        // Dan pastikan kamu sudah import TodoAdapter dan TodoItemEvents dengan benar
        todoAdapter = TodoAdapter(mutableListOf(), object : TodoAdapter.TodoItemEvents {
            override fun onEdit(todo: Todo) {
                val intent = Intent(this@TodoListActivity, TodoEditActivity::class.java)
                intent.putExtra("todo_item_id", todo.id)
                startActivity(intent)
            }

            override fun onDelete(todo: Todo) {
                showDeleteConfirmation(todo)
            }
        })

        // Ganti 'container' sesuai ID RecyclerView di XML kamu (misal: rvTodoList atau tetap container)
        binding.container.apply {
            adapter = todoAdapter
            layoutManager = LinearLayoutManager(this@TodoListActivity)
        }
    }

    // Saya rapikan sedikit logic delete biar onSetupRecyclerView tidak terlalu panjang
    private fun showDeleteConfirmation(todo: Todo) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Menghapus")
        builder.setMessage("Apakah kamu yakin ingin menghapus cerita ini?")

        builder.setPositiveButton("Ya") { _, _ ->
            lifecycleScope.launch {
                try {
                    todoUseCase.deleteTodo(todo.id)
                    Toast.makeText(this@TodoListActivity, "Cerita berhasil dihapus", Toast.LENGTH_SHORT).show()
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

    private fun initializeData() {
        lifecycleScope.launch {
            // Pastikan ID 'container' dan 'uiLoading' ada di XML kamu
            binding.container.visibility = View.GONE
            binding.uiLoading.visibility = View.VISIBLE

            try {
                val todoList = todoUseCase.getTodo()
                Log.d("TodoList", "Data: $todoList")

                binding.uiLoading.visibility = View.GONE
                binding.container.visibility = View.VISIBLE
                todoAdapter.updateData(todoList)
            } catch (e: Exception) {
                binding.uiLoading.visibility = View.GONE // Sembunyikan loading kalau error
                Toast.makeText(this@TodoListActivity, e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun displayMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
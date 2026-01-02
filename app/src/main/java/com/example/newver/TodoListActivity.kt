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
import com.example.newver.api.RetrofitClient
import com.example.newver.databinding.ActivityTodoListBinding
import com.example.newver.entity.Todo
import com.example.newver.usecase.TodoUseCase
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class TodoListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTodoListBinding
    private lateinit var todoUseCase: TodoUseCase
    private lateinit var todoAdapter: TodoAdapter
    private lateinit var auth: FirebaseAuth

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

        // Inisialisasi Auth & UseCase
        auth = FirebaseAuth.getInstance()
        todoUseCase = TodoUseCase()

        // Cek Login: Kalau belum login, lempar ke MainActivity (Login)
        if (auth.currentUser == null) {
            goToLogin()
            return
        }

        setupRecyclerView()
        registerEvents()

        // Logic Quote
        fetchQuote()
        binding.tvQuote.setOnClickListener {
            fetchQuote()
            Toast.makeText(this, "Mengambil quote baru...", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onStart() {
        super.onStart()
        // Cek lagi untuk keamanan ganda, ambil data user
        if (auth.currentUser != null) {
            initializeData()
        }
    }

    private fun registerEvents() {
        // 1. Tombol Tambah
        binding.TombolCreateTodo.setOnClickListener {
            startActivity(Intent(this, TodoCreateActivity::class.java))
        }

        // 2. TOMBOL LOGOUT (BARU)
        binding.btnLogout.setOnClickListener {
            // Tampilkan dialog konfirmasi biar gak kepencet
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Keluar Akun")
            builder.setMessage("Yakin mau logout dan kembali ke halaman login?")
            builder.setPositiveButton("Ya") { _, _ ->
                logoutUser()
            }
            builder.setNegativeButton("Enggak") { dialog, _ ->
                dialog.dismiss()
            }
            builder.show()
        }
    }

    // Fungsi Logout
    private fun logoutUser() {
        auth.signOut() // Hapus sesi Firebase
        goToLogin() // Pindah halaman
    }

    private fun goToLogin() {
        val intent = Intent(this, MainActivity::class.java)
        // Hapus history activity agar tombol Back tidak mengembalikan user ke sini
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun fetchQuote() {
        lifecycleScope.launch {
            try {
                binding.tvQuote.text = "Mencari inspirasi..."
                val response = RetrofitClient.instance.getRandomQuote()
                if (response.isSuccessful) {
                    val data = response.body()
                    val quoteText = "\"${data?.quote}\"\n- ${data?.author}"
                    binding.tvQuote.text = quoteText
                } else {
                    binding.tvQuote.text = "Gagal memuat motivasi hari ini."
                }
            } catch (e: Exception) {
                binding.tvQuote.text = "Tetap semangat menjalani hari! (Offline)"
                Log.e("API_ERROR", "Error fetch quote: ${e.message}")
            }
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
                showDeleteConfirmation(todo)
            }
        })

        binding.container.apply {
            adapter = todoAdapter
            layoutManager = LinearLayoutManager(this@TodoListActivity)
        }
    }

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
            binding.container.visibility = View.GONE
            binding.uiLoading.visibility = View.VISIBLE

            val userId = auth.currentUser?.uid

            if (userId == null) {
                displayMessage("User tidak ditemukan")
                return@launch
            }

            try {
                // Mengambil data spesifik user
                val todoList = todoUseCase.getTodo(userId)
                Log.d("TodoList", "Data: $todoList")

                binding.uiLoading.visibility = View.GONE
                binding.container.visibility = View.VISIBLE
                todoAdapter.updateData(todoList)
            } catch (e: Exception) {
                binding.uiLoading.visibility = View.GONE
                Toast.makeText(this@TodoListActivity, e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun displayMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
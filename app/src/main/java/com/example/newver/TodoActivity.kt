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
import com.example.newver.databinding.ActivityTaskBinding
import com.example.newver.entity.Todo
import com.example.newver.usecase.TodoUseCase
import com.google.firebase.auth.FirebaseAuth // 1. IMPORT PENTING
import kotlinx.coroutines.launch

class TodoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTaskBinding
    private lateinit var mymyusecase: TodoUseCase
    private lateinit var mymyadapter: TodoAdapter
    private lateinit var auth: FirebaseAuth // 2. Variabel Auth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inisialisasi Auth & UseCase
        auth = FirebaseAuth.getInstance()
        mymyusecase = TodoUseCase()

        // Cek apakah user sudah login
        if (auth.currentUser == null) {
            Toast.makeText(this, "Silakan login terlebih dahulu", Toast.LENGTH_SHORT).show()
            // Opsional: Redirect ke halaman Login jika ada
            // startActivity(Intent(this, LoginActivity::class.java))
            // finish()
        }

        setupRecyclerView()

        // Panggil data hanya jika user sudah login
        if (auth.currentUser != null) {
            initializeData()
        }

        registerEvents()
    }

    private fun registerEvents() {
        binding.TombolCreateTodo.setOnClickListener{
            // Pastikan nama Activity tujuannya benar.
            // Jika nama file kamu 'TodoCreateActivity', ganti CreateTodoActivity di bawah ini.
            val intent = Intent(this, TodoCreateActivity::class.java)
            startActivity(intent)
            // finish() // Jangan finish() supaya pas diback user kembali ke list ini
        }
    }

    private fun setupRecyclerView() {
        // 3. PERBAIKAN: Adapter butuh Listener (Events)
        mymyadapter = TodoAdapter(mutableListOf(), object : TodoAdapter.TodoItemEvents {
            override fun onEdit(todo: Todo) {
                // Arahkan ke halaman Edit (jika ada)
                val intent = Intent(this@TodoActivity, TodoEditActivity::class.java)
                intent.putExtra("todo_item_id", todo.id)
                startActivity(intent)
            }

            override fun onDelete(todo: Todo) {
                showDeleteConfirmation(todo)
            }
        })

        binding.container.apply {
            adapter = mymyadapter
            layoutManager = LinearLayoutManager(this@TodoActivity)
        }
    }

    // Fungsi Dialog Konfirmasi Hapus
    private fun showDeleteConfirmation(todo: Todo) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Hapus")
        builder.setMessage("Yakin hapus cerita ini?")
        builder.setPositiveButton("Ya") { _, _ ->
            lifecycleScope.launch {
                mymyusecase.deleteTodo(todo.id)
                initializeData() // Refresh data setelah hapus
                Toast.makeText(this@TodoActivity, "Terhapus", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Batal") { dialog, _ -> dialog.dismiss() }
        builder.show()
    }

    private fun initializeData() {
        lifecycleScope.launch {
            binding.container.visibility = View.GONE
            binding.uiLoading.visibility = View.VISIBLE

            try {
                // 4. PERBAIKAN: Ambil UID User
                val userId = auth.currentUser?.uid

                if (userId != null) {
                    // 5. PERBAIKAN: Kirim userId ke UseCase
                    val todoList = mymyusecase.getTodo(userId)
                    Log.d("Tes", todoList.toString())

                    binding.uiLoading.visibility = View.GONE
                    binding.container.visibility = View.VISIBLE

                    // 6. PERBAIKAN: Masukkan variabel 'todoList' (bukan Class Todo)
                    mymyadapter.updateData(todoList)
                } else {
                    binding.uiLoading.visibility = View.GONE
                    Toast.makeText(this@TodoActivity, "User tidak ditemukan", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                binding.uiLoading.visibility = View.GONE
                Toast.makeText(this@TodoActivity, e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
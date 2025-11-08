package com.example.newver

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.newver.adapter.TodoAdapter
import com.example.newver.databinding.ActivityTaskBinding
import com.example.newver.entity.Todo
import com.example.newver.usecase.TodoUseCase
import kotlinx.coroutines.launch

class TodoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTaskBinding
    private lateinit var mymyusecase: TodoUseCase
    private lateinit var mymyadapter: TodoAdapter


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

        mymyusecase = TodoUseCase()
        setupRecyclerView()

        initializeData()

        registerEvents()
    }

    private fun registerEvents() {
        binding.TombolCreateTodo.setOnClickListener{
            val intent = Intent(this, CreateTodoActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun setupRecyclerView() {

        mymyadapter = TodoAdapter(mutableListOf())
        binding.container.apply {
            adapter = mymyadapter
            layoutManager = LinearLayoutManager(this@TodoActivity)
        }
    }

    private fun TodoAdapter(dataset: MutableList<Todo>): TodoAdapter {
        return TODO("Provide the return value")
    }

    private fun initializeData() {
        lifecycleScope.launch {
            // sembunyikan tambilan recyclerview terlebih dahulu dan tampilkan ui loading
            binding.container.visibility = View.GONE
            binding.uiLoading.visibility = View.VISIBLE

            try {
                //  ambil data dariu firebase
                var todoList = mymyusecase.getTodo()
                Log.d("Tes  ", todoList.toString())

                // jika sudah mendapatkan data dan tidak ada error tampilkan kembali recyclerview dan sembunyikan ui loading
                binding.uiLoading.visibility = View.GONE
                binding.container.visibility = View.VISIBLE

                // update data yang ada di adapter
                mymyadapter.updateData(todoList)

            } catch (e: Exception) {
                Toast.makeText(this@TodoActivity, e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
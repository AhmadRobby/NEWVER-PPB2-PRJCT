package com.example.newver.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.newver.entity.Todo
import com.example.newver.databinding.ItemTodoBinding

class TodoAdapter(
    private val dataset: MutableList<Todo>,
    private val events: TodoItemEvents,
) : RecyclerView.Adapter<TodoAdapter.CustomViewHolder>() {

    // 1. LIST WARNA (Saya masukkan di sini)
    // Format transparansi #33xxxxxx (Glass Effect)
    private val colors = listOf(
        "#336C48C5", // Ungu
        "#33C0392B", // Merah
        "#3327AE60", // Hijau
        "#332980B9", // Biru
        "#33D35400", // Oranye
        "#338E44AD"  // Ungu Muda
    )

    interface TodoItemEvents {
        fun onDelete(todo: Todo)
        fun onEdit(todo: Todo)
    }

    inner class CustomViewHolder(val view: ItemTodoBinding) : RecyclerView.ViewHolder(view.root) {

        // 2. Update fungsi binData supaya menerima 'position'
        fun binData(item: Todo, position: Int) {
            view.title.text = item.title
            view.description.text = item.description

            // --- LOGIC WARNA ---
            // Kita ambil sisa bagi index dengan jumlah warna
            val colorIndex = position % colors.size
            val selectedColor = colors[colorIndex]

            // Akses ID 'cardView' (dari item_todo.xml)
            // Pastikan di item_todo.xml ID-nya adalah @+id/card_view
            view.cardView.setCardBackgroundColor(Color.parseColor(selectedColor))

            // Event Long Click (Hapus)
            view.root.setOnLongClickListener {
                events.onDelete(todo = item)
                true
            }

            // Event Click (Edit)
            view.root.setOnClickListener {
                events.onEdit(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val binding = ItemTodoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CustomViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val data = dataset[position]
        // 3. Kirim posisi ke fungsi binData
        holder.binData(data, position)
    }

    override fun getItemCount() = dataset.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newData: List<Todo>) {
        dataset.clear()
        dataset.addAll(newData)
        notifyDataSetChanged()
    }
}
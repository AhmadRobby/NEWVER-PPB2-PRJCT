package com.example.newver.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.newver.entity.Todo
import com.example.newver.databinding.ItemTodoBinding
class TodoAdapter (
    private val dataset: MutableList<Todo>,
    private val events: TodoItemEvents,
): RecyclerView.Adapter<TodoAdapter.CustomViewHolder>(){

    interface TodoItemEvents {
        fun onDelete(todo: Todo)
        fun onEdit(todo: Todo)
    }

    inner class CustomViewHolder(val view: ItemTodoBinding)
        :RecyclerView.ViewHolder(view.root) {

        fun binData(item: Todo) {
            view.title.text = item.title
            view.description.text = item.description

            //
            view.root.setOnLongClickListener{

                events.onDelete(todo = item)

                true
            }

            view.root.setOnClickListener{
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
        holder.binData(data)
    }

    override fun getItemCount() = dataset.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newData: List<Todo>) {
        dataset.clear()
        dataset.addAll(newData)
        notifyDataSetChanged()
    }

}
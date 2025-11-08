package com.example.newver.usecase

import com.example.newver.entity.Todo
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

class TodoUseCase {
    private val db = Firebase.firestore
    private val collectionName = "todo" // pastikan konsisten huruf kecil semua

    suspend fun getTodo(): List<Todo> {
        return try {
            val data = db.collection(collectionName)
                .get()
                .await()

            if (!data.isEmpty) {
                data.documents.map {
                    Todo(
                        id = it.id,
                        title = it.getString("title").orEmpty(),
                        description = it.getString("description").orEmpty()
                    )
                }
            } else {
                emptyList()
            }
        } catch (exc: Exception) {
            throw Exception("Gagal mengambil data: ${exc.message}")
        }
    }

    suspend fun getTodo(id: String): Todo? {
        val data = db.collection(collectionName)
            .document(id)
            .get()
            .await()

        return if (data.exists()) {
            Todo(
                id = data.id,
                title = data.getString("title").orEmpty(),
                description = data.getString("description").orEmpty()
            )
        } else {
            null
        }
    }

    suspend fun createTodo(todo: Todo): Todo {
        val payload = hashMapOf(
            "title" to todo.title,
            "description" to todo.description
        )

        return try {
            val docRef = db.collection(collectionName)
                .add(payload)
                .await()
            todo.copy(id = docRef.id)
        } catch (exc: Exception) {
            throw Exception("Gagal membuat todo: ${exc.message}")
        }
    }

    suspend fun deleteTodo(id: String) {
        try {
            db.collection(collectionName)
                .document(id)
                .delete()
                .await()
        } catch (exc: Exception) {
            throw Exception("Gagal menghapus todo: ${exc.message}")
        }
    }

    suspend fun updateTodo(todo: Todo) {
        val payload = hashMapOf(
            "title" to todo.title,
            "description" to todo.description
        )

        try {
            db.collection(collectionName)
                .document(todo.id)
                .set(payload)
                .await()
        } catch (exc: Exception) {
            throw Exception("Gagal memperbarui todo: ${exc.message}")
        }
    }
}
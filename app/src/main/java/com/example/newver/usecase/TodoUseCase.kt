package com.example.newver.usecase

import com.example.newver.entity.Todo
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

class TodoUseCase {
    private val db = Firebase.firestore
    private val collectionName = "todos" // Saya sarankan pakai "todos" (jamak) biar standar, tapi "todo" juga boleh asal konsisten sama yang lain.

    // 1. UBAH DI SINI: Tambahkan parameter userId
    suspend fun getTodo(userId: String): List<Todo> {
        return try {
            val data = db.collection(collectionName)
                .whereEqualTo("userId", userId) // <--- FILTER USER ID
                .get()
                .await()

            if (!data.isEmpty) {
                data.documents.map {
                    Todo(
                        id = it.id,
                        title = it.getString("title").orEmpty(),
                        description = it.getString("description").orEmpty(),
                        // Jangan lupa ambil userId juga dari database
                        userId = it.getString("userId").orEmpty()
                    )
                }
            } else {
                emptyList()
            }
        } catch (exc: Exception) {
            throw Exception("Gagal mengambil data: ${exc.message}")
        }
    }

    suspend fun getTodoById(id: String): Todo? {
        val data = db.collection(collectionName)
            .document(id)
            .get()
            .await()

        return if (data.exists()) {
            Todo(
                id = data.id,
                title = data.getString("title").orEmpty(),
                description = data.getString("description").orEmpty(),
                userId = data.getString("userId").orEmpty()
            )
        } else {
            null
        }
    }

    suspend fun createTodo(todo: Todo): Todo {
        // 2. UBAH DI SINI: Masukkan userId ke payload simpan
        val payload = hashMapOf(
            "title" to todo.title,
            "description" to todo.description,
            "userId" to todo.userId, // <--- PENTING: Penanda Pemilik
            "createdTime" to System.currentTimeMillis() // Opsional: biar bisa diurutkan
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
        // 3. UBAH DI SINI: Masukkan userId lagi
        // Kenapa? Karena .set() akan MENIMPA seluruh dokumen.
        // Kalau userId tidak dimasukkan di sini, nanti field userId di database akan hilang.
        val payload = hashMapOf(
            "title" to todo.title,
            "description" to todo.description,
            "userId" to todo.userId, // <--- PENTING
            "createdTime" to todo.createdTime
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
package com.example.newver.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Todo(
    var id: String = "",
    var title: String = "",
    var description: String = "",
    var isFinished: Boolean = false,
    var createdTime: Long = 0,

    // TAMBAHAN BARU:
    var userId: String = ""
) : Parcelable
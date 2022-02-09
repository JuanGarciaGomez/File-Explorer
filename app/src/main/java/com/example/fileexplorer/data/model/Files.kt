package com.example.fileexplorer.data.model

data class Files(
    val name: String,
    val path: String,
    val icon: String,
    var isSelected: Boolean
)
package com.example.fileexplorer.data.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fileexplorer.R
import com.example.fileexplorer.data.model.Files
import com.example.fileexplorer.domain.GetFilesUseCase.Companion.changeState
import java.io.File

class FileAdapter(
    private val fileList: MutableList<Files>,
    private val listener: itemListener,
) : RecyclerView.Adapter<FileAdapter.FileHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileHolder {
        val layoutInflater =
            LayoutInflater.from(parent.context).inflate(R.layout.item_file, parent, false)
        return FileHolder(layoutInflater)
    }

    override fun onBindViewHolder(holder: FileHolder, position: Int) {
        holder.render(fileList[position], position)
        holder.itemView.setOnLongClickListener{ view ->
            val check = view.findViewById<ImageView>(R.id.check_file)
            changeState(check, position)
            listener.active()
            return@setOnLongClickListener true
        }
    }

    override fun getItemCount(): Int = fileList.size

    interface itemListener {
        fun onClick(path: String, check: ImageView, position: Int)
        fun active()
    }

    inner class FileHolder(val view: View) : RecyclerView.ViewHolder(view) {

        var image = view.findViewById<ImageView>(R.id.image_file)
        var name = view.findViewById<TextView>(R.id.name_file)
        var check = view.findViewById<ImageView>(R.id.check_file)

        fun render(file: Files, position: Int) {
            name.text = file.name
            val file1 = File(file.path)
            if (file1.isDirectory)
                image.setImageResource(R.drawable.folder_)
            else
                when (file.icon) {
                    "doc", "docx" -> image.setImageResource(R.drawable.icon_word)
                    "exe" -> image.setImageResource(R.drawable.icon_exe)
                    "png", "jpg" -> image.setImageResource(R.drawable.icon_imagen)
                    "pdf" -> image.setImageResource(R.drawable.icon_pdf)
                    "txt" -> image.setImageResource(R.drawable.icon_txt)
                    "zip" -> image.setImageResource(R.drawable.icon_zip)
                    "rar" -> image.setImageResource(R.drawable.icon_rar)
                    else -> image.setImageResource(R.drawable.icon_error)
                }
            view.setOnClickListener {
                listener.onClick(file.path, check, position)
            }
        }

    }
}
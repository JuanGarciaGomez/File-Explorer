package com.example.fileexplorer.domain

import android.app.Activity
import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.view.isVisible
import com.example.fileexplorer.R
import com.example.fileexplorer.data.model.Files
import java.io.File

class GetFilesUseCase {

    companion object {
        // Preview directory -> PD
        private var previewDirectory = true
        val globalFileList: MutableList<Files> = ArrayList()
        val listFilesPaths: MutableList<String> = ArrayList()

        fun changeState(check: ImageView, position: Int) {
            //val buttonsLinear = act.findViewById<LinearLayout>(R.id.buttons_layout)
            if(check.isVisible){
                check.visibility = View.GONE
                globalFileList[position].isSelected = false
            }else{
                check.visibility = View.VISIBLE
                globalFileList[position].isSelected = true
            }
        }

        fun getFiles(path: String): MutableList<Files> {
            val currentDirectory = File(path)

            currentDirectory.listFiles()?.sortedBy { it.name.lowercase() }?.forEach { file ->
                if(file.isDirectory){
                    globalFileList.add(Files(file.name, file.path, file.extension, false))
                }
            }

            currentDirectory.listFiles()?.sortedBy { it.name.lowercase() }?.forEach { file ->
                if(file.isFile){
                    globalFileList.add(Files(file.name, file.path, file.extension, false))
                }
            }
            return globalFileList
        }

        fun getAllFiles(path: String): MutableList<String> {
            readFiles(File(path), previewDirectory, path)
            return listFilesPaths
        }

        private fun readFiles(parentFile: File, PD: Boolean,  path: String) {
            parentFile.listFiles()?.forEach {
                if (it.isDirectory && PD) readFiles(it!!, PD, path)
                listFilesPaths.add(it.path)
            }
        }
    }

}
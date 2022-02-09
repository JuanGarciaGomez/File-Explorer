package com.example.fileexplorer.ui.view

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.fileexplorer.R
import com.example.fileexplorer.databinding.ActivityReadFilesBinding
import com.example.fileexplorer.ui.viewmodel.FileViewModel

class ReadFilesActivity : AppCompatActivity() {

    private val fileViewModel: FileViewModel by viewModels()
    private lateinit var b: ActivityReadFilesBinding
    var bodyText = ""
    var fileName = ""
    var path = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityReadFilesBinding.inflate(layoutInflater)
        setContentView(b.root)

        bodyText = intent.getStringExtra("file").toString()
        fileName = intent.getStringExtra("fileName").toString()
        path = intent.getStringExtra("path").toString()
        b.fileText.setText(bodyText)
        b.fileText.setSelection(b.fileText.text.length)

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_edit, menu)
        this.title = fileName
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        if (fileViewModel.itemSelectedFiles(item, this, b.fileText.text.toString(), fileName,path)) true
        else super.onOptionsItemSelected(item)
}
package com.example.fileexplorer.ui.view

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.example.fileexplorer.R
import com.example.fileexplorer.data.adapter.FileAdapter
import com.example.fileexplorer.data.extensionsfunctions.env
import com.example.fileexplorer.data.extensionsfunctions.toast
import com.example.fileexplorer.data.model.Files
import com.example.fileexplorer.databinding.ActivityMainBinding
import com.example.fileexplorer.databinding.DialogCrateFileBinding
import com.example.fileexplorer.domain.GetFilesUseCase
import com.example.fileexplorer.domain.GetFilesUseCase.Companion.changeState
import com.example.fileexplorer.domain.GetFilesUseCase.Companion.globalFileList
import com.example.fileexplorer.ui.viewmodel.FileViewModel
import java.io.File

class MainActivity : AppCompatActivity(), FileAdapter.itemListener, SearchView.OnQueryTextListener  {

    private val fileViewModel: FileViewModel by viewModels()
    private lateinit var b: ActivityMainBinding
    private lateinit var bDialog: DialogCrateFileBinding
    private var method = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityMainBinding.inflate(layoutInflater)
        bDialog = DialogCrateFileBinding.inflate(layoutInflater)
        setContentView(bDialog.root)
        setContentView(b.root)

        fileViewModel.onCreate()

        b.swipeContainer.setColorSchemeResources(R.color.teal_700)
        b.swipeContainer.setProgressBackgroundColorSchemeResource(R.color.white)

        b.swipeContainer.setOnRefreshListener(OnRefreshListener {
            // Esto se ejecuta cada vez que se realiza el gesto
            b.swipeContainer.isRefreshing = true
            fileViewModel.fillList(b.rutaActual.text.toString())
            b.swipeContainer.isRefreshing = false

        })

        fileViewModel.path.observe(this, Observer { ruta ->
            b.rutaActual.text = ruta
        })

        fileViewModel.fileModel.observe(this, Observer { fileList ->
            val recycler: RecyclerView = findViewById(R.id.lista_ficheros)
            recycler.layoutManager = GridLayoutManager(this, 4)
            val adapter = FileAdapter(fileList, this)
            recycler.adapter = adapter
        })

        //Lower Buttons menu
        b.renameButton.setOnClickListener {
            fileViewModel.renameFile(this)
            visibilityButtonsBottom()
        }
        b.infoButton.setOnClickListener {
            fileViewModel.infoAlert(this)

        }

        b.copyButton.setOnClickListener{
            fileViewModel.moveOrCopyFile(true,method)
            method = ""
            visibilityButtonsBottom()
            b.confirmation.visibility = VISIBLE
        }

        b.moveButton.setOnClickListener {
            fileViewModel.moveOrCopyFile(true, method)
            method = "move"
            visibilityButtonsBottom()
            b.confirmation.visibility = VISIBLE
        }

        b.acceptButton.setOnClickListener {
            fileViewModel.moveOrCopyFile(true,method)
            b.confirmation.visibility = GONE
        }

        b.cancelButton.setOnClickListener {
            fileViewModel.moveOrCopyFile(false,method)
            b.confirmation.visibility = GONE
        }

        b.deleteButton.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setMessage("¿Está seguro que desea eliminar los archivos?")
                .setPositiveButton("Aceptar",
                    DialogInterface.OnClickListener { dialog, id ->
                        fileViewModel.deleteFiles(this)
                        fileViewModel.searchAllFiles()
                        visibilityButtonsBottom()
                    })
                .setNegativeButton("Cancelar",
                    DialogInterface.OnClickListener { dialog, id ->

                    })
            builder.create().show()
        }

    }

    override fun active() {
        visibilityButtonsBottom()
    }

    fun visibilityButtonsBottom(){
        if (fileViewModel.checkSelected()) {
            b.buttonsLayout.visibility = VISIBLE
        } else {
            b.buttonsLayout.visibility = GONE
        }
    }

    override fun onClick(path: String, check: ImageView, position: Int) {
        val ruta = File(path)
        val strs = path.split("/").toTypedArray()
        if(fileViewModel.checkSelected()){
            changeState(check, position)
        }else{
            if(ruta.isFile){
                if (ruta.extension == "txt" || ruta.extension == "xml"){
                    val intent = Intent(this,ReadFilesActivity::class.java).apply {
                        putExtra("file",ruta.readText())
                        putExtra("fileName",strs[strs.size-1])
                        putExtra("path",b.rutaActual.text.toString())
                    }
                    startActivity(intent)
                }else toast("No se puede abrir el archivo ")

            }else{
                fileViewModel.fillList(path)
            }
        }
        visibilityButtonsBottom()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if ((b.rutaActual.text == "Buscando" ||
                        b.rutaActual.text == "Archivo no encontrado")
            ) {
                fileViewModel.fillList(env())
                return true
            } else {
                if (fileViewModel.onBack(keyCode, this)) {
                    visibilityButtonsBottom()
                    return true
                }
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_search, menu)

        val searchItem = menu?.findItem(R.id.action_search)
        val searchView = searchItem?.actionView as SearchView
        searchView.setOnQueryTextListener(this)

        return super.onCreateOptionsMenu(menu)
    }


    override fun onQueryTextSubmit(query: String?): Boolean {
        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        if (newText != null) {
            if (newText.length >= 3) {
                globalFileList.clear()
                GetFilesUseCase.listFilesPaths.forEach { firstFile ->
                     val flag = firstFile.split("/").toTypedArray()
                    if (flag[flag.size - 1].lowercase().contains(newText.lowercase())) {
                        val file = File(firstFile)
                        if (!file.isHidden){
                            globalFileList.add(Files(file.name, file.path, file.extension, false))
                        }
                    }
                }
                fileViewModel.showPath("Buscando")
                if (globalFileList.isNotEmpty())  fileViewModel.fillSearchList()
                else {
                    fileViewModel.fillList("")
                    fileViewModel.showPath("Archivo no encontrado")
                }
            }
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        if (fileViewModel.itemSelected(item, this, b)) true
        else super.onOptionsItemSelected(item)

}
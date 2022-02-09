package com.example.fileexplorer.ui.viewmodel

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Environment
import android.renderscript.ScriptGroup
import android.view.KeyEvent
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.fileexplorer.R
import com.example.fileexplorer.data.extensionsfunctions.env
import com.example.fileexplorer.data.extensionsfunctions.toast
import com.example.fileexplorer.data.model.Files
import com.example.fileexplorer.data.utils.Callback
import com.example.fileexplorer.databinding.ActivityMainBinding
import com.example.fileexplorer.domain.GetFilesUseCase
import com.example.fileexplorer.domain.GetFilesUseCase.Companion.globalFileList
import com.example.fileexplorer.domain.GetFilesUseCase.Companion.listFilesPaths
import com.example.fileexplorer.ui.view.CreateAlertDialogView.Companion.dialogCreateFile
import com.example.fileexplorer.ui.view.CreateAlertDialogView.Companion.dialogText
import com.example.fileexplorer.ui.view.CreateAlertDialogView.Companion.infoAlertDialog
import com.example.fileexplorer.ui.view.CreateAlertDialogView.Companion.loginDialog
import com.example.fileexplorer.ui.view.MainActivity
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class FileViewModel : ViewModel(){

    val fileModel = MutableLiveData<MutableList<Files>>()
    var allFiles: MutableList<String> = ArrayList()

    val path = MutableLiveData<String>()
    private var initTime = 0.0
    private var direction = ""
    val filesInMemory: MutableList<File> = ArrayList()

    private val mainPath: String = Environment.getExternalStorageDirectory().path

    fun onCreate(){
        showPath(mainPath)
        fillList(mainPath)
        searchAllFiles()
    }

    fun showPath(currentPath: String){
        path.postValue(currentPath)
    }

    fun searchAllFiles() {
        listFilesPaths.clear()
        allFiles = GetFilesUseCase.getAllFiles(mainPath)
    }
    fun fillList(path: String){
        globalFileList.clear()
        val fileList = GetFilesUseCase.getFiles(path)
        direction = path
        fileModel.postValue(fileList)
        showPath(path)
    }
    fun fillSearchList(){
        val newList = globalFileList.sortedBy { File(it.path).isFile }
        fileModel.postValue(newList as MutableList<Files>?)
    }

    fun checkSelected(): Boolean{
        globalFileList.forEach { file ->
            if(file.isSelected){
                return true
            }
        }
        return false
    }

    fun deleteFiles(act: Activity){
        var selected = false
        globalFileList.forEach { file ->
            if(file.isSelected) {
                File(file.path).deleteRecursively()
                selected = true
            }
        }
        if (!selected) act.toast("Seleccione una carpeta/archivo")
        fillList(direction)
    }

    fun moveOrCopyFile(flag: Boolean, method: String){
        if(!flag){
            filesInMemory.clear()
            return
        }
        if(filesInMemory.isEmpty()){
            globalFileList.forEach { file ->
                if(file.isSelected){
                    filesInMemory.add(File(file.path))
                    file.isSelected = false
                }
            }
            fillList(direction)
            return
        }else if(method == "move"){
            filesInMemory.forEach { file ->
                val newFile = File("$direction/${file.name}")
                file.renameTo(newFile)
                fillList(direction)
            }
            filesInMemory.clear()
            return
        } else {
            filesInMemory.forEach { file ->
                var count = 0
                while (true){
                    try{
                        if(file.isFile && count == 0){
                            val name = file.name.replace(".${file.extension}", "")
                            file.copyRecursively(File("$direction/$name.${file.extension}"), false)
                        }else if(file.isFile){
                            val name = file.name.replace(".${file.extension}", "")
                            file.copyRecursively(File("$direction/$name($count).${file.extension}"), false)
                        }else if(file.isDirectory && count == 0){
                            file.copyRecursively(File("$direction/${file.name}"), false)
                        }else{
                            file.copyRecursively(File("$direction/${file.name}($count)"), false)
                        }
                        break
                    }catch (e: Exception){
                        count++
                    }
                }
                fillList(direction)
            }
            filesInMemory.clear()
            return
        }
    }

    fun renameFile(act: Activity){
        var count = 0
        var newFile: File? = null
        var extension = ""
        var isFile = false
        globalFileList.forEach { file ->
            if(file.isSelected){
                count++
                newFile = File(file.path)
                extension = newFile!!.extension
                isFile = newFile!!.isFile
            }
        }
        if(count == 1){
            dialogText(act, extension, isFile, object: Callback{
                override fun callBackString(text: String) {
                    if(text == "Ya existe un archivo o carpeta con ese nombre" || text == "Por favor digite un nombre"){
                        act.toast(text)
                    }else{
                        if(newFile?.isFile == true){
                            newFile?.renameTo(File(newFile!!.parent + "/$text." + newFile!!.extension))
                        }else{
                            newFile?.renameTo(File(newFile!!.parent + "/$text"))
                        }
                        fillList(direction)
                    }
                }
            })
        }
    }

    @Throws(Exception::class)
    fun zipFolder(srcFolder: String?, destZipFile: String?) {
        var zip: ZipOutputStream? = null
        var fileWriter: FileOutputStream? = null
        fileWriter = FileOutputStream(destZipFile)
        zip = ZipOutputStream(fileWriter)
        if (srcFolder != null) {
            addFolderToZip("", srcFolder, zip)
        }
        zip.flush()
        zip.close()
    }
    @Throws(java.lang.Exception::class)
    private fun addFileToZip(path: String, srcFile: String, zip: ZipOutputStream) {
        val folder = File(srcFile)
        if (folder.isDirectory) {
            addFolderToZip(path, srcFile, zip)
        } else {
            val buf = ByteArray(1024)
            var len: Int
            val `in` = FileInputStream(srcFile)
            zip.putNextEntry(ZipEntry(path + "/" + folder.name))
            while (`in`.read(buf).also { len = it } > 0) {
                zip.write(buf, 0, len)
            }
        }
    }
    @Throws(java.lang.Exception::class)
    private fun addFolderToZip(path: String, srcFolder: String, zip: ZipOutputStream) {
        val folder = File(srcFolder)
        for (fileName in folder.list()) {
            if (path == "") {
                addFileToZip(folder.name, "$srcFolder/$fileName", zip)
            } else {
                addFileToZip(path + "/" + folder.name, "$srcFolder/$fileName", zip)
            }
        }
    }

   /* @Throws(IOException::class)
    fun zip(path: File) {
        val files = path.listFiles()
        val fos = FileOutputStream(direction)
        val zipOut = ZipOutputStream(fos)
        for (zipThis in files) {
            val fis = FileInputStream(zipThis)
            val zipEntry = ZipEntry(zipThis.name)
            zipOut.putNextEntry(zipEntry)
            val bytes = ByteArray(2048)
            var length: Int
            while (fis.read(bytes).also { length = it } >= 0) {
                zipOut.write(bytes, 0, length)
            }
            fis.close()
        }
        zipOut.close()
        fos.close()
    }*/
    fun login(act: Activity){
       loginDialog(act)
    }

    fun compress(mainPath: String, act: Activity) {
        var selected = false
        globalFileList.forEach { file ->
            if(file.isSelected){
                var out: String = "$direction/${file.name}.zip"
                zipFolder(direction,out)
                //zip(File("filestozip"))
                selected = true
            }
        }
        if (!selected) act.toast("Seleccione una carpeta/archivo")
        fillList(direction)
    }

    fun onBack(keyCode: Int, act: Activity): Boolean {
        if(checkSelected()){
            globalFileList.forEach { file ->
                if(file.isSelected){
                    file.isSelected = false
                }
            }
            fillList(direction)
        } else if (keyCode == KeyEvent.KEYCODE_BACK && act.env() == direction) {
            if (System.currentTimeMillis() - initTime > 3000) {
                act.toast("Nuevamente para salir")
            } else {
                act.finish()
            }
            initTime = System.currentTimeMillis().toDouble()
        } else {
            if ( keyCode == KeyEvent.KEYCODE_DEL) return true
            else {
                val currentDirectory: String? = File(direction).parent
                currentDirectory?.let { fillList(it) }
            }
        }
        return true
    }

    fun itemSelected(item: MenuItem, act: Activity, b: ActivityMainBinding) = when (item.itemId) {
        R.id.action_search -> {
            true
        }
        R.id.add_folder -> {
            // User chose the "Settings" item, show the app settings UI...
            alertNewFile(act, direction)
            true
        }
        R.id.comprimir -> {
            compress(mainPath,act)
            true
        }
        R.id.unPack -> {
            act.toast("Descomprimir")
            true
        }

        R.id.info -> {
            infoAlert(act)
            true
        }
        R.id.rename -> {
            renameFile(act)
            true
        }
        R.id.copy -> {
            var method = ""
            moveOrCopyFile(true,method)
            method = ""
            b.buttonsLayout.visibility = GONE
            b.confirmation.visibility = VISIBLE
            b.acceptButton.setOnClickListener {
                moveOrCopyFile(true,method)
                b.confirmation.visibility = GONE
            }
            b.cancelButton.setOnClickListener {
                moveOrCopyFile(false,method)
                b.confirmation.visibility = GONE
            }
            true
        }
        R.id.move -> {
            var method = "move"
            moveOrCopyFile(true,method)
            method = "move"
            b.buttonsLayout.visibility = GONE
            b.confirmation.visibility = VISIBLE
            b.acceptButton.setOnClickListener {
                moveOrCopyFile(true,method)
                b.confirmation.visibility = GONE
            }
            b.cancelButton.setOnClickListener {
                moveOrCopyFile(false,method)
                b.confirmation.visibility = GONE
            }
            true
        }

        R.id.delete ->{
            val builder = AlertDialog.Builder(act)
            builder.setMessage("¿Está seguro que desea eliminar los archivos?")
                .setPositiveButton("Aceptar",
                    DialogInterface.OnClickListener { dialog, id ->
                        deleteFiles(act)
                        searchAllFiles()
                        b.buttonsLayout.visibility = GONE
                    })
                .setNegativeButton("Cancelar",
                    DialogInterface.OnClickListener { dialog, id ->

                    })
            builder.create().show()
            true
        }

        R.id.login -> {
            login(act)
            true
        }
        else -> false
    }

    fun itemSelectedFiles(item: MenuItem, act: Activity, bodyText: String, fileName: String,path: String) = when (item.itemId) {
        R.id.action_save -> {
            val dir = File(path, fileName)
            val fileOutputStream = FileOutputStream(dir)
            fileOutputStream.bufferedWriter().use { it.write(bodyText) }
            act.toast("Archivo guardado con exito")
            act.finish()
            true
        }
        else -> false
    }

    fun fileExist(path: String, name: String): Boolean {
        val currentDirectory = File(path)
        //Log.e(null, path)
        currentDirectory.listFiles()?.forEach { file ->
            if (file.name == name) {
                return true
            }
        }
        return false
    }

    fun alertNewFile(activity: Activity, path: String) {
        dialogCreateFile(activity, path, object: Callback{
            override fun callBackString(text: String) {
                if(text == "Por favor digite un nombre"){
                    activity.toast(text)
                }else if(fileExist(path, text)){
                    activity.toast(text)
                }else if(!fileExist(path, text)){
                    activity.toast(text)
                    fillList(path)
                    searchAllFiles()
                }else{
                    activity.toast(text)
                    fillList(path)
                    searchAllFiles()
                }
            }
        })
    }

    fun infoAlert(act: Activity) {
        infoAlertDialog(act)
    }

}
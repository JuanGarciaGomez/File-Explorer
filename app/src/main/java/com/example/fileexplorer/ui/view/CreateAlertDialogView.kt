package com.example.fileexplorer.ui.view

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.fileexplorer.R
import com.example.fileexplorer.data.utils.Callback
import com.example.fileexplorer.domain.GetFilesUseCase.Companion.globalFileList
import java.io.File
import java.io.FileOutputStream
import java.text.DecimalFormat
import java.util.*


class CreateAlertDialogView : AppCompatActivity() {

    companion object {

        fun dialogCreateFile(activity: Activity, path: String, callback: Callback) {

            val view = activity.layoutInflater.inflate(R.layout.dialog_crate_file, null)

            val ok = view.findViewById<TextView>(R.id.txt_ok)
            val cancel = view.findViewById<TextView>(R.id.txt_cancel)
            val name = view.findViewById<EditText>(R.id.item_name)

            val builder = AlertDialog.Builder(activity)
            builder.setView(view)
            val dialog = builder.create()
            dialog.show()


            ok.setOnClickListener {
                val nameString = name.text.toString()
                val folder = view.findViewById<RadioButton>(R.id.dialog_radio_folder)
                val file = view.findViewById<RadioButton>(R.id.dialog_radio_file)

                val dir = File(path, nameString)

                if (nameString.isNotEmpty() && folder.isChecked) {
                    if (dir.mkdirs()) {
                        callback.callBackString("Carpeta creada con exito")
                        dialog.dismiss()
                    } else {
                        callback.callBackString("Ya existe una carpeta con el mismo nombre")
                    }
                } else if (nameString.isNotEmpty() && file.isChecked) {
                    var existFileok = false

                    globalFileList.forEach { list ->
                        if (list.name == nameString) existFileok = true
                    }
                    Log.e("existFileOk", existFileok.toString())

                    if (existFileok) {
                        val view = activity.layoutInflater.inflate(R.layout.dialog_text, null)

                        val okDialog2 = view.findViewById<TextView>(R.id.txt_ok)
                        val cancelDialog2 = view.findViewById<TextView>(R.id.txt_cancel)
                        val textViewDialog2 = view.findViewById<TextView>(R.id.text_dialog)
                        val iteNameDialog2 = view.findViewById<EditText>(R.id.item_name2)

                        textViewDialog2.setText("El archivo ya existe, desea reescribirlo?")
                        iteNameDialog2.setText(nameString)
                        val builder = AlertDialog.Builder(activity)
                        builder.setView(view)
                        val dialog2 = builder.create()
                        dialog2.show()

                        okDialog2.setOnClickListener {
                            FileOutputStream(dir)
                            callback.callBackString("Archivo creado con exito")
                            dialog2.dismiss()
                            dialog.dismiss()
                        }
                        cancelDialog2.setOnClickListener {
                            dialog2.dismiss()
                            dialog.dismiss()
                        }

                    }//callback.callBackString("Ya existe un archivo con el mismo nombre")
                    else {
                        FileOutputStream(dir)
                        callback.callBackString("Archivo creado con exito")
                        dialog.dismiss()
                    }

                } else {
                    callback.callBackString("Por favor digite un nombre")
                }
            }

            cancel.setOnClickListener {
                dialog.dismiss()
            }
        }

        fun loginDialog(activity: Activity) {

            val view = activity.layoutInflater.inflate(R.layout.dialog_login, null)

            val entrar = view.findViewById<Button>(R.id.login_btn)

            val builder = AlertDialog.Builder(activity)
            builder.setView(view)
            val dialog = builder.create()
            dialog.show()


            entrar.setOnClickListener {

            }

        }

        fun dialogText(activity: Activity, extension: String, isFile: Boolean, callback: Callback) {

            val view = activity.layoutInflater.inflate(R.layout.dialog_text, null)

            val ok = view.findViewById<TextView>(R.id.txt_ok)
            val cancel = view.findViewById<TextView>(R.id.txt_cancel)

            val builder = AlertDialog.Builder(activity)
            builder.setView(view)
            val dialog = builder.create()
            dialog.show()

            ok.setOnClickListener {
                val name = view.findViewById<EditText>(R.id.item_name2)
                val nameString = name.text.toString()

                if (!nameString.isEmpty()) {
                    var exist = false
                    globalFileList.forEach { file ->
                        if (isFile) {
                            if (file.name == "$nameString.$extension" && !file.isSelected) exist =
                                true
                        } else {
                            if (file.name == nameString && !file.isSelected) exist = true
                        }
                    }
                    if (exist) {
                        callback.callBackString("Ya existe un archivo o carpeta con ese nombre")
                    } else {
                        callback.callBackString(nameString)
                        dialog.dismiss()
                    }
                } else {
                    callback.callBackString("Por favor digite un nombre")
                }
            }
            cancel.setOnClickListener {
                dialog.dismiss()
            }
        }

        @SuppressLint("SetTextI18n")
        fun infoAlertDialog(activity: Activity) {

            val view = activity.layoutInflater.inflate(R.layout.dialog_info, null)

            val acept = view.findViewById<TextView>(R.id.txt_aceptar)
            val name = view.findViewById<TextView>(R.id.name_resul_txt)
            val ubicacion = view.findViewById<TextView>(R.id.ubication_resul_txt)
            val tamaño = view.findViewById<TextView>(R.id.tamaño_resul_txt)
            val modificacion = view.findViewById<TextView>(R.id.modificacion_resul_txt)

            globalFileList.forEach { file ->
                if (file.isSelected) {
                    val file1 = File(file.path)
                    val folderSize = getFolderSize(file1)

                    name.text = file.name
                    ubicacion.text = file.path
                    tamaño.text ="${longToStorageUnit(folderSize)} ($folderSize Bytes)"
                    modificacion.text = Date(file1.lastModified()).toString()
                }
            }

            val builder = AlertDialog.Builder(activity)
            builder.setView(view)
            val dialog = builder.create()
            dialog.show()

            acept.setOnClickListener {
                dialog.dismiss()
            }
        }

        private fun getFolderSize(file: File): Long {
            var size = file.length()
            if (file.isDirectory) file.listFiles()?.forEach {
                size += getFolderSize(it)
            }
            return size
        }

        private fun floatForm(d: Double): String {
            return DecimalFormat("#.##").format(d)
        }

        private fun longToStorageUnit(size: Long): String {
            val kb: Long = 1024
            val mb = kb * 1024
            val gb = mb * 1024
            val tb = gb * 1024
            val pb = tb * 1024
            val eb = pb * 1024
            return when {
                size < kb -> floatForm(size.toDouble()) + " B"
                size < mb -> floatForm(size.toDouble() / kb) + " KB"
                size < gb -> floatForm(size.toDouble() / mb) + " MB"
                size < tb -> floatForm(size.toDouble() / gb) + " GB"
                size < pb -> floatForm(size.toDouble() / tb) + " TB"
                size < eb -> floatForm(size.toDouble() / pb) + " PB"
                else -> floatForm(size.toDouble() / eb) + " EB"
            }
        }
    }
}
package com.example.fileexplorer.data.utils

import android.content.Context
import android.os.AsyncTask
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

class UnpackFile(
    context: Context?,
    zipFile: String?,
    location: String?,
    b: Boolean,
    param: FileCallback
) : AsyncTask<String, Integer, Boolean>() {
    //private ProgressDialog mProgresoDescompresion;
    private var _Ubicacion_ZIP: String? = null
    private var _Destino_Descompresion: String? = null
    private var _Mantener_ZIP = false
    private var callback: FileCallback? = null

    /**
     * Descomprime un archivo .ZIP
     * @param ctx Contexto de la Aplicación Android
     * @param Ubicacion Ruta ABSOLUTA de un archivo .zip
     * @param Destino Ruta ABSOLUTA del destino de la descompresión. Finalizar con /
     * @param Mantener Indica si se debe mantener el archivo ZIP despues de descomprimir
     */
    fun UnpackFile(
        ctx: Context?,
        Ubicacion: String?,
        Destino: String?,
        Mantener: Boolean,
        callback: FileCallback?
    ) {
    }

    override fun onPreExecute() {
        super.onPreExecute()
        //mProgresoDescompresion.show();
    }

    override fun doInBackground(vararg params: String?): Boolean? {
        var size: Int
        val buffer = ByteArray(2048)
        File(_Destino_Descompresion).mkdirs() //Crea la ruta de descompresion si no existe
        try {
            try {
                val lector_archivo = FileInputStream(_Ubicacion_ZIP)
                val lector_zip = ZipInputStream(lector_archivo)
                var item_zip: ZipEntry? = null
                while (lector_zip.nextEntry.also { item_zip = it } != null) {
                    if (item_zip!!.isDirectory) { //Si el elemento es un directorio, crearlo
                        Crea_Carpetas(item_zip!!.name, _Destino_Descompresion)
                    } else {
                        val outStream = FileOutputStream(_Destino_Descompresion + item_zip!!.name)
                        val bufferOut = BufferedOutputStream(outStream, buffer.size)
                        while (lector_zip.read(buffer, 0, buffer.size).also { size = it } != -1) {
                            bufferOut.write(buffer, 0, size)
                        }
                        bufferOut.flush()
                        bufferOut.close()
                    }
                }
                lector_zip.close()
                lector_archivo.close()

                //Conservar archvi .zip
                if (!_Mantener_ZIP) File(_Ubicacion_ZIP).delete()

                //Espera para poder realizar las validaciones por cada ciclo
                Thread.sleep(5000)
                return true
            } catch (e: Exception) {
            }

            //mProgresoDescompresion.dismiss();
        } catch (e: Exception) {
        }
        return false
    }

    /*protected fun onProgressUpdate(vararg progress: Int?) {
        super.onProgressUpdate(*progress)
        //mProgresoDescompresion.setProgress(progress[0]);
    }*/

    override fun onPostExecute(aBoolean: Boolean) {
        super.onPostExecute(aBoolean)

        //callback
        callback!!.RspUnpack(aBoolean)
    }

    /**
     * Crea la carpeta donde seran almacenados los archivos del .zip
     * @param dir
     * @param location
     */
    private fun Crea_Carpetas(dir: String, location: String?) {
        val f = File(location + dir)
        if (!f.isDirectory) {
            f.mkdirs()
        }
    }

    /**
     *
     */
    interface FileCallback {
        fun RspUnpack(OK_unpack: Boolean): Boolean
    }

}
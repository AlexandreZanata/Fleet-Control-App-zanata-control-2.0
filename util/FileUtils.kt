package com.alexandre.controledefrota.util

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object FileUtils {

    fun obterNomeArquivo(context: Context, uri: Uri): String {
        var resultado: String? = null
        if (uri.scheme == "content") {
            val cursor: Cursor? = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val indexNome = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (indexNome != -1) {
                        resultado = it.getString(indexNome)
                    }
                }
            }
        }
        if (resultado == null) {
            resultado = uri.path
            val corte = resultado?.lastIndexOf('/')
            if (corte != -1 && corte != null) {
                resultado = resultado?.substring(corte + 1)
            }
        }
        return resultado ?: "arquivo_${System.currentTimeMillis()}"
    }

    fun obterExtensao(nomeArquivo: String): String {
        val ponto = nomeArquivo.lastIndexOf(".")
        return if (ponto > 0 && ponto < nomeArquivo.length - 1) {
            nomeArquivo.substring(ponto + 1).lowercase()
        } else {
            "desconhecido"
        }
    }

    fun copiarArquivoParaStorage(context: Context, uri: Uri, extensao: String): File {
        val novoArquivo = File(
            context.getExternalFilesDir(null),
            "anexo_${System.currentTimeMillis()}.$extensao"
        )

        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(novoArquivo).use { output ->
                input.copyTo(output)
            }
        }

        return novoArquivo
    }
}
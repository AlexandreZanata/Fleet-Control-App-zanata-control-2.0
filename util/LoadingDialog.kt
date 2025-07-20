package com.alexandre.controledefrota.util

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.Window
import android.widget.TextView
import com.alexandre.controledefrota.R

class LoadingDialog(private val context: Context) {
    private val dialog: Dialog = Dialog(context)
    private var txtMensagem: TextView? = null

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_loading, null)
        txtMensagem = view.findViewById(R.id.txtMensagem)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(view)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    // Métodos originais
    fun show() {
        if (!dialog.isShowing) {
            dialog.show()
        }
    }

    fun dismiss() {
        if (dialog.isShowing) {
            dialog.dismiss()
        }
    }

    // Métodos com nomes em português (para compatibilidade)
    fun mostrar(mensagem: String = "Carregando...") {
        txtMensagem?.text = mensagem
        show()
    }

    fun esconder() {
        dismiss()
    }
}
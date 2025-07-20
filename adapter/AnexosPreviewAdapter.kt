package com.alexandre.controledefrota.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.alexandre.controledefrota.R
import com.alexandre.controledefrota.databinding.ItemAnexoPreviewBinding
import com.alexandre.controledefrota.model.ServicoAnexo
import com.bumptech.glide.Glide
import java.io.File

class AnexosPreviewAdapter(
    private val onAnexoClick: (ServicoAnexo) -> Unit
) : ListAdapter<ServicoAnexo, AnexosPreviewAdapter.AnexoViewHolder>(AnexoDiffCallback()) {

    inner class AnexoViewHolder(private val binding: ItemAnexoPreviewBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(anexo: ServicoAnexo) {
            binding.txtNomeAnexo.text = anexo.nomeArquivo

            // Definir ícone com base no tipo de arquivo
            val isImage = anexo.tipoArquivo == "IMAGEM" ||
                    anexo.tipoArquivo == "JPG" ||
                    anexo.tipoArquivo == "PNG" ||
                    anexo.tipoArquivo == "JPEG"

            if (isImage) {
                Glide.with(binding.imgAnexo.context)
                    .load(File(anexo.caminhoArquivo))
                    .placeholder(R.drawable.ic_image_placeholder)
                    .error(R.drawable.ic_broken_image)
                    .centerCrop()
                    .into(binding.imgAnexo)
            } else {
                // Configurar ícone baseado no tipo de arquivo
                val resId = when (anexo.tipoArquivo.uppercase()) {
                    "PDF" -> R.drawable.ic_pdf
                    "DOC", "DOCX" -> R.drawable.ic_doc
                    "XLS", "XLSX" -> R.drawable.ic_xls
                    "TXT" -> R.drawable.ic_txt
                    else -> R.drawable.ic_document
                }
                binding.imgAnexo.setImageResource(resId)
            }

            binding.root.setOnClickListener {
                onAnexoClick(anexo)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnexoViewHolder {
        val binding = ItemAnexoPreviewBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AnexoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AnexoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class AnexoDiffCallback : DiffUtil.ItemCallback<ServicoAnexo>() {
        override fun areItemsTheSame(oldItem: ServicoAnexo, newItem: ServicoAnexo): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ServicoAnexo, newItem: ServicoAnexo): Boolean {
            return oldItem == newItem
        }
    }
}
package com.alexandre.controledefrota.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.alexandre.controledefrota.R
import com.alexandre.controledefrota.databinding.ItemAnexoBinding
import com.alexandre.controledefrota.model.ServicoAnexo

class AnexosAdapter(private val onRemoveClick: (ServicoAnexo) -> Unit) :
    ListAdapter<ServicoAnexo, AnexosAdapter.AnexoViewHolder>(AnexoDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnexoViewHolder {
        val binding = ItemAnexoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AnexoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AnexoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class AnexoViewHolder(private val binding: ItemAnexoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(anexo: ServicoAnexo) {
            binding.txtNomeAnexo.text = anexo.nomeArquivo
            binding.txtTipoAnexo.text = anexo.tipoArquivo

            // Definir ícone com base no tipo de anexo
            val icone = when {
                anexo.tipoArquivo == "IMAGEM" -> R.drawable.ic_image
                anexo.tipoArquivo == "PDF" -> R.drawable.ic_pdf
                else -> R.drawable.ic_document
            }

            try {
                binding.imgTipoAnexo.setImageResource(icone)
            } catch (e: Exception) {
                // Fallback para um ícone padrão se não encontrar o recurso
                binding.imgTipoAnexo.setImageResource(R.drawable.ic_document)
            }

            binding.btnRemover.setOnClickListener {
                onRemoveClick(anexo)
            }
        }
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
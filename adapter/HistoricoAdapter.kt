package com.alexandre.controledefrota.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.alexandre.controledefrota.R
import com.alexandre.controledefrota.databinding.ItemHistoricoBinding
import com.alexandre.controledefrota.model.Historico
import com.alexandre.controledefrota.model.ServicoAnexo
import com.alexandre.controledefrota.viewmodel.ServicoAnexoViewModel
import com.bumptech.glide.Glide
import java.io.File
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class HistoricoAdapter(
    private val onItemClick: (Historico) -> Unit,
    private val anexoViewModel: ServicoAnexoViewModel
) : ListAdapter<Historico, HistoricoAdapter.HistoricoViewHolder>(HistoricoDiffCallback()) {

    private val anexosCache = mutableMapOf<Long, List<ServicoAnexo>>()
    private val expandedItems = mutableSetOf<Long>()

    inner class HistoricoViewHolder(private val binding: ItemHistoricoBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(historico: Historico) {
            binding.txtTipoServico.text = historico.tipoServico

            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            binding.txtData.text = "Data: ${dateFormat.format(historico.data)}"

            binding.txtQuilometragem.text = "Km: ${historico.quilometragem}"

            val numberFormat = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
            binding.txtCusto.text = "Custo: ${numberFormat.format(historico.custoServico)}"

            binding.txtComentario.text = if (historico.comentario.isNullOrBlank())
                "Sem comentário"
            else
                "Comentário: ${historico.comentario}"

            // Carregar anexos
            loadAnexos(historico)

            itemView.setOnClickListener {
                onItemClick(historico)
            }

            binding.btnVerAnexos.setOnClickListener {
                if (expandedItems.contains(historico.id)) {
                    expandedItems.remove(historico.id)
                    binding.recyclerViewAnexos.visibility = View.GONE
                    binding.btnVerAnexos.text = "Ver anexos"
                } else {
                    expandedItems.add(historico.id)
                    binding.recyclerViewAnexos.visibility = View.VISIBLE
                    binding.btnVerAnexos.text = "Esconder anexos"
                }
            }
        }

        private fun loadAnexos(historico: Historico) {
            // Verificar se já temos os anexos em cache
            if (anexosCache.containsKey(historico.id)) {
                updateAnexosUI(historico.id, anexosCache[historico.id]!!)
            } else {
                // Carregar anexos do banco de dados
                anexoViewModel.obterAnexosPorServico(historico.id).observeForever { anexos ->
                    anexosCache[historico.id] = anexos
                    updateAnexosUI(historico.id, anexos)
                }
            }
        }

        private fun updateAnexosUI(historicoId: Long, anexos: List<ServicoAnexo>) {
            // Configurar visibilidade dos elementos relacionados aos anexos
            if (anexos.isNotEmpty()) {
                binding.layoutAnexosPreview.visibility = View.VISIBLE
                binding.btnVerAnexos.visibility = View.VISIBLE

                // Mostrar a primeira imagem como preview
                val primeiraImagem = anexos.firstOrNull { it.tipoArquivo == "IMAGEM" || it.tipoArquivo == "JPG" || it.tipoArquivo == "PNG" }

                if (primeiraImagem != null) {
                    Glide.with(binding.imgAnexoPreview.context)
                        .load(File(primeiraImagem.caminhoArquivo))
                        .placeholder(R.drawable.ic_image_placeholder)
                        .error(R.drawable.ic_broken_image)
                        .centerCrop()
                        .into(binding.imgAnexoPreview)
                } else {
                    // Se não houver imagens, mostrar ícone para documentos
                    binding.imgAnexoPreview.setImageResource(R.drawable.ic_document)
                    binding.imgAnexoPreview.scaleType = ImageView.ScaleType.CENTER_INSIDE
                }

                // Mostrar quantidade de anexos
                if (anexos.size > 1) {
                    binding.txtQtdAnexos.visibility = View.VISIBLE
                    binding.txtQtdAnexos.text = "+${anexos.size - 1}"
                } else {
                    binding.txtQtdAnexos.visibility = View.GONE
                }

                // Configurar o recycler view dos anexos expandidos
                val anexosAdapter = AnexosPreviewAdapter { anexo ->
                    openAnexo(anexo, binding.root.context)
                }
                binding.recyclerViewAnexos.apply {
                    layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                    adapter = anexosAdapter
                }
                anexosAdapter.submitList(anexos)

                // Mostrar o recycler expandido se este item estiver expandido
                binding.recyclerViewAnexos.visibility = if (expandedItems.contains(historicoId)) View.VISIBLE else View.GONE
                binding.btnVerAnexos.text = if (expandedItems.contains(historicoId)) "Esconder anexos" else "Ver anexos"

            } else {
                binding.layoutAnexosPreview.visibility = View.GONE
                binding.btnVerAnexos.visibility = View.GONE
                binding.recyclerViewAnexos.visibility = View.GONE
            }
        }
    }

    private fun openAnexo(anexo: ServicoAnexo, context: Context) {
        val file = File(anexo.caminhoArquivo)
        if (!file.exists()) {
            Toast.makeText(context, "Arquivo não encontrado", Toast.LENGTH_SHORT).show()
            return
        }

        val uri = FileProvider.getUriForFile(
            context,
            context.applicationContext.packageName + ".provider",
            file
        )

        val intent = Intent()
        val mimeType = when (anexo.tipoArquivo.uppercase()) {
            "IMAGEM", "JPG", "JPEG", "PNG" -> "image/*"
            "PDF" -> "application/pdf"
            "DOC", "DOCX" -> "application/msword"
            "XLS", "XLSX" -> "application/vnd.ms-excel"
            "TXT" -> "text/plain"
            else -> "*/*"
        }

        intent.action = Intent.ACTION_VIEW
        intent.setDataAndType(uri, mimeType)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        val chooser = Intent.createChooser(intent, "Abrir com...")
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(chooser)
        } else {
            Toast.makeText(context, "Nenhum aplicativo encontrado para abrir este arquivo", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoricoViewHolder {
        val binding = ItemHistoricoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return HistoricoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoricoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class HistoricoDiffCallback : DiffUtil.ItemCallback<Historico>() {
        override fun areItemsTheSame(oldItem: Historico, newItem: Historico): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Historico, newItem: Historico): Boolean {
            return oldItem == newItem
        }
    }
}
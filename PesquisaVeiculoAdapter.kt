package com.alexandre.controledefrota.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.alexandre.controledefrota.R
// Adicione este import no início do arquivo:
import com.alexandre.controledefrota.model.Veiculo

class PesquisaVeiculoAdapter(
    private val onItemClick: (Veiculo) -> Unit
) : RecyclerView.Adapter<PesquisaVeiculoAdapter.PesquisaViewHolder>() {

    private var veiculos: List<Veiculo> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PesquisaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_resultado_pesquisa, parent, false)
        return PesquisaViewHolder(view)
    }

    override fun onBindViewHolder(holder: PesquisaViewHolder, position: Int) {
        holder.bind(veiculos[position])
    }

    override fun getItemCount(): Int = veiculos.size

    fun atualizarLista(lista: List<Veiculo>) {
        this.veiculos = lista
        notifyDataSetChanged()
    }

    inner class PesquisaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtPlaca: TextView = itemView.findViewById(R.id.txtPlacaPesquisa)
        private val txtDescricao: TextView = itemView.findViewById(R.id.txtDescricaoPesquisa)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(veiculos[position])
                }
            }
        }

        fun bind(veiculo: Veiculo) {
            txtPlaca.text = veiculo.placa
            txtDescricao.text = "${veiculo.marca} ${veiculo.modelo} - ${veiculo.motorista}"
        }
    }
}
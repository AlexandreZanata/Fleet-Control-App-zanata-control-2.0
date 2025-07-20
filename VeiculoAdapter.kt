package com.alexandre.controledefrota.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.alexandre.controledefrota.R
// Adicione este import no início do arquivo:
import com.alexandre.controledefrota.model.Veiculo

class VeiculoAdapter(private val onItemClick: (Veiculo) -> Unit) :
    ListAdapter<Veiculo, VeiculoAdapter.VeiculoViewHolder>(VeiculoDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VeiculoViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_veiculo, parent, false)
        return VeiculoViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: VeiculoViewHolder, position: Int) {
        val veiculo = getItem(position)
        holder.bind(veiculo)
    }

    inner class VeiculoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtPlaca: TextView = itemView.findViewById(R.id.txtPlaca)
        private val txtVeiculo: TextView = itemView.findViewById(R.id.txtVeiculo)
        private val txtMarcaModelo: TextView = itemView.findViewById(R.id.txtMarcaModelo)
        private val txtMotorista: TextView = itemView.findViewById(R.id.txtMotorista)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position))
                }
            }
        }

        fun bind(veiculo: Veiculo) {
            txtPlaca.text = veiculo.placa
            txtVeiculo.text = veiculo.tipoVeiculo
            txtMarcaModelo.text = "${veiculo.marca} ${veiculo.modelo} (${veiculo.ano})"
            txtMotorista.text = veiculo.motorista
        }
    }

    class VeiculoDiffCallback : DiffUtil.ItemCallback<Veiculo>() {
        override fun areItemsTheSame(oldItem: Veiculo, newItem: Veiculo): Boolean {
            return oldItem.placa == newItem.placa
        }

        override fun areContentsTheSame(oldItem: Veiculo, newItem: Veiculo): Boolean {
            return oldItem == newItem
        }
    }
}
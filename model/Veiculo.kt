package com.alexandre.controledefrota.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "veiculos")
data class Veiculo(
    @PrimaryKey
    val placa: String,
    val marca: String,
    val modelo: String,
    val ano: String,
    val tipoVeiculo: String,
    val motorista: String,
    val chassi: String = "",
    val renavan: String = "",
    val onus: String = "",
    val valor: String = "",
    val cep: String = ""
)
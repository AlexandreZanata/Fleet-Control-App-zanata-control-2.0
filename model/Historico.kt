package com.alexandre.controledefrota.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "historicos",
    foreignKeys = [
        ForeignKey(
            entity = Veiculo::class,
            parentColumns = ["placa"],
            childColumns = ["placaVeiculo"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("placaVeiculo")]
)
data class Historico(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val placaVeiculo: String,
    val tipoServico: String,
    val quilometragem: Int,
    val data: Date,
    val comentario: String,
    val custoServico: Double,
    val tecnicoResponsavel: String = "",
    val statusServico: String = "Concluído"
)
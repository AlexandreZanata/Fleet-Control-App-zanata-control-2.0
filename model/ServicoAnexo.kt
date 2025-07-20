package com.alexandre.controledefrota.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "servico_anexos",
    foreignKeys = [
        ForeignKey(
            entity = Historico::class,
            parentColumns = ["id"],
            childColumns = ["historicoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("historicoId")]
)
data class ServicoAnexo(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val historicoId: Long,
    val caminhoArquivo: String,
    val tipoArquivo: String,
    val nomeArquivo: String,
    val dataCriacao: Long = System.currentTimeMillis()
)
package com.alexandre.controledefrota.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.alexandre.controledefrota.model.ServicoAnexo

@Dao
interface ServicoAnexoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(anexo: ServicoAnexo): Long

    @Delete
    suspend fun deletar(anexo: ServicoAnexo)

    @Query("SELECT * FROM servico_anexos WHERE historicoId = :historicoId ORDER BY dataCriacao DESC")
    fun obterAnexosPorServico(historicoId: Long): LiveData<List<ServicoAnexo>>

    @Query("SELECT * FROM servico_anexos WHERE id = :id")
    suspend fun obterAnexoPorId(id: Long): ServicoAnexo?
}
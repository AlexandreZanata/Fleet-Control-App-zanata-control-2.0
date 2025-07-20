package com.alexandre.controledefrota.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.alexandre.controledefrota.model.Historico

@Dao
interface HistoricoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(historico: Historico): Long

    @Update
    suspend fun atualizar(historico: Historico)

    @Delete
    suspend fun deletar(historico: Historico)

    @Query("SELECT * FROM historicos ORDER BY data DESC")
    fun obterTodos(): LiveData<List<Historico>>

    @Query("SELECT * FROM historicos WHERE placaVeiculo = :placa ORDER BY data DESC")
    fun obterHistoricoPorVeiculo(placa: String): LiveData<List<Historico>>

    @Query("SELECT * FROM historicos WHERE id = :id")
    suspend fun obterPorId(id: Long): Historico?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirTodos(historicos: List<Historico>)
}
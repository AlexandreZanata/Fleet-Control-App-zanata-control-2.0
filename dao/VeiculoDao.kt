package com.alexandre.controledefrota.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.alexandre.controledefrota.model.Veiculo

@Dao
interface VeiculoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(veiculo: Veiculo)

    @Update
    suspend fun atualizar(veiculo: Veiculo)

    @Delete
    suspend fun deletar(veiculo: Veiculo)

    @Query("SELECT * FROM veiculos ORDER BY placa ASC")
    fun obterTodos(): LiveData<List<Veiculo>>

    @Query("SELECT * FROM veiculos WHERE placa = :placa")
    suspend fun obterPorPlaca(placa: String): Veiculo?

    @Query("SELECT * FROM veiculos WHERE motorista LIKE '%' || :motorista || '%'")
    fun buscarPorMotorista(motorista: String): LiveData<List<Veiculo>>

    // Adicione este método
    @Query("SELECT * FROM veiculos WHERE motorista LIKE :query ORDER BY placa ASC")
    suspend fun buscarPorMotoristaSync(query: String): List<Veiculo>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirTodos(veiculos: List<Veiculo>)
}
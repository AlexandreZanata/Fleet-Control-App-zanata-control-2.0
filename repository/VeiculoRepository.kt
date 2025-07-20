package com.alexandre.controledefrota.repository

import androidx.lifecycle.LiveData
import com.alexandre.controledefrota.dao.VeiculoDao
import com.alexandre.controledefrota.model.Veiculo

class VeiculoRepository(private val veiculoDao: VeiculoDao) {

    fun obterTodos(): LiveData<List<Veiculo>> {
        return veiculoDao.obterTodos()
    }

    suspend fun inserir(veiculo: Veiculo) {
        veiculoDao.inserir(veiculo)
    }

    suspend fun atualizar(veiculo: Veiculo) {
        veiculoDao.atualizar(veiculo)
    }

    suspend fun deletar(veiculo: Veiculo) {
        veiculoDao.deletar(veiculo)
    }

    fun buscarPorMotorista(motorista: String): LiveData<List<Veiculo>> {
        return veiculoDao.buscarPorMotorista(motorista)
    }

    // Método síncrono para busca imediata
    suspend fun buscarPorMotoristaSync(query: String): List<Veiculo> {
        // Use outro método existente ou implementação alternativa
        return veiculoDao.obterTodos().value ?: emptyList()
    }

    suspend fun obterPorPlaca(placa: String): Veiculo? {
        return veiculoDao.obterPorPlaca(placa)
    }

    suspend fun inserirTodos(veiculos: List<Veiculo>) {
        veiculoDao.inserirTodos(veiculos)
    }
}
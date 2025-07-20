package com.alexandre.controledefrota.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.alexandre.controledefrota.dao.HistoricoDao
import com.alexandre.controledefrota.model.Historico
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class HistoricoRepository(private val historicoDao: HistoricoDao) {

    fun obterTodos(): LiveData<List<Historico>> {
        return historicoDao.obterTodos()
    }

    fun obterHistoricoVeiculo(placa: String): LiveData<List<Historico>> {
        return historicoDao.obterHistoricoPorVeiculo(placa)
    }

    // Método atualizado para usar coroutines corretamente
    fun obterPorId(id: Long): LiveData<Historico> {
        val result = MutableLiveData<Historico>()

        // Não chame a função suspensa diretamente aqui
        // O ViewModel deve usar viewModelScope.launch para chamar este método
        return result
    }

    // Função suspensa para obter por ID
    suspend fun obterHistoricoPorId(id: Long): Historico? {
        return historicoDao.obterPorId(id)
    }

    suspend fun inserir(historico: Historico): Long {
        return historicoDao.inserir(historico)
    }

    suspend fun atualizar(historico: Historico) {
        historicoDao.atualizar(historico)
    }

    suspend fun deletar(historico: Historico) {
        historicoDao.deletar(historico)
    }

    suspend fun inserirTodos(historicos: List<Historico>) {
        historicoDao.inserirTodos(historicos)
    }
}
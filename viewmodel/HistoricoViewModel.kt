package com.alexandre.controledefrota.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.alexandre.controledefrota.database.AppDatabase
import com.alexandre.controledefrota.model.Historico
import com.alexandre.controledefrota.repository.HistoricoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HistoricoViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: HistoricoRepository
    val todosHistoricos: LiveData<List<Historico>>

    init {
        val historicoDao = AppDatabase.getDatabase(application).historicoDao()
        repository = HistoricoRepository(historicoDao)
        todosHistoricos = repository.obterTodos()
    }

    fun obterHistoricoVeiculo(placa: String): LiveData<List<Historico>> {
        return repository.obterHistoricoVeiculo(placa)
    }

    fun obterPorId(id: Long): LiveData<Historico> {
        val result = MutableLiveData<Historico>()

        viewModelScope.launch(Dispatchers.IO) {
            val historico = repository.obterHistoricoPorId(id)
            withContext(Dispatchers.Main) {
                result.value = historico
            }
        }

        return result
    }

    fun inserirHistorico(historico: Historico, callback: (Long) -> Unit) {
        viewModelScope.launch {
            val id = withContext(Dispatchers.IO) {
                repository.inserir(historico)
            }
            callback(id)
        }
    }

    fun atualizarHistorico(historico: Historico) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.atualizar(historico)
        }
    }

    fun deletarHistorico(historico: Historico) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deletar(historico)
        }
    }

    fun inserirTodos(historicos: List<Historico>) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.inserirTodos(historicos)
        }
    }
}
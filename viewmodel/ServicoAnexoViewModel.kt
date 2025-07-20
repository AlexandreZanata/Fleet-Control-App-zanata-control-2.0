package com.alexandre.controledefrota.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.alexandre.controledefrota.database.AppDatabase
import com.alexandre.controledefrota.model.ServicoAnexo
import com.alexandre.controledefrota.repository.ServicoAnexoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ServicoAnexoViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ServicoAnexoRepository

    init {
        val servicoAnexoDao = AppDatabase.getDatabase(application).servicoAnexoDao()
        repository = ServicoAnexoRepository(servicoAnexoDao)
    }

    fun inserirAnexo(anexo: ServicoAnexo) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.inserir(anexo)
        }
    }

    fun deletarAnexo(anexo: ServicoAnexo) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deletar(anexo)
        }
    }

    fun obterAnexosPorServico(historicoId: Long): LiveData<List<ServicoAnexo>> {
        return repository.obterAnexosPorServico(historicoId)
    }
}
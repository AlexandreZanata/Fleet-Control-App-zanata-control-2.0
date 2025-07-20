package com.alexandre.controledefrota.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.alexandre.controledefrota.database.AppDatabase
import com.alexandre.controledefrota.repository.VeiculoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.alexandre.controledefrota.model.Veiculo

class VeiculoViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: VeiculoRepository
    val todosVeiculos: LiveData<List<Veiculo>>
    val resultadosPesquisa = MutableLiveData<List<Veiculo>>()

    init {
        val veiculoDao = AppDatabase.getDatabase(application).veiculoDao()
        repository = VeiculoRepository(veiculoDao)
        todosVeiculos = repository.obterTodos()
    }

    fun inserir(veiculo: Veiculo) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.inserir(veiculo)
        }
    }

    fun atualizar(veiculo: Veiculo) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.atualizar(veiculo)
        }
    }

    fun deletar(veiculo: Veiculo) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deletar(veiculo)
        }
    }

    fun buscarPorMotorista(motorista: String): LiveData<List<Veiculo>> {
        return repository.buscarPorMotorista(motorista)
    }

    fun obterVeiculoPorPlaca(placa: String): LiveData<Veiculo?> {
        return obterPorPlaca(placa)
    }

    fun inserirTodos(veiculos: List<Veiculo>) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.inserirTodos(veiculos)
        }
    }

    // Método para importar veículos
    fun importarVeiculos(veiculos: List<Veiculo>) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.inserirTodos(veiculos)
        }
    }

    // Método para pesquisar veículos
    fun pesquisar(query: String) {
        if (query.isEmpty()) {
            resultadosPesquisa.postValue(emptyList())
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val resultados = repository.buscarPorMotoristaSync(query)
            withContext(Dispatchers.Main) {
                resultadosPesquisa.value = resultados
            }
        }
    }

    fun obterPorPlaca(placa: String): LiveData<Veiculo?> {
        val result = MutableLiveData<Veiculo?>()

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val veiculo = repository.obterPorPlaca(placa)
                Log.d("VeiculoViewModel", "Veículo obtido por placa $placa: ${veiculo?.placa ?: "nulo"}")
                withContext(Dispatchers.Main) {
                    result.value = veiculo
                }
            } catch (e: Exception) {
                Log.e("VeiculoViewModel", "Erro ao obter veículo por placa: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    result.value = null
                }
            }
        }

        return result
    }
}